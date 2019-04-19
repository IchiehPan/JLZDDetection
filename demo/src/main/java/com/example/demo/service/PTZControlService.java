package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;
import com.sun.jna.NativeLong;

@Service
public class PTZControlService {

	HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
	
	
	
	
	//Speed :[1,7]
	public boolean StarPTZ(String ip, String Speed,@RequestParam(defaultValue="21") String iPTZCommand) {

		DeviceInfo deviceInfo = DeviceInfo.getuserMap().get(ip);
		if (deviceInfo != null) {

			if (!hcNetSDK.NET_DVR_PTZControlWithSpeed_Other(deviceInfo.getM_nUserID(),
					new NativeLong(deviceInfo.getM_lpDeviceInfo().byStartChan), Integer.parseInt(iPTZCommand), 0,
					Integer.parseInt(Speed))) {
				System.out.println(hcNetSDK.NET_DVR_GetLastError());
				return false;

			}

			return true;
		}

		return false;
	}

	public boolean StopPTZ(String ip, String Speed,@RequestParam(defaultValue="21") String iPTZCommand) {

		DeviceInfo deviceInfo = DeviceInfo.getuserMap().get(ip);
		if (deviceInfo != null) {

			if (!hcNetSDK.NET_DVR_PTZControlWithSpeed_Other(deviceInfo.getM_nUserID(),
					new NativeLong(deviceInfo.getM_lpDeviceInfo().byStartChan), HCNetSDK.TILT_UP, 1,
					Integer.parseInt(Speed))) {
				System.out.println(hcNetSDK.NET_DVR_GetLastError());
				return false;

			}

			return true;
		}

		return false;
	}
	
	
	

}
