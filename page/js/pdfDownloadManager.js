/*
 * pdfDownloadManager.js — 紀念 PDF 下載 Modal 控制器
 *
 * 流程：
 *  1. 使用者按「下載 PDF」按鈕 → 開 modal
 *  2. 自動載入隱藏 iframe（expert-data-v8-pdf.jsp?embed=1&shotdata_id=...）
 *  3. iframe 內容渲染完，postMessage 'pdf-ready' 給父視窗
 *  4. 父視窗對 iframe 內 .pdf-page 跑 html2canvas → jsPDF 合成 PDF
 *  5. 同時觸發瀏覽器下載 + 上傳 PDF blob 到 server
 *  6. 上傳成功後更新 QR Code，指向 server 上的 PDF URL
 *
 * 依賴：window.PDF_CONTEXT（v8.jsp 注入）、html2canvas、jsPDF、QRCode（lib）
 */
(function () {
	'use strict';

	const STORAGE_KEY_NICKNAME = 'gm_pdf_nickname';
	const STORAGE_KEY_VENUE    = 'gm_pdf_venue';
	const STORAGE_KEY_REMEMBER = 'gm_pdf_remember';
	const POLL_TIMEOUT_MS = 2 * 60 * 1000 + 5000; // 2 分 5 秒（對齊 videoPoller）
	const PDF_PAGE_PATH = 'expert-data-v8-pdf.jsp';
	// 用 Servlet 而非 JSP，避免 Tomcat 8.0.52 + Java 17 的 JDT 編譯問題
	// 對應 PdfUploadServlet 的 @WebServlet("/service/pdf-upload")
	const PDF_UPLOAD_PATH = 'pdf-upload';

	const ctx = window.PDF_CONTEXT || {};
	const shotDataId = ctx.shotDataId || '';

	// ============== DOM ==============
	const triggerBtn   = document.getElementById('btn-download-pdf');
	const modal        = document.getElementById('pdf-modal');
	const inputNick    = document.getElementById('pdf-input-nickname');
	const inputVenue   = document.getElementById('pdf-input-venue');
	const inputRemember= document.getElementById('pdf-input-remember');
	const btnDownload  = document.getElementById('pdf-btn-download');
	const btnLabel     = btnDownload ? btnDownload.querySelector('.pdf-btn-label') : null;
	const statusEl     = document.getElementById('pdf-status');
	const qrCanvas     = document.getElementById('pdf-qr-canvas');
	const pdfHost      = document.getElementById('pdf-host');

	if (!triggerBtn || !modal || !btnDownload || !pdfHost) {
		console.warn('pdfDownloadManager: required DOM elements missing.');
		return;
	}

	// ============== 狀態 ==============
	let videoTimedOut = false;
	let videoReadyResolved = false;
	let lastUploadedUrl = null;

	// ============== 初始化 ==============
	function init() {
		// 載入 localStorage
		try {
			const remember = localStorage.getItem(STORAGE_KEY_REMEMBER);
			if (remember === 'false') inputRemember.checked = false;
			const savedNick = localStorage.getItem(STORAGE_KEY_NICKNAME);
			const savedVenue = localStorage.getItem(STORAGE_KEY_VENUE);
			if (savedNick && inputRemember.checked) inputNick.value = savedNick;
			if (savedVenue && inputRemember.checked) inputVenue.value = savedVenue;
		} catch (e) { /* localStorage 無法用 */ }

		if (!inputNick.value) inputNick.value = ctx.defaultNickname || 'Guest';
		if (!inputVenue.value) inputVenue.placeholder = '室內高爾夫模擬器';

		triggerBtn.addEventListener('click', openModal);
		btnDownload.addEventListener('click', startDownload);

		// 點擊 backdrop 或關閉按鈕
		modal.addEventListener('click', (e) => {
			if (e.target.closest('[data-pdf-close]')) closeModal();
		});

		document.addEventListener('keydown', (e) => {
			if (e.key === 'Escape' && modal.classList.contains('is-open')) closeModal();
		});

		// 等待影片分析完成（依現有 videoPoller）
		setupVideoReadinessTracking();
	}

	function setupVideoReadinessTracking() {
		const wantsFront = !!ctx.frontExpected;
		const wantsSide  = !!ctx.sideExpected;
		// 沒有預期任何影片 → 立刻 ready
		if (!wantsFront && !wantsSide) {
			videoReadyResolved = true;
			return;
		}

		// 已有 ready flag（v8.jsp 開頁時已分析完）
		try {
			if (typeof frontAnalyzReady !== 'undefined' && typeof sideAnalyzReady !== 'undefined') {
				const f = wantsFront ? frontAnalyzReady : true;
				const s = wantsSide  ? sideAnalyzReady  : true;
				if (f && s) { videoReadyResolved = true; return; }
			}
		} catch (e) { /* 變數不存在 */ }

		// 還沒 ready → 監聽 videoPoller 完成 / 超時
		const start = Date.now();
		const checkInterval = setInterval(() => {
			let ready = false;
			let timedOut = (Date.now() - start) > POLL_TIMEOUT_MS;
			try {
				const f = wantsFront ? !!window.frontAnalyzReady : true;
				const s = wantsSide  ? !!window.sideAnalyzReady  : true;
				ready = f && s;
			} catch (e) {}
			if (ready) {
				clearInterval(checkInterval);
				videoReadyResolved = true;
			} else if (timedOut) {
				clearInterval(checkInterval);
				videoTimedOut = true;
				videoReadyResolved = true;
			}
		}, 1500);
	}

	// ============== Modal 控制 ==============
	function openModal() {
		modal.classList.add('is-open');
		modal.setAttribute('aria-hidden', 'false');
		setStatus('');

		// 按鈕狀態：未 ready 時 disable
		updateDownloadButtonState();

		// QR 區塊重置
		setQrLoading('PDF 生成中...');
		lastUploadedUrl = null;

		// 持續更新按鈕狀態
		const tick = setInterval(() => {
			if (!modal.classList.contains('is-open')) { clearInterval(tick); return; }
			updateDownloadButtonState();
		}, 800);
	}

	function closeModal() {
		modal.classList.remove('is-open');
		modal.setAttribute('aria-hidden', 'true');
		// 清掉 iframe 釋放資源
		pdfHost.innerHTML = '';
	}

	function updateDownloadButtonState() {
		if (btnDownload.getAttribute('aria-busy') === 'true') return;
		const ready = videoReadyResolved;
		btnDownload.disabled = !ready;
		if (!ready) {
			btnLabel.textContent = '影片分析中…';
		} else if (videoTimedOut) {
			btnLabel.textContent = '下載 PDF（影片未完成，將略過影像）';
		} else {
			btnLabel.textContent = '下載到電腦';
		}
	}

	function setStatus(msg, type) {
		statusEl.textContent = msg || '';
		statusEl.classList.remove('is-error', 'is-ok');
		if (type === 'error') statusEl.classList.add('is-error');
		else if (type === 'ok') statusEl.classList.add('is-ok');
	}

	function setQrLoading(msg) {
		if (!qrCanvas) return;
		qrCanvas.classList.remove('is-ready');
		qrCanvas.innerHTML = '<div class="pdf-qr-loading">' + (msg || 'PDF 生成中...') + '</div>';
	}

	function setQrCode(url) {
		if (!qrCanvas) return;
		qrCanvas.innerHTML = '';
		try {
			new QRCode(qrCanvas, {
				text: url,
				width: 132,
				height: 132,
				colorDark: '#1a1a1a',
				colorLight: '#ffffff',
				correctLevel: QRCode.CorrectLevel.M
			});
			qrCanvas.classList.add('is-ready');
		} catch (e) {
			console.warn('QRCode error:', e);
			qrCanvas.innerHTML = '<div class="pdf-qr-loading">QR 產生失敗</div>';
		}
	}

	// ============== 下載主流程 ==============
	function startDownload() {
		if (btnDownload.disabled) return;
		// 防呆：shotDataId 必須是純數字字串（避免 JSP 把 null/undefined 注入成字串 "null"）
		if (!shotDataId || shotDataId === 'null' || shotDataId === 'undefined' || !/^\d+$/.test(String(shotDataId))) {
			console.error('[PDF] invalid shotDataId:', shotDataId, 'PDF_CONTEXT:', ctx);
			setStatus('找不到擊球編號（shotDataId=' + shotDataId + '）', 'error');
			return;
		}

		btnDownload.setAttribute('aria-busy', 'true');
		btnDownload.disabled = true;
		btnLabel.textContent = '產生中…';
		setStatus('正在產生 PDF，請稍候…');

		const nickname = (inputNick.value || '').trim() || ctx.defaultNickname || 'Guest';
		const venue    = (inputVenue.value || '').trim() || '室內高爾夫模擬器';

		// 寫入 localStorage
		try {
			localStorage.setItem(STORAGE_KEY_REMEMBER, inputRemember.checked ? 'true' : 'false');
			if (inputRemember.checked) {
				localStorage.setItem(STORAGE_KEY_NICKNAME, nickname);
				localStorage.setItem(STORAGE_KEY_VENUE, venue);
			}
		} catch (e) { /* ignore */ }

		// 建 iframe 觸發 pdf.jsp 渲染
		pdfHost.innerHTML = '';
		const iframe = document.createElement('iframe');
		iframe.style.cssText = 'border:0;width:794px;height:1200px;background:#fff;';
		const params = new URLSearchParams();
		params.set('shotdata_id', shotDataId);
		params.set('nickname', nickname);
		params.set('venue', venue);
		params.set('embed', '1');
		// 帶入原始查詢字串，讓 ExpertData.processRequest 拿得到所需參數
		const ownParams = new URLSearchParams(window.location.search);
		ownParams.forEach((v, k) => {
			if (!params.has(k)) params.set(k, v);
		});
		iframe.src = PDF_PAGE_PATH + '?' + params.toString();
		pdfHost.appendChild(iframe);

		// 等 postMessage
		const onMessage = function (ev) {
			if (!ev.data || ev.data.type !== 'pdf-ready') return;
			if (ev.source !== iframe.contentWindow) return;
			window.removeEventListener('message', onMessage);
			capturePdf(iframe, ev.data, nickname).catch(err => {
				console.error('PDF capture failed:', err);
				setStatus('PDF 產生失敗：' + (err && err.message ? err.message : err), 'error');
				resetDownloadButton();
			});
		};
		window.addEventListener('message', onMessage);

		// 60 秒沒收到訊號就超時
		setTimeout(() => {
			if (btnDownload.getAttribute('aria-busy') === 'true' && pdfHost.contains(iframe)) {
				window.removeEventListener('message', onMessage);
				setStatus('PDF 產生逾時，請重試', 'error');
				resetDownloadButton();
			}
		}, 60000);
	}

	async function capturePdf(iframe, readyPayload, nickname) {
		const idoc = iframe.contentDocument || iframe.contentWindow.document;
		const pages = idoc.querySelectorAll('.pdf-page:not(.is-hidden)');
		if (!pages || pages.length === 0) throw new Error('找不到 PDF 頁面');

		setStatus('擷取頁面內容…');
		// 給 fonts/圖片一點時間
		await wait(300);

		const pageImages = [];
		for (let i = 0; i < pages.length; i++) {
			const canvas = await html2canvas(pages[i], {
				backgroundColor: '#ffffff',
				scale: 2,
				useCORS: true,
				logging: false,
				windowWidth: 794,
				windowHeight: 1123
			});
			pageImages.push(canvas.toDataURL('image/jpeg', 0.92));
		}

		setStatus('合成 PDF…');
		const { jsPDF } = window.jspdf;
		const pdf = new jsPDF({ unit: 'mm', format: 'a4', orientation: 'portrait' });
		const pageW = 210, pageH = 297;
		pageImages.forEach((img, idx) => {
			if (idx > 0) pdf.addPage();
			pdf.addImage(img, 'JPEG', 0, 0, pageW, pageH, undefined, 'FAST');
		});

		const recordId = readyPayload.recordId || ('GM-' + shotDataId);
		const fileName = 'GolfMaster-紀念-' + recordId + '.pdf';
		const blob = pdf.output('blob');

		// 觸發瀏覽器下載
		const blobUrl = URL.createObjectURL(blob);
		const a = document.createElement('a');
		a.href = blobUrl;
		a.download = fileName;
		document.body.appendChild(a);
		a.click();
		document.body.removeChild(a);
		setTimeout(() => URL.revokeObjectURL(blobUrl), 1000);

		setStatus('PDF 已下載到電腦，正在上傳供手機掃碼…');

		// 上傳到 server
		try {
			const url = await uploadPdf(blob, fileName);
			lastUploadedUrl = url;
			setQrCode(url);
			setStatus('完成！可掃 QR Code 下載到手機', 'ok');
		} catch (e) {
			console.warn('PDF upload failed:', e);
			setStatus('已下載到電腦，但 QR 上傳失敗：' + (e.message || e), 'error');
			setQrLoading('上傳失敗，無法產生 QR');
		}

		resetDownloadButton();
	}

	function resetDownloadButton() {
		btnDownload.removeAttribute('aria-busy');
		btnDownload.disabled = false;
		updateDownloadButtonState();
	}

	async function uploadPdf(blob, fileName) {
		// 用 raw application/pdf 上傳，避免引入 commons-fileupload。
		// shotdata_id 走 URL query，body 是純 PDF 二進位流。
		const url = PDF_UPLOAD_PATH + '?shotdata_id=' + encodeURIComponent(shotDataId);
		console.log('[PDF] uploading to', url, 'size=', blob.size);
		const resp = await fetch(url, {
			method: 'POST',
			headers: { 'Content-Type': 'application/pdf' },
			body: blob
		});
		const text = (await resp.text()).trim();
		console.log('[PDF] upload response status=', resp.status, 'body=', text.slice(0, 500));
		if (!resp.ok) throw new Error('HTTP ' + resp.status + ': ' + text.slice(0, 200));
		let data;
		try {
			data = JSON.parse(text);
		} catch (e) {
			console.error('[PDF] JSON.parse failed on response:', e, '\nFull response:', text);
			throw new Error('回應解析失敗（' + e.message + '），原始內容：' + text.slice(0, 200));
		}
		if (data.status !== 'ok' || !data.url) throw new Error(data.message || '上傳失敗');
		return data.url;
	}

	function wait(ms) { return new Promise(r => setTimeout(r, ms)); }

	// ============== Boot ==============
	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();