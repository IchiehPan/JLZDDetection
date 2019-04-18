package com.example.demo.conreoller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.DeviceInfo;

import com.example.demo.service.loginService;

//摄像头注册Login的类
@RestController
@RequestMapping("/hkversion")
public class LoginController {

	@Autowired
	private loginService loginService;

	private DeviceInfo deviceInfo = null;

	@RequestMapping("/login")
	@ResponseBody
	public String login(String username, String password, String deviceip) {
		
		deviceInfo = loginService.Login(deviceip, "8000", username, password);
		if (deviceInfo != null) {
			return "login success";
		}
		/*
		 * int is_start=guardService.startGuard(deviceInfo, deviceIp); if(is_start==0) {
		 * 
		 * return 0; //fail } previewservice.startPlay(deviceInfo, hcNetSDK);
		 * 
		 * regionPointService.GetFieldpoint(deviceInfo);
		 * 
		 * videoSaveService.getVideoByTime("1970-01-22 00:00:00","1970-01-23"+
		 * "23:59:59",deviceInfo,hcNetSDK,""); listeningService.startListen(m_sDeviceIP,
		 * deviceInfo, "8000");
		 * 
		 * previewservice.startPlay(deviceInfo,hcNetSDK);
		 * videoSaveService.timeQuit(deviceInfo,hcNetSDK);
		 * videoSaveService.findFileCount("1970-01-21 00:00:00","1970-01-23"+
		 * "23:59:59",deviceInfo,hcNetSDK); videoSaveService.findBycalend(deviceInfo,
		 * hcNetSDK); videoSaveService.findFileCount("1970-01-22 00:00:00","1970-01-22"+
		 * "23:59:59",deviceInfo,hcNetSDK);
		 */
		return "login fail";
	}

	@RequestMapping("/logout")
	@ResponseBody
	public String logout(String ip) {
		if (loginService.logout(ip)) {

			return "logout success";
		}
		return "logout fail";
	}
	@RequestMapping("/getOnlineDevice")
	@ResponseBody
	public Map<String, DeviceInfo> getOnlineDevice() {
		
		return DeviceInfo.getuserMap();
	}
	
	
	@RequestMapping("/checktime")
	@ResponseBody
	public int checktime(String ip) {
	
		if(loginService.checkTime(ip)) {
			
			return 1;
		}
			
		return 0;

		
		
	}
	
	
}
