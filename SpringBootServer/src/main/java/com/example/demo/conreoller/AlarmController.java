package com.example.demo.conreoller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.AlarmPoint;
import com.example.demo.entity.DeviceInfo;
import com.example.demo.service.GuardService;
import com.example.demo.service.RegionPointService;
import com.example.demo.service.loginService;

@RestController
public class AlarmController {

	@Autowired
	RegionPointService regionPointService;

	@Autowired
	private GuardService guardService;
	
	@Autowired
	private loginService loginService;

	@PostMapping(value = "/setRegionPonit")
	public String setRegionPoint(String ip, String x1, String y1, String x2, String y2,String x3, String y3,String x4, String y4 ,
			String username,String password,String no) {

		 //System.out.println(x1+" x1 "+y1+" y1 "+x2+" x2 "+y2+" y2 "+x3+" x3 "+y3+" y3 "+x4+" x4 "+y4+" y4 ");
		
		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo deviceInfo = map.get(ip);
		if (deviceInfo != null) {
			boolean SetRegion = regionPointService.SetFieldpoint(deviceInfo, x1, y1, x2, y2, x3, y3, x4, y4,no);
			if (SetRegion) {
				return "success";
			}
			return "set regionPoint fail";

		} else {
			deviceInfo = loginService.Login(ip, "8000", username, password);
			boolean SetRegion = regionPointService.SetFieldpoint(deviceInfo, x1, y1, x2, y2, x3, y3, x4, y4,no);
			if (SetRegion) {
				return "success";
			}
			return "set regionPoint fail";
		}

	}

	@GetMapping(value = "/getRegionPonit")
	public List<AlarmPoint> getRegionPoint(String ip,String username,String password,String no) {

		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo deviceInfo = map.get(ip);
		if (deviceInfo != null) {
			List<AlarmPoint> alarmPoints = regionPointService.GetFieldpoint(deviceInfo,Integer.parseInt(no));
			if (alarmPoints != null) {
				return alarmPoints;
			}
			return null;

		} else {
			deviceInfo = loginService.Login(ip, "8000", username, password);
			List<AlarmPoint> alarmPoints = regionPointService.GetFieldpoint(deviceInfo,Integer.parseInt(no));
			if (alarmPoints != null) {
				return alarmPoints;
			}
			return null;
		}

	}

	@GetMapping(value = "/getTraversPonit")
	public List<AlarmPoint> getTraversPoint(String ip,String username,String password,String no) {

		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo deviceInfo = map.get(ip);
		if (deviceInfo != null) {
			List<AlarmPoint> alarmPoints = regionPointService.GetTraversPont(deviceInfo,Integer.parseInt(no));
			if (alarmPoints != null) {
				return alarmPoints;
			}
			return null;

		} else {
			deviceInfo = loginService.Login(ip, "8000", username, password);
			List<AlarmPoint> alarmPoints = regionPointService.GetTraversPont(deviceInfo,Integer.parseInt(no));
			if (alarmPoints != null) {
				return alarmPoints;
			}
			return null;
		}

	}

	@PostMapping(value = "/setTraversPonit")
	public String setTraversPonit(String ip, String x1, String y1, String x2, String y2, 
			String type,String username,String password,String no) {

		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo deviceInfo = map.get(ip);

		if (deviceInfo != null) {
			boolean SetRegion = regionPointService.SetTraversPoint(deviceInfo, x1, y1, x2, y2, Integer.parseInt(type),Integer.parseInt(no));
			if (SetRegion) {
				return "success";
			}
			return "set regionPoint fail";

		} else {
			deviceInfo = loginService.Login(ip, "8000", username, password);
			boolean SetRegion = regionPointService.SetTraversPoint(deviceInfo, x1, y1, x2, y2, Integer.parseInt(type),Integer.parseInt(no));
			if (SetRegion) {
				return "success";
			}
			return "set regionPoint fail";
		}

	}

	@GetMapping(value = "/startGuard")
	public boolean startGuard(String ip) {

		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo deviceInfo = map.get(ip);
		if (deviceInfo != null) {
			int isStart = guardService.startGuard(deviceInfo, ip);
			if (isStart == 1) {
				return true;
			}

		} else {

			return false;
		}

		return false;

	}

	@GetMapping(value = "/stopGuard")
	public boolean stoptGuard(String ip) {

		Map<String, DeviceInfo> map = DeviceInfo.getuserMap();
		DeviceInfo deviceInfo = map.get(ip);
		if (deviceInfo != null) {
			
			int isStop = guardService.stopGuard(deviceInfo.getM_lAlarmHandle());
		
			if (isStop == 0) 
				return true;
			

		} else {

			return false;
		}

		return false;
	}

}
