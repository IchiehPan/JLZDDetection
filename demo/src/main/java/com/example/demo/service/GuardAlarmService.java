package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;
import com.example.demo.utils.HttpClientUtil2;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import net.sf.json.JSONObject;

@Service
public class GuardAlarmService {
	private static final Map<String, String> Base64Picture = new HashMap<>();
	private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
	private static DeviceInfo deviceInfo;
	HttpClientUtil2 client = new HttpClientUtil2();;
	private Map<String, String> map = new HashMap<>();
	private String ip;
	private String serialnumber;

	public void init(DeviceInfo deviceInfo) {
		GuardAlarmService.deviceInfo = deviceInfo;
		System.out.println("GuardAlarmService-----init");
		// SavePictureToByte();

	}

	@Async
	public void Start(String serialnumber, String ip) {
		SavePictureToByte();
		this.ip = ip;
		this.serialnumber = serialnumber;
		String resultPostUrl = "http://192.168.8.212:8333/alert_handle/invasion_alert/";
		// map.put("macaddress", "");
		map.put("ip", ip);
		map.put("serialnumber", serialnumber);
		map.put("snapshot_base64", Base64Picture.get("NowBase64Picture"));

		String result = client.doPost(resultPostUrl, map, "utf-8");
		JSONObject jsonobject = JSONObject.fromObject(result);
		int status = (int) jsonobject.get("status");

		if (status == 200) {
			JSONObject data = JSONObject.fromObject(jsonobject.get("data"));

			Boolean issend = MyHandler.sendMessageToUser(ip, new TextMessage(data.toString()));

		}

	}

	@Async
	public static void SavePictureToByte() {

		HCNetSDK.NET_DVR_JPEGPARA jpegpara = new HCNetSDK.NET_DVR_JPEGPARA();
		jpegpara.wPicSize = 2;
		jpegpara.wPicQuality = 0;
		jpegpara.read();
		byte[] bytes = null;
		int dwPicSize = 704 * 576;
		NativeLong ichane = new NativeLong(deviceInfo.getM_lpDeviceInfo().byStartChan);
		IntByReference lpSizeReturned = new IntByReference();
		lpSizeReturned.setValue(0);
		Pointer p = new Memory(704 * 576);
		if (hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(deviceInfo.getM_nUserID(), ichane, jpegpara, p, dwPicSize,
				lpSizeReturned)) {
			bytes = p.getByteArray(0, lpSizeReturned.getValue());
			System.out.println("succ");
		} else {
			int iErr = hCNetSDK.NET_DVR_GetLastError();
			System.out.println("realplay err" + iErr);
		}

		if (Base64Picture.get("NowBase64Picture") == null) {
			Base64Picture.put("NowBase64Picture", byte2Base64StringFun(bytes));
			Base64Picture.put("newBase64Picture", byte2Base64StringFun(bytes));

		} else {
			Base64Picture.put("NowBase64Picture", Base64Picture.get("newBase64Picture"));
			Base64Picture.put("newBase64Picture", byte2Base64StringFun(bytes));

		}

	}

	public static String getBase64Picture() {

		SavePictureToByte();
		return Base64Picture.get("NowBase64Picture");

	}

	public static String byte2Base64StringFun(byte[] b) {
		Base64 base64 = new Base64();
		return base64.encodeToString(b);
	}
}
