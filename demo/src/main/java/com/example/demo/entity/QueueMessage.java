package com.example.demo.entity;

import com.sun.jna.NativeLong;


public class QueueMessage {

	private HCNetSDK.RECV_ALARM m_palarmInfo;
	private NativeLong m_command;
	private HCNetSDK.NET_DVR_ALARMER m_pAlarmer;
	
	/**
	 * @param palarmInfo
	 * @param command
	 * @param pAlarmer
	 */
	public QueueMessage(HCNetSDK.RECV_ALARM palarmInfo,NativeLong command,HCNetSDK.NET_DVR_ALARMER pAlarmer)
	{
		m_palarmInfo = palarmInfo;
		m_command = command;
		m_pAlarmer = pAlarmer;
	}

	public void setM_palarmInfo(HCNetSDK.RECV_ALARM m_palarmInfo) {
		this.m_palarmInfo = m_palarmInfo;
	}

	public void setM_command(NativeLong m_command) {
		this.m_command = m_command;
	}

	public HCNetSDK.RECV_ALARM getM_palarmInfo() {
		return m_palarmInfo;
	}

	public NativeLong getM_command() {
		return m_command;
	}

	public HCNetSDK.NET_DVR_ALARMER getM_pAlarmer() {
		return m_pAlarmer;
	}

	public void setM_pAlarmer(HCNetSDK.NET_DVR_ALARMER m_pAlarmer) {
		this.m_pAlarmer = m_pAlarmer;
	} 
	
}
