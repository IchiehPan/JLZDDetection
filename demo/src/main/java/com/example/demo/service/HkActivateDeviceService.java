package com.example.demo.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;
import com.sun.jna.NativeLong;

import com.sun.jna.ptr.IntByReference;

@Service
public class HkActivateDeviceService {

	private HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
	private String Nowip = "192.168.1.64";

	@Autowired
	private loginService loginService;

	public Boolean ActivateDevice(String newIp, String newPassword, String newGatewayIp) {
		hcNetSDK.NET_DVR_Init();
		hcNetSDK.NET_DVR_SetLogToFile(true, null, false);
		hcNetSDK.NET_DVR_SetConnectTime(2000, 1);
		hcNetSDK.NET_DVR_SetReconnect(10000, true);

		HCNetSDK.NET_DVR_ACTIVATECFG ipActivateCfg = new HCNetSDK.NET_DVR_ACTIVATECFG();
		ipActivateCfg.dwSize = ipActivateCfg.size();
		ipActivateCfg.sPassword = newPassword.getBytes();
		ipActivateCfg.write();
		if (!hcNetSDK.NET_DVR_ActivateDevice(Nowip, (short) 8000, ipActivateCfg)) {
			System.out.println("error" + hcNetSDK.NET_DVR_GetLastError());
			return false;
		}
		DeviceInfo deviceInfo = loginService.Login(Nowip, "8000", "admin", newPassword);
		if (deviceInfo == null) {
			return false;
		}
		if (!setDeviceIp(deviceInfo, newIp, newGatewayIp)) {
			return false;
		}
		loginService.removeHasLogin(Nowip);
		hcNetSDK.NET_DVR_Cleanup();
		return true;
	}

	public boolean getDeviceInfo(DeviceInfo deviceInfo) {
		HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfg = new HCNetSDK.NET_DVR_DEVICECFG_V40();
		devicecfg.dwSize = devicecfg.size();
		IntByReference intByReference = new IntByReference(2048);
		if (hcNetSDK.NET_DVR_GetDVRConfig(deviceInfo.getM_nUserID(), HCNetSDK.NET_DVR_GET_DEVICECFG_V40,
				new NativeLong(deviceInfo.getM_lpDeviceInfo().byStartChan), devicecfg.getPointer(), devicecfg.size(),
				intByReference)) {
			devicecfg.read();
			deviceInfo.setBsbyDevTypeName(new String(devicecfg.bsbyDevTypeName));
			deviceInfo.setDeviceName(new String(devicecfg.sDVRName));
			deviceInfo.setsSerialNumber(new String(devicecfg.sSerialNumber));
			Map<String, DeviceInfo> usrs = DeviceInfo.getuserMap();
			usrs.put(Nowip, deviceInfo);
			return true;
			
		}

		return false;
	}

	public boolean setDeviceIp(DeviceInfo deviceInfo, String newIp, String newGatewayIp) {
		HCNetSDK.NET_DVR_NETCFG_V30 netcfg_V30 = new HCNetSDK.NET_DVR_NETCFG_V30();
		IntByReference intByReference = new IntByReference(2048);
		if (hcNetSDK.NET_DVR_GetDVRConfig(deviceInfo.getM_nUserID(), HCNetSDK.NET_DVR_GET_NETCFG_V30,
				new NativeLong(deviceInfo.getM_lpDeviceInfo().byStartChan), netcfg_V30.getPointer(), netcfg_V30.size(),
				intByReference)) {
			netcfg_V30.read();
			/*
			 * System.out.println(new String(netcfg_V30.struDnsServer1IpAddr.sIpV4));//
			 * 8.8.8.8 System.out.println(new
			 * String(netcfg_V30.struEtherNet[0].struDVRIP.sIpV4));// 192.168.8.177
			 * System.out.println(new String(netcfg_V30.struMulticastIpAddr.sIpV4));//
			 * 0.0.0.0 System.out.println(new String(netcfg_V30.struGatewayIpAddr.sIpV4));//
			 * 192.168.8.1
			 */
		}

		netcfg_V30.struEtherNet[0].struDVRIP.sIpV4 = newIp.getBytes();
		netcfg_V30.struGatewayIpAddr.sIpV4 = newGatewayIp.getBytes();
		netcfg_V30.write();
		if (!hcNetSDK.NET_DVR_SetDVRConfig(deviceInfo.getM_nUserID(), HCNetSDK.NET_DVR_SET_NETCFG_V30,
				new NativeLong(deviceInfo.getM_lpDeviceInfo().byStartChan), netcfg_V30.getPointer(), netcfg_V30.size())) {
			return false;
		}
		if (!hcNetSDK.NET_DVR_RebootDVR(deviceInfo.getM_nUserID())) {
			return false;
		}
		return true;

	}

}
