package com.golfmaster.moduel.analysis;

import com.golfmaster.moduel.DeviceData;

public abstract class AnalysisRunner extends DeviceData implements Runnable {
	protected int m_weight;
	protected WrgData m_wrgData;
	protected WrgExpert m_wrgExpert;

	public AnalysisRunner(DeviceData.WrgData wrgData) {
		m_weight = 1;
		m_wrgData = wrgData;
		m_wrgExpert = new WrgExpert();
	}

	public void setWeight(int nWeight) {
		m_weight = nWeight;
	}

	public int getWeight() {
		return m_weight;
	}

	public WrgExpert getWrgExpert() {
		return m_wrgExpert;
	}


}
