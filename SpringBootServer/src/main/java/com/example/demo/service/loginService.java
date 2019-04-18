package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

@Service
public class loginService {

	private NativeLong nlUserID;
	private HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;

	public DeviceInfo Login(String sDeviceIP, String iport, String username, String password) {

		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo isLogin = map.get(sDeviceIP);
		if (isLogin == null) {
			hcNetSDK.NET_DVR_Init();
			hcNetSDK.NET_DVR_SetLogToFile(true, null, false);
			hcNetSDK.NET_DVR_SetConnectTime(2000, 1);
			hcNetSDK.NET_DVR_SetReconnect(10000, true);
			HCNetSDK.NET_DVR_USER_LOGIN_INFO struLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
			HCNetSDK.NET_DVR_DEVICEINFO_V40 struDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

			Pointer PointerstruDeviceInfoV40 = struDeviceInfo.getPointer();
			Pointer PointerstruLoginInfo = struLoginInfo.getPointer();

			for (int i = 0; i < sDeviceIP.length(); i++) {
				struLoginInfo.sDeviceAddress[i] = (byte) sDeviceIP.charAt(i);
			}

			struLoginInfo.sPassword = password.getBytes();

			struLoginInfo.sUserName = username.getBytes();

			struLoginInfo.wPort = Short.parseShort(iport);
			struLoginInfo.write();

			DeviceInfo deviceInfo = null;

			nlUserID = hcNetSDK.NET_DVR_Login_V40(PointerstruLoginInfo, PointerstruDeviceInfoV40);

			if (nlUserID.longValue() == -1) {
				System.out.println("login error,error code：" + hcNetSDK.NET_DVR_GetLastError());
				return null;
			} else {
				System.out.println("login success");
				struDeviceInfo.read();
				deviceInfo = new DeviceInfo(sDeviceIP, Short.parseShort(iport), username, password,
						struDeviceInfo.struDeviceV30, nlUserID);
				Map<String, DeviceInfo> usrs = DeviceInfo.getuserMap();
				usrs.put(sDeviceIP, deviceInfo);
				return deviceInfo;
			}
		}
		return isLogin;
	}

	public void removeHasLogin(String ip) {
		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo isLogin = map.get(ip);
		if(isLogin!=null) {
			map.remove(ip);
		}
	}
	
	
	
	public boolean logout(String ip) {
		// 注销
		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo isLogin = map.get(ip);
		if(isLogin!=null) {
			if (hcNetSDK.NET_DVR_Logout(isLogin.getM_nUserID())) {
				System.out.println("注销成功");
				map.remove(ip);
				return true;
			}
		}
		return false;
			
		

	}

	public boolean checkTime(String ip) {
		HCNetSDK.NET_DVR_TIME strCurTime = new HCNetSDK.NET_DVR_TIME();
		LocalDateTime localDateTime = LocalDateTime.now();
		strCurTime.dwYear = (localDateTime.getYear());
		strCurTime.dwMonth = (localDateTime.getMonthValue());
		strCurTime.dwDay = (localDateTime.getDayOfMonth());
		strCurTime.dwHour = (localDateTime.getHour());
		strCurTime.dwMinute = (localDateTime.getMinute());
		strCurTime.dwSecond = (localDateTime.getSecond());
		strCurTime.write();
		Pointer lpPicConfig = strCurTime.getPointer();
		DeviceInfo deviceInfo = DeviceInfo.getuserMap().get(ip);
        if(deviceInfo==null) {
        	return false;
        }
		boolean setDVRConfigSuc = hcNetSDK.NET_DVR_SetDVRConfig(deviceInfo.getM_nUserID(), HCNetSDK.NET_DVR_SET_TIMECFG,
				new NativeLong(0), lpPicConfig, strCurTime.size());
		strCurTime.read();System.out.println(setDVRConfigSuc);
		return setDVRConfigSuc;
		
	}

}
