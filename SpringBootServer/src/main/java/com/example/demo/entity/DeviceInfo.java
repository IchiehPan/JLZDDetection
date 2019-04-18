package com.example.demo.entity;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.NativeLong;

public class DeviceInfo {
	private String m_sDVRIP;
	private short m_wDVRPort;
	private String m_sUserName;
	private String m_sPassword;
	private NativeLong m_nUserID;// login handle
	private String deviceName;
	HCNetSDK.NET_DVR_DEVICEINFO_V30 m_lpDeviceInfo;
	private static final Map<String, DeviceInfo> users = new HashMap<>(); // login user list
	NativeLong m_lAlarmHandle; // guarding handle
	NativeLong m_lListenHandle; // listening handle
	private String bsbyDevTypeName;
    private String sSerialNumber; //序列号
	
	
	
	public String getsSerialNumber() {
		return sSerialNumber;
	}

	public void setsSerialNumber(String sSerialNumber) {
		this.sSerialNumber = sSerialNumber;
	}

	public static Map<String, DeviceInfo> getuserMap() {
		return users;
	}

	public DeviceInfo(String sDVRIP, short wDVRPort, String sUserName, String sPassword,
			HCNetSDK.NET_DVR_DEVICEINFO_V30 lpDeviceInfo, NativeLong nUserID) {
		m_sDVRIP = sDVRIP;
		m_wDVRPort = wDVRPort;
		m_sUserName = sUserName;
		m_sPassword = sPassword;
		m_nUserID = nUserID;
		m_lpDeviceInfo = lpDeviceInfo;
		m_lAlarmHandle = new NativeLong(-1);
		m_lListenHandle = new NativeLong(-1);

	}

	public String getM_sDVRIP() {
		return m_sDVRIP;
	}

	public void setM_sDVRIP(String m_sDVRIP) {
		this.m_sDVRIP = m_sDVRIP;
	}

	public short getM_wDVRPort() {
		return m_wDVRPort;
	}

	public void setM_wDVRPort(short m_wDVRPort) {
		this.m_wDVRPort = m_wDVRPort;
	}

	public String getM_sUserName() {
		return m_sUserName;
	}

	public void setM_sUserName(String m_sUserName) {
		this.m_sUserName = m_sUserName;
	}

	public String getM_sPassword() {
		return m_sPassword;
	}

	public void setM_sPassword(String m_sPassword) {
		this.m_sPassword = m_sPassword;
	}

	public NativeLong getM_nUserID() {
		return m_nUserID;
	}

	public void setM_nUserID(NativeLong m_nUserID) {
		this.m_nUserID = m_nUserID;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public HCNetSDK.NET_DVR_DEVICEINFO_V30 getM_lpDeviceInfo() {
		return m_lpDeviceInfo;
	}

	public void setM_lpDeviceInfo(HCNetSDK.NET_DVR_DEVICEINFO_V30 m_lpDeviceInfo) {
		this.m_lpDeviceInfo = m_lpDeviceInfo;
	}

	public NativeLong getM_lAlarmHandle() {
		return m_lAlarmHandle;
	}

	public void setM_lAlarmHandle(NativeLong m_lAlarmHandle) {
		this.m_lAlarmHandle = m_lAlarmHandle;
	}

	public NativeLong getM_lListenHandle() {
		return m_lListenHandle;
	}

	public void setM_lListenHandle(NativeLong m_lListenHandle) {
		this.m_lListenHandle = m_lListenHandle;
	}

	public String getBsbyDevTypeName() {
		return bsbyDevTypeName;
	}

	public void setBsbyDevTypeName(String bsbyDevTypeName) {
		this.bsbyDevTypeName = bsbyDevTypeName;
	}

	public static Map<String, DeviceInfo> getUsers() {
		return users;
	}


	
	
}
