/*
 * 球桿推薦API
 * 參數: yardage
 * http://localhost/GolfMaster/service/club-recommendation.jsp?yardage=120
 * http://18.181.37.98/GolfMaster/service/club-recommendation.jsp?yardage=120
 * http://61.216.149.161/GolfMaster/service/club-recommendation.jsp?yardage=120
 * 
 */
package com.golfmaster.service;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.golfmaster.common.Logs;
import com.golfmaster.common.ApiResponse;

public class ClubRecommendation
{
	private class ParamData
	{
		private String yardage;
	}

	public String processRequest(HttpServletRequest request)
	{
		String strClub;
		JSONObject jsonResponse = null;

		printParam(request);
		ClubRecommendation.ParamData paramData = new ClubRecommendation.ParamData();
		jsonResponse = requestAndTrimParams(request, paramData);
		if (null != jsonResponse)
			return jsonResponse.toString();

		strClub = clubRecommend(paramData);
		jsonResponse = new JSONObject();
		jsonResponse.put("success", true);
		jsonResponse.put("club", strClub);
		Logs.log(Logs.RUN_LOG, "Response : " + jsonResponse.toString());
		return jsonResponse.toString();
	}

	private JSONObject requestAndTrimParams(HttpServletRequest request, ParamData paramData)
	{
		try
		{
			paramData.yardage = StringUtils.trimToEmpty(request.getParameter("yardage"));

			if (StringUtils.isEmpty(paramData.yardage))
			{
				return ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
			}
		}
		catch (Exception e)
		{
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			return ApiResponse.unknownError();
		}

		return null;
	}

	private String clubRecommend(ParamData paramData)
	{
		String strClub = "無法建議";
		int nNum;

		try
		{
			nNum = Integer.valueOf(paramData.yardage).intValue();
			if (220 <= nNum)
			{
				strClub = "建議使用1號木杆" + ",桿面仰角為11度";
			}
			if (190 <= nNum && nNum <= 220)
			{
				strClub = "建議使用1號鐵桿" + ",桿面仰角為16度";
			}
			if (180 <= nNum && nNum <= 210)
			{
				strClub = "建議使用2號鐵桿" + ",桿面仰角為20度";
			}
			if (170 <= nNum && nNum <= 200)
			{
				strClub = "建議使用3號鐵桿" + ",桿面仰角為24度";
			}
			if (160 <= nNum && nNum <= 190)
			{
				strClub = "建議使用4號鐵桿" + ",桿面仰角為28度";
			}
			if (150 <= nNum && nNum <= 180)
			{
				strClub = "建議使用5號鐵桿" + ",桿面仰角為32度";
			}
			if (140 <= nNum && nNum <= 170)
			{
				strClub = "建議使用6號鐵桿" + ",桿面仰角為36度";
			}
			if (130 <= nNum && nNum <= 160)
			{
				strClub = "建議使用7號鐵桿" + ",桿面仰角為40度";
			}
			if (120 <= nNum && nNum <= 150)
			{
				strClub = "建議使用8號鐵桿" + ",桿面仰角為44度";
			}
			if (110 <= nNum && nNum <= 140)
			{
				strClub = "建議使用9號鐵桿" + ",桿面仰角為48度";
			}
			if (50 <= nNum && nNum <= 110)
			{
				strClub = "建議使用S號鐵桿" + ",桿面仰角為56度";
			}
			if (nNum <= 50)
			{
				strClub = "建議使用大角度桿" + ",桿面仰角為60度";
			}
		}
		catch (Exception e)
		{
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		return strClub;
	}

	private void printParam(HttpServletRequest request)
	{
		String strRequest = " =========== Request Parameter ============";
		Enumeration<?> in = request.getParameterNames();
		while (in.hasMoreElements())
		{
			String paramName = in.nextElement().toString();
			String pValue = request.getParameter(paramName);
			strRequest = strRequest + "\n" + paramName + " : " + pValue;
		}
		Logs.log(Logs.RUN_LOG, strRequest);
	}
}
