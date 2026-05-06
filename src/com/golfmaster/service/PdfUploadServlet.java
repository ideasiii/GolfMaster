package com.golfmaster.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * 紀念 PDF 上傳處理 Servlet。
 *
 * 為何不用 JSP：Tomcat 8.0.52 內建的 Eclipse JDT compiler 不認得 Java 17 的某些
 * standard library class file（會丟 ClassFormatException），所以用 JSP 寫的版本
 * 在 Java 17 + Tomcat 8.0.52 環境下無法編譯成功。改成 Servlet 就完全跳過 JSP 編譯。
 *
 * 流程：
 *  - POST /GolfMaster/service/pdf-upload?shotdata_id=N
 *  - body 是純 application/pdf 二進位
 *  - 寫到 /opt/golfmaster/pdf-temp/GM-{shotDataId}.pdf  (一桿一檔，重新下載直接覆寫)
 *  - 回 JSON {"status":"ok","url":"...","overwritten":true|false}
 *
 * 為什麼一桿一檔不帶 timestamp：
 *  - 同一桿邏輯上只該有一個紀念 PDF
 *  - 使用者多次點下載（換暱稱/場地）→ 覆寫舊版，QR URL 永遠有效
 *  - 永久保留也不會無限累積，磁碟用量上限 = 總擊球數 × 每檔大小
 */
@WebServlet(
    name = "PdfUploadServlet",
    urlPatterns = "/service/pdf-upload",
    asyncSupported = false
)
public class PdfUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String VERSION = "servlet-v5-2026-05-06";
    private static final int  MAX_BYTES = 10 * 1024 * 1024;
    /**
     * 對外 URL 路徑：優先讀 context.xml 的 &lt;Environment name="pdfTempUrlPath"&gt;，
     * 沒設定就用此預設值。須對應 &lt;PreResources webAppMount&gt;。
     * 實際 server 寫檔路徑透過 application.getRealPath(urlPath) 動態反查 &lt;PreResources base&gt;。
     */
    private static final String DEFAULT_URL_PATH = "/downloads/pdf-temp/";
    /** getRealPath 也失敗時的最終 fallback（建議部署時保證 PreResources 設定正確） */
    private static final String HARDCODED_FALLBACK_DIR = "/opt/golfmaster/pdf-temp/";

    /**
     * 讀 context.xml 的 Environment 變數。失敗回 null（不丟例外）。
     * 跟 com.golfmaster.service.Config.getParameter 同邏輯，內嵌避免跨類別依賴。
     */
    private static String readEnv(String name) {
        try {
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            Object v = ctx.lookup(name);
            return (v instanceof String) ? (String) v : null;
        } catch (NamingException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 標準化 URL path：保證頭尾都有 "/"。
     */
    private static String normalizeUrlPath(String raw) {
        if (raw == null || raw.trim().isEmpty()) return DEFAULT_URL_PATH;
        String s = raw.trim();
        if (!s.startsWith("/")) s = "/" + s;
        if (!s.endsWith("/"))   s = s + "/";
        return s;
    }

    /** 第一次成功偵測到的 LAN IP，後續沿用 */
    private static volatile String cachedLanIp = null;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("X-Pdf-Upload-Version", VERSION);
        response.setHeader("Cache-Control", "no-store");

        File writtenFile = null;

        try {
            // === 1. 解析參數 ===
            String shotDataId = request.getParameter("shotdata_id");
            String contentType = request.getContentType();

            if (shotDataId == null || shotDataId.trim().isEmpty()) {
                writeJson(response, 400, new JSONObject()
                    .put("status", "error").put("message", "missing shotdata_id"));
                return;
            }
            shotDataId = shotDataId.trim();
            if (!shotDataId.matches("\\d+")) {
                writeJson(response, 400, new JSONObject()
                    .put("status", "error").put("message", "invalid shotdata_id"));
                return;
            }

            if (contentType == null || !contentType.toLowerCase().contains("application/pdf")) {
                writeJson(response, 415, new JSONObject()
                    .put("status", "error")
                    .put("message", "expected Content-Type application/pdf, got: " + contentType));
                return;
            }

            int contentLength = request.getContentLength();
            if (contentLength > MAX_BYTES) {
                writeJson(response, 413, new JSONObject()
                    .put("status", "error").put("message", "file too large"));
                return;
            }

            // === 2. 決定目的地 ===
            // 步驟：
            //  1. 從 context.xml 讀 pdfTempUrlPath（沒設用 DEFAULT_URL_PATH）
            //  2. 用 application.getRealPath(urlPath) 透過 <PreResources webAppMount → base> 對應，自動拿到實際 server 路徑
            //  3. 失敗回 HARDCODED_FALLBACK_DIR；都不行才落到系統 tmpdir
            String urlPath = normalizeUrlPath(readEnv("pdfTempUrlPath"));
            String tmpFallback = System.getProperty("java.io.tmpdir") + File.separator
                + "golfmaster-pdf-temp" + File.separator;

            String resolvedRealPath = getServletContext().getRealPath(urlPath);
            File targetDir = (resolvedRealPath != null) ? new File(resolvedRealPath) : new File(HARDCODED_FALLBACK_DIR);
            boolean usingPrimary = true;
            StringBuilder dirDebug = new StringBuilder();
            dirDebug.append("urlPath=").append(urlPath);
            dirDebug.append("; getRealPath=").append(resolvedRealPath);
            dirDebug.append("; targetDir=").append(targetDir.getAbsolutePath());

            if (!targetDir.exists()) {
                boolean created = targetDir.mkdirs();
                dirDebug.append("; mkdirs=").append(created);
                if (!created) {
                    targetDir = new File(tmpFallback);
                    usingPrimary = false;
                    targetDir.mkdirs();
                    dirDebug.append("; tmpFallback=").append(targetDir.getAbsolutePath());
                }
            }
            if (!targetDir.isDirectory() || !targetDir.canWrite()) {
                dirDebug.append("; not writable (isDir=")
                    .append(targetDir.isDirectory()).append(", canWrite=")
                    .append(targetDir.canWrite()).append(")");
                targetDir = new File(tmpFallback);
                usingPrimary = false;
                targetDir.mkdirs();
                dirDebug.append("; tmpFallback=").append(targetDir.getAbsolutePath());
            }

            // === 3. 檔名 — 一桿一檔，重新下載直接覆寫，避免無限累積 ===
            // 同一個 shot_data_id 永遠對應同一個檔，QR Code URL 也不會變
            String filename = "GM-" + shotDataId + ".pdf";
            File targetFile = new File(targetDir, filename);
            boolean overwritten = targetFile.exists();
            writtenFile = targetFile;

            // === 4. 串流寫檔 ===
            long bytesWritten = 0;
            try (InputStream in = request.getInputStream();
                 OutputStream fos = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    bytesWritten += n;
                    if (bytesWritten > MAX_BYTES) {
                        writeJson(response, 413, new JSONObject()
                            .put("status", "error").put("message", "file too large (stream)"));
                        return;
                    }
                    fos.write(buf, 0, n);
                }
            }

            // === 5. PDF magic bytes ===
            try (RandomAccessFile raf = new RandomAccessFile(targetFile, "r")) {
                byte[] magic = new byte[4];
                raf.readFully(magic);
                if (!(magic[0] == '%' && magic[1] == 'P' && magic[2] == 'D' && magic[3] == 'F')) {
                    targetFile.delete();
                    writtenFile = null;
                    writeJson(response, 400, new JSONObject()
                        .put("status", "error").put("message", "not a valid PDF"));
                    return;
                }
            }

            // === 6. 對外 URL ===
            // 解析優先序（預期 99% 走 (3) 自動命中，不需任何設定）：
            //  (1) pdfPublicHost (JNDI)         緊急備案 — 自動偵測都失準時才動，不必重打 WAR
            //                                   （Tomcat 允許 conf/Catalina/localhost/{app}.xml 外部覆寫）
            //  (2) System property -Dgolfmaster.pdf.host=...
            //                                   另一個逃生口（廠商改 setenv.sh）
            //  (3) request.getServerName()      非 localhost/127.0.0.1 直接用，最高擬真
            //  (4) detectLanIpViaDefaultRoute() UDP-socket connect 走 OS 路由表，
            //                                   multi-NIC 機器（docker0 + WiFi）也選對
            //  (5) detectLanIpByEnumeration()   最後備援：掃介面，跳過 docker/br-/virbr
            String scheme = request.getScheme();
            int port = request.getServerPort();
            String contextPath = request.getContextPath();

            String hostSource;
            String host;
            String envHost = readEnv("pdfPublicHost");
            String sysPropHost = System.getProperty("golfmaster.pdf.host");
            if (envHost != null && !envHost.trim().isEmpty()) {
                host = envHost.trim();
                hostSource = "env";
            } else if (sysPropHost != null && !sysPropHost.trim().isEmpty()) {
                host = sysPropHost.trim();
                hostSource = "sysprop";
            } else {
                String reqHost = request.getServerName();
                if (reqHost != null && !"localhost".equalsIgnoreCase(reqHost) && !"127.0.0.1".equals(reqHost)) {
                    host = reqHost;
                    hostSource = "request";
                } else {
                    String routed = detectLanIpViaDefaultRoute();
                    if (routed != null) {
                        host = routed;
                        hostSource = "defaultRoute";
                    } else {
                        String enumIp = detectLanIpByEnumeration();
                        if (enumIp != null) {
                            host = enumIp;
                            hostSource = "enum";
                        } else {
                            host = (reqHost != null) ? reqHost : "localhost";
                            hostSource = "fallback";
                        }
                    }
                }
            }
            String publicUrl = scheme + "://" + host + ":" + port + contextPath + urlPath + filename;

            // === 7. 回應 ===
            writeJson(response, 200, new JSONObject()
                .put("status", "ok")
                .put("url", publicUrl)
                .put("file", filename)
                .put("size", bytesWritten)
                .put("savedTo", targetFile.getAbsolutePath())
                .put("usingPrimary", usingPrimary)
                .put("overwritten", overwritten)
                .put("dirDebug", dirDebug.toString())
                .put("hostUsed", host)
                .put("hostSource", hostSource)
                .put("version", VERSION));

        } catch (Throwable t) {
            if (writtenFile != null && writtenFile.exists()) {
                try { writtenFile.delete(); } catch (Exception ignored) {}
            }

            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            if (stack.length() > 1500) stack = stack.substring(0, 1500);

            try {
                writeJson(response, 500, new JSONObject()
                    .put("status", "error")
                    .put("message", t.getClass().getSimpleName() + ": "
                        + (t.getMessage() == null ? "(no message)" : t.getMessage()))
                    .put("stack", stack)
                    .put("version", VERSION));
            } catch (Exception ignored) {}
        }
    }

    /**
     * GET 回個簡單訊息（debug 用，可 curl 確認 servlet 已部署）。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("X-Pdf-Upload-Version", VERSION);
        writeJson(response, 200, new JSONObject()
            .put("status", "ok")
            .put("message", "pdf-upload servlet alive; POST application/pdf with ?shotdata_id=N")
            .put("version", VERSION));
    }

    private static void writeJson(HttpServletResponse response, int status, JSONObject body) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(status);
        }
        response.getWriter().print(body.toString());
        response.getWriter().flush();
    }

    /**
     * 走 OS routing table 拿「真的會用來連外的網卡」的 IP。
     *
     * 原理：對 8.8.8.8:53 開 UDP socket 並 connect()。UDP connect 不會真的傳資料、
     * 也不需要對方存活，只是觸發 kernel 解 routing → 把 socket 綁到 default route
     * 那張網卡的 source IP。getLocalAddress() 取出來就是對的。
     *
     * 在多網卡機器（docker0 / 虛擬橋接 / WiFi）上比 NetworkInterface 列舉法可靠，
     * 因為列舉法分不出哪張是「真的對外」的。
     *
     * 失敗情境：機器完全離線（連 routing 都沒設）→ 回 null，由列舉法接手。
     */
    private static String detectLanIpViaDefaultRoute() {
        String ip = cachedLanIp;
        if (ip != null) return ip.isEmpty() ? null : ip;
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 53);
            InetAddress local = socket.getLocalAddress();
            if (local != null && !local.isAnyLocalAddress() && !local.isLoopbackAddress()) {
                String h = local.getHostAddress();
                if (h != null && h.indexOf(':') < 0) {
                    cachedLanIp = h;
                    return h;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * 備援偵測：列舉所有網卡找 site-local IP。
     *  - 跳過 loopback / 沒 up / virtual / 介面名為 docker* / br-* / virbr* / veth* / tun* / tap*
     *    （這些是 Docker / KVM bridge / VPN tunnel，常落在 172.x，但手機連不到）
     *  - 偏好 192.168.x（家用/辦公 WiFi 最常見），其次 10.x，最後才用 172.16~31.x
     *
     * 只在 detectLanIpViaDefaultRoute() 失敗（離線機器）時才會跑到這裡。
     */
    private static String detectLanIpByEnumeration() {
        String pick192 = null, pick10 = null, pick172 = null;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis != null && nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()) continue;
                String niName = ni.getName() == null ? "" : ni.getName().toLowerCase();
                if (niName.startsWith("docker") || niName.startsWith("br-")
                    || niName.startsWith("virbr") || niName.startsWith("veth")
                    || niName.startsWith("tun")   || niName.startsWith("tap")) {
                    continue;
                }
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a.isLoopbackAddress() || a.isLinkLocalAddress()) continue;
                    String h = a.getHostAddress();
                    if (h == null || h.indexOf(':') >= 0 || !a.isSiteLocalAddress()) continue;
                    if (h.startsWith("192.168.") && pick192 == null) pick192 = h;
                    else if (h.startsWith("10.")  && pick10  == null) pick10  = h;
                    else if (pick172 == null)                          pick172 = h;
                }
            }
        } catch (Exception ignored) {}
        return (pick192 != null) ? pick192 : (pick10 != null) ? pick10 : pick172;
    }
}