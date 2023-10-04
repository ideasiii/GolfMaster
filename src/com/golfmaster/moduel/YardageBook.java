package com.golfmaster.moduel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.golfmaster.common.DBUtil;

public class YardageBook {
	
	/*
	 * 模組參數
	 */
	public class ParamData {
		
		private String player;
		private String startDate;
		private String endDate;
		private double dMax;
		private double dMin;
		
		public ParamData(String player, String startDate, String endDate, double dMin, double dMax) {
			this.player = player;
			this.startDate = startDate;
			this.endDate = endDate;
			this.dMin = dMin;
			this.dMax = dMax;
		}
		
		
		public String getPlayer() {
			return player;
		}

		public String getStartDate() {
			return startDate;
		}

		public String getEndDate() {
			return endDate;
		}

		public double getdMax() {
			return dMax;
		}

		public double getdMin() {
			return dMin;
		}
	}
	
	/*
	 * 碼數表數據資料結構
	 */
	public class YardageData {
		
		private String clubType;
		private double carryDistFtMax;
		private double carryDistFtMin;
		private double carryDistFtAvg;
		
		
		public String getClubType() {
			return clubType;
		}
		
		public void setClubType(String clubType) {
			this.clubType = clubType;
		}
		
		public double getCarryDistFtMax() {
			return carryDistFtMax;
		}
		
		public void setCarryDistFtMax(double carryDistFtMax) {
			this.carryDistFtMax = carryDistFtMax;
		}
		
		public double getCarryDistFtMin() {
			return carryDistFtMin;
		}
		
		public void setCarryDistFtMin(double carryDistFtMin) {
			this.carryDistFtMin = carryDistFtMin;
		}
		
		public double getCarryDistFtAvg() {
			return carryDistFtAvg;
		}
		
		public void setCarryDistFtAvg(double carryDistFtAvg) {
			this.carryDistFtAvg = carryDistFtAvg;
		}
	}
	
	
	public List<YardageData> getYardageBook(ParamData paramData) throws Exception {
		try {
			List<YardageData> list = this.queryData(this.getQuerySql(paramData));
			for(YardageData yardageData : list) {
				yardageData.setCarryDistFtAvg(this.doubleValueScale((yardageData.getCarryDistFtAvg() / 3.0)));
				yardageData.setCarryDistFtMax(this.doubleValueScale((yardageData.getCarryDistFtMax() / 3.0)));
				yardageData.setCarryDistFtMin(this.doubleValueScale((yardageData.getCarryDistFtMin() / 3.0)));
			}
			
			return list;
		}catch(Exception e) {
			throw e;
		}
	}
	
	private List<YardageData> queryData(String sql) throws Exception{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql);
			
			List<YardageData> list = new ArrayList<YardageData>();
			while (rs.next()) {
				YardageData yardageData = new YardageData();
				yardageData.setClubType(rs.getString("ClubType"));
				yardageData.setCarryDistFtAvg(rs.getDouble("avg"));
				yardageData.setCarryDistFtMax(rs.getDouble("max"));
				yardageData.setCarryDistFtMin(rs.getDouble("mix"));
				
				list.add(yardageData);
			}
			
			return list;
		}catch(Exception e) {
			throw e;
		}finally {
			DBUtil.close(rs, stmt, conn);
		}
	}
	
	private String getQuerySql(ParamData paramData) throws Exception{
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("select ClubType, min(CarryDistFt) as 'mix', round(avg(CarryDistFt),4) as 'avg', max(CarryDistFt) as 'max' from golf_master.shot_data");
			sql.append(" where player = '" + paramData.getPlayer() + "' ");
			
			/*
			if(paramData.getdMin() > 0 && paramData.getdMax() > 0) {
				sql.append(" and (CarryDistFt > " + (paramData.getdMin() * 3) +" and CarryDistFt < " + (paramData.getdMax() * 3) + ") ");
			}
			*/
			
			if(paramData.getdMin() > 0) {
				sql.append(" and (CarryDistFt >= " + paramData.getdMin() + ") ");
			}
			if((paramData.getStartDate() != null && !paramData.getStartDate().isEmpty()) &&(paramData.getEndDate() != null && !paramData.getEndDate().isEmpty())) {
				sql.append(" and (Date >= '" + paramData.getStartDate() + "' and Date <= '" + paramData.getEndDate() + "') " );
			}
			
			sql.append(" group by ClubType order by 1 ");
			
			return sql.toString();
		}catch(Exception e) {
			throw e;
		}
	}
	
	private double doubleValueScale(double value) {
		BigDecimal bigDecimal = new BigDecimal(value);
		value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		return value;
	}
}
