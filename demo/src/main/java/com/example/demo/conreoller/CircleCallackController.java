package com.example.demo.conreoller;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;

import com.example.demo.entity.DeviceInfo;

import com.example.demo.service.MyHandler;
import com.example.demo.service.loginService;

import net.sf.json.JSONObject;

@RestController
public class CircleCallackController {

	@Autowired
	private loginService loginService;

	/*
	 * @Autowired private HkActivateDeviceService kActivateDeviceService;
	 */

	@RequestMapping(value = "/PictureCircle", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public void PictureCircle(HttpServletRequest request, HttpServletResponse response) {

		// String data = request.getParameter("data");
		@SuppressWarnings("unused")
		String ip = null;
		JSONObject jsonParam = this.getJSONParam(request);
		if (jsonParam != null) {
			//LocalDateTime dateTime = LocalDateTime.now();
			//System.out.println("warning===" + dateTime.getSecond() + "s");
			JSONObject dataParam = JSONObject.fromObject(jsonParam);
			JSONObject ipParam = JSONObject.fromObject(dataParam.get("data"));
			// System.out.println(dataParam.get("data"));
			// System.out.println(ipParam.get("bbox"));
			ip = (String) ipParam.get("ip");
			if (ipParam.get("bbox") != null) {

				/*System.out.println(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() + "-------------");
				System.out.println(ipParam.get("timestamp") + "---------------------");
				System.out.println(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli()
						- (long) ipParam.get("timestamp"));*/
				MyHandler.sendMessageToUser(ipParam.get("ip") + "", new TextMessage(dataParam.get("data") + ""));

				// LocalDateTime dateTime2 =LocalDateTime.now();
				// System.out.println("warning2====="+dateTime2.getSecond()+"s");

				/*
				 * if(map.get("bbox").toString()==null) { map.put("bbox",
				 * (String)ipParam.get("bbox")); }else { MyHandler.sendMessageToUser(ip+"", new
				 * TextMessage("1")); }
				 */

			}
		}

	}

	public JSONObject getJSONParam(HttpServletRequest request) {
		JSONObject jsonParam = null;
		try {
			// 获取输入流
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

			// 写入数据到Stringbuilder
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = streamReader.readLine()) != null) {
				sb.append(line);
			}

			jsonParam = JSONObject.fromObject(sb.toString());
			// 直接将json信息打印出来

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return jsonParam;
	}

	@RequestMapping("/checktime")
	public int checktime(String ip) {
		if (loginService.checkTime(ip)) {
			return 1;
		}
		;
		return 0;

	}

	@RequestMapping("/getDeviceInfo")
	public DeviceInfo getDeviceInfo(String newIp, String Password) {
		// HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
		DeviceInfo deviceInfo = loginService.Login("192.168.8.177", "8000", "admin", "jhd123456");
		// kActivateDeviceService.ActivateDevice(newIp,"jhd123456");
		// kActivateDeviceService.setDeviceIp(deviceInfo,"192.168.8.66");
		return deviceInfo;
	}

	/*
	 * @RequestMapping("/login") public int login(String username,String
	 * password,String deviceIp) { HCNetSDK hcNetSDK =HCNetSDK.INSTANCE;
	 * 
	 * DeviceInfo deviceInfo = loginService.Login(deviceIp, "8000", username,
	 * password, hcNetSDK); NativeLong ichan =new
	 * NativeLong(deviceInfo.GetlpDeviceInfo().byStartChan);
	 * if(deviceInfo.GetNUserID().intValue()!=0) { return 1; } return 0; }
	 */
}
