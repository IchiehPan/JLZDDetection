package com.example.demo.conreoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.DeviceInfo;
import com.example.demo.service.HkActivateDeviceService;

@RestController
public class ActivateDeviceInfo {

	@Autowired
	private HkActivateDeviceService hkActivateDeviceService;

	@PostMapping(value ="/activatedevice")
	@ResponseBody
	public int InitDevice(String newIp, String newPassword, String newGatewayIp) {

		if (!hkActivateDeviceService.ActivateDevice(newIp, newPassword, newGatewayIp)) {

			return 0;
		}
		
		return 1;

	}

	@GetMapping(value ="/getdeviceinfo")
	
	public DeviceInfo getDeviceInfo(String ip) {
                
		DeviceInfo deviceInfo  = DeviceInfo.getuserMap().get(ip);
		if(deviceInfo == null) {
			return null;
		}
		if(hkActivateDeviceService.getDeviceInfo(deviceInfo)) {
			return DeviceInfo.getuserMap().get(ip);
			
		}
		return null;

	}
	
	
}
