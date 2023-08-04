package com.golfmaster.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;

public class RawDataReceive extends DeviceData {

	public JSONObject getJmexRawDataReq(HttpServletRequest req) {
		JSONObject jsobjParam = new JSONObject();
		JSONObject job = new JSONObject();
		try {
			printParam(req);
			String jmexID = req.getParameter("jmex");
			if (jmexID != null) {
				Long jID = Long.parseLong(jmexID);
				jsobjParam = this.JMEXshotData(jID, job);
				
				jsobjParam.put("result", true);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
		Logs.log(Logs.RUN_LOG, "Response : " + jsobjParam.toString());
		return jsobjParam;
	}
	
	public JSONObject getIRITRawDataReq(HttpServletRequest req) {
		JSONObject jsobjParam = new JSONObject();
		JSONObject job = new JSONObject();
		try {
			printParam(req);
			String IRITid = req.getParameter("IRIT");
			if (IRITid != null) {
				Long iritID = Long.parseLong(IRITid);
				jsobjParam = this.JMEXshotData(iritID, job);
				
				jsobjParam.put("result", true);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
		Logs.log(Logs.RUN_LOG, "Response : " + jsobjParam.toString());
		return jsobjParam;
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
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);
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
		return stmtRs;
	}

	public JSONObject JMEXshotData(Long jmexID, JSONObject jsonResp) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();
		JSONObject jsonProject = null;

		strSQL = String.format("SELECT backSwingTime,downSwingTime,tempo FROM raw_data.JMEX WHERE id=%d", jmexID);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				jsonProject = new JSONObject();
				jsonProject.put("backSwingTime", rs.getString("backSwingTime"));
				jsonProject.put("downSwingTime", rs.getString("downSwingTime"));
				jsonProject.put("tempo", rs.getString("tempo"));

				jarrProj.put(jsonProject);
			}
			jsonResp.put("success", true);

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		return jsonProject;
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

	public JSONObject IRITshotData(Long IRITid, JSONObject jsonResp) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();
		JSONObject jsonProject = null;
		strSQL = String.format("SELECT * FROM raw_data.IRIT WHERE id=%d", IRITid);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				jsonProject = new JSONObject();
				jsonProject.put("BallSpeed", rs.getString("BallSpeed"));
				jsonProject.put("BackSpin", rs.getString("BackSpin"));
				jsonProject.put("SideSpin", rs.getString("SideSpin"));
				jsonProject.put("LaunchAngle", rs.getString("LaunchAngle"));
				jsonProject.put("Angle", rs.getString("Angle"));

				jarrProj.put(jsonProject);
			}
			jsonResp.put("success", true);

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		return jsonProject;
	}

	public int insertQuerryIRIT(ITRIData paramData, JSONObject jsonResp) {
		int stmtRs = -1;

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();

		strSQL = String.format(
				"INSERT INTO raw_data.IRIT (BallSpeed,BackSpin,SideSpin,LaunchAngle,Angle) VALUES (%f,%f,%f,%f,%f);",
				paramData.BallSpeed, paramData.BackSpin, paramData.SideSpin, paramData.LaunchAngle, paramData.Angle);
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
