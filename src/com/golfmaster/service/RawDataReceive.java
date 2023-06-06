package com.golfmaster.service;

import java.awt.geom.FlatteningPathIterator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData.jmexParamData;
import com.golfmaster.moduel.DeviceData.spaceCapuleParamData;

public class RawDataReceive {
	public int insertJmexRawData(HttpServletRequest req) {
		printParam(req);
		jmexParamData paramData = null;
		String id = req.getParameter("id");
		String pelvisSpeed = req.getParameter("pelvisSpeed");
		String trunkSpeed = req.getParameter("trunkSpeed");
		String forearmSpeed = req.getParameter("forearmSpeed");
		String handSpeed = req.getParameter("handSpeed");
		String trunkForwardBendAddress = req.getParameter("trunkForwardBendAddress");
		String trunkForwardBendTop = req.getParameter("trunkForwardBendTop");
		String trunkForwardBendImpact = req.getParameter("trunkForwardBendImpact");
		String trunkSideBendAddress = req.getParameter("trunkSideBendAddress");
		String trunkSideBendTop = req.getParameter("trunkSideBendTop");
		String trunkSideBendImpact = req.getParameter("trunkSideBendImpact");
		String trunkRotationAddress = req.getParameter("trunkRotationAddress");
		String trunkRotationTop = req.getParameter("trunkRotationTop");
		String trunkRotationImpact = req.getParameter("trunkRotationImpact");
		String pelvisForwardBendAddress = req.getParameter("pelvisForwardBendAddress");
		String pelvisForwardBendTop = req.getParameter("pelvisForwardBendTop");
		String pelvisForwardBendImpact = req.getParameter("pelvisForwardBendImpact");
		String pelvisSideBendAddress = req.getParameter("pelvisSideBendAddress");
		String pelvisSideBendTop = req.getParameter("pelvisSideBendTop");
		String pelvisSideBendImpact = req.getParameter("pelvisSideBendImpact");
		String pelvisRotationAddress = req.getParameter("pelvisRotationAddress");
		String pelvisRotationTop = req.getParameter("pelvisRotationTop");
		String pelvisRotationImpact = req.getParameter("pelvisRotationImpact");
		String TSPelvis = req.getParameter("TSPelvis");
		String TSTrunk = req.getParameter("TSTrunk");
		String TSForearm = req.getParameter("TSForearm");
		String TSHand = req.getParameter("TSHand");
		String PSSPelvis = req.getParameter("PSSPelvis");
		String PSSTrunk = req.getParameter("PSSTrunk");
		String PSSForearm = req.getParameter("PSSForearm");
		String PSSHand = req.getParameter("PSSHand");
		String backSwingTime = req.getParameter("backSwingTime");
		String downSwingTime = req.getParameter("downSwingTime");
		String tempo = req.getParameter("tempo");
		String addressS_Posture = req.getParameter("addressS_Posture");
		String addressC_Posture = req.getParameter("addressC_Posture");
		String topX_Factor = req.getParameter("topX_Factor");
		String topReverseSpine = req.getParameter("topReverseSpine");
		String topFlatShoulder = req.getParameter("topFlatShoulder");
		String impactHipTurn = req.getParameter("impactHipTurn");
		paramData.id = Integer.parseInt(id);
		paramData.pelvisSpeed = Float.parseFloat(pelvisSpeed);
		paramData.trunkSpeed = Float.parseFloat(trunkSpeed);
		paramData.forearmSpeed = Float.parseFloat(forearmSpeed);
		paramData.handSpeed = Float.parseFloat(handSpeed);
		paramData.trunkForwardBendAddress = Float.parseFloat(trunkForwardBendAddress);
		paramData.trunkForwardBendTop = Float.parseFloat(trunkForwardBendTop);
		paramData.trunkForwardBendImpact = Float.parseFloat(trunkForwardBendImpact);
		paramData.trunkSideBendAddress = Float.parseFloat(trunkSideBendAddress);
		paramData.trunkSideBendTop = Float.parseFloat(trunkSideBendTop);
		paramData.trunkSideBendImpact = Float.parseFloat(trunkSideBendImpact);
		paramData.trunkRotationAddress = Float.parseFloat(trunkRotationAddress);
		paramData.trunkRotationTop = Float.parseFloat(trunkRotationTop);
		paramData.trunkRotationImpact = Float.parseFloat(trunkRotationImpact);
		paramData.pelvisForwardBendAddress = Float.parseFloat(pelvisForwardBendAddress);
		paramData.pelvisForwardBendTop = Float.parseFloat(pelvisForwardBendTop);
		paramData.pelvisForwardBendImpact = Float.parseFloat(pelvisForwardBendImpact);
		paramData.pelvisSideBendAddress = Float.parseFloat(pelvisSideBendAddress);
		paramData.pelvisSideBendTop = Float.parseFloat(pelvisSideBendTop);
		paramData.pelvisSideBendImpact = Float.parseFloat(pelvisSideBendImpact);
		paramData.pelvisRotationAddress = Float.parseFloat(pelvisRotationAddress);
		paramData.pelvisRotationTop = Float.parseFloat(pelvisRotationTop);
		paramData.pelvisRotationImpact = Float.parseFloat(pelvisRotationImpact);
		paramData.TSPelvis = Integer.parseInt(TSPelvis);
		paramData.TSTrunk = Integer.parseInt(TSTrunk);
		paramData.TSForearm = Integer.parseInt(TSForearm);
		paramData.TSHand = Integer.parseInt(TSHand);
		paramData.PSSPelvis = Integer.parseInt(PSSPelvis);
		paramData.PSSTrunk = Integer.parseInt(PSSTrunk);
		paramData.PSSForearm = Integer.parseInt(PSSForearm);
		paramData.PSSHand = Integer.parseInt(PSSHand);
		paramData.backSwingTime = Float.parseFloat(backSwingTime);
		paramData.downSwingTime = Float.parseFloat(downSwingTime);
		paramData.tempo = Float.parseFloat(tempo);
		paramData.addressS_Posture = addressS_Posture;
		paramData.addressC_Posture = addressC_Posture;
		paramData.topX_Factor = topX_Factor;
		paramData.topReverseSpine = topReverseSpine;
		paramData.topFlatShoulder = topFlatShoulder;
		paramData.impactHipTurn = impactHipTurn;

		JSONObject jsobj = new JSONObject();
		String errorType = "{\"code\":0,\"success\": true,\"message\": []}";
		int result = insertQueryJMEX(paramData,jsobj);
		return 0;
	}

	public int insertQueryJMEX(jmexParamData paramData, JSONObject jsonResp) {
		int stmtRs = -1;

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();

		strSQL = String.format(
				"INSERT INTO raw_data.JMEX (pelvisSpeed,trunkSpeed,forearmSpeed,handSpeed,trunkForwardBendAddress,trunkForwardBendTop,trunkForwardBendImpact,"
						+ "trunkSideBendAddress,trunkSideBendTop,trunkSideBendImpact,trunkRotationAddress,trunkRotationTop,trunkRotationImpact,"
						+ "pelvisForwardBendAddress,pelvisForwardBendTop,pelvisForwardBendImpact,pelvisSideBendAddress,pelvisSideBendTop,pelvisSideBendImpact,"
						+ "pelvisRotationAddress,pelvisRotationTop,pelvisRotationImpact,TSPelvis,TSTrunk,TSForearm,TSHand,PSSPelvis,PSSTrunk,PSSForearm,"
						+ "PSSHand,backSwingTime,downSwingTime,tempo,addressS_Posture,addressC_Posture,topX_Factor,topReverseSpine,topFlatShoulder,impactHipTurn)"
						+ " VALUES (%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%d,%d,%d,%d,%d,%d,%d,%d,%f,%f,%f,%s,%s,%s,%s,%s,%s);",
				paramData.pelvisSpeed, paramData.trunkSpeed, paramData.forearmSpeed, paramData.handSpeed,
				paramData.trunkForwardBendAddress, paramData.trunkForwardBendTop, paramData.trunkForwardBendImpact,
				paramData.trunkSideBendAddress, paramData.trunkSideBendTop, paramData.trunkSideBendImpact,
				paramData.trunkRotationAddress, paramData.trunkRotationTop, paramData.trunkRotationImpact,
				paramData.pelvisForwardBendAddress, paramData.pelvisForwardBendTop, paramData.pelvisForwardBendImpact,
				paramData.pelvisSideBendAddress, paramData.pelvisSideBendTop, paramData.pelvisSideBendImpact,
				paramData.pelvisRotationAddress, paramData.pelvisRotationTop, paramData.pelvisRotationImpact,
				paramData.TSPelvis, paramData.TSTrunk, paramData.TSForearm, paramData.TSHand, paramData.PSSPelvis,
				paramData.PSSTrunk, paramData.PSSForearm, paramData.PSSHand, paramData.backSwingTime,
				paramData.downSwingTime, paramData.tempo, paramData.addressS_Posture, paramData.addressC_Posture,
				paramData.topX_Factor, paramData.topReverseSpine, paramData.topFlatShoulder, paramData.impactHipTurn);
		Logs.log(Logs.RUN_LOG, strSQL);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			stmtRs = stmt.executeUpdate(strSQL);

			JSONObject jsonProject = new JSONObject();
			jarrProj.put(jsonProject);
			jsonResp.put("success", true);

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		return 0;
	}

	public int insertSpaceCapuleRawData(HttpServletRequest req) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProjects = new JSONArray();

		strSQL = "INSERT INTO raw_data.SpaceCapule () VALUES ();";
		return 0;
	}

	private void printParam(HttpServletRequest request) {
		String strRequest = " =========== Request Parameter ============";
		Enumeration<?> in = request.getParameterNames();
		while (in.hasMoreElements()) {
			String paramName = in.nextElement().toString();
			String pValue = request.getParameter(paramName);
			strRequest = strRequest + "\n" + paramName + " : " + pValue;
		}
		Logs.log(Logs.RUN_LOG, strRequest);
	}
}
