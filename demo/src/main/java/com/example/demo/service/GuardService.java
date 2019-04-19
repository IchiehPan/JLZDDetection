package com.example.demo.service;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import com.example.demo.entity.AlarmThread;
import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;
import com.example.demo.entity.HCNetSDK.NET_DVR_ALARMER;
import com.example.demo.entity.HCNetSDK.RECV_ALARM;
import com.example.demo.utils.HttpClientUtil2;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import net.sf.json.JSONObject;

// deal callback
@Service
public class GuardService {

	private AlarmThread paAlarmThread;
	private FMSGCallBack fMSFCallBack;// alarm callback
	static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
	DeviceInfo deviceInfo;
	private NativeLong lAlarmHandle;

	public int startGuard(DeviceInfo deviceInfo, String ip) {

		this.deviceInfo = deviceInfo;

		NativeLong lUserID = deviceInfo.getM_nUserID();
		if (lUserID.intValue() == -1) {
			System.out.println("Please login");
			return 0;
		}

		if (deviceInfo.getM_lAlarmHandle().intValue() == -1) {

			fMSFCallBack = new FMSGCallBack();
			Pointer pUser = null;

			if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V30(fMSFCallBack, pUser)) {
				System.out.println("Set callBack fail,error code:" + hCNetSDK.NET_DVR_GetLastError());

				return 0;
			}
			paAlarmThread = AlarmThread.getInstance();

			if (!paAlarmThread.GetStart()) {
				paAlarmThread.start();
				paAlarmThread.SetStart(true);
			}
			lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V30(lUserID);
			deviceInfo.setM_lAlarmHandle(lAlarmHandle);

			if (lAlarmHandle.intValue() == -1) {
				System.out.println("Guarding fail,error code:" + hCNetSDK.NET_DVR_GetLastError());

				return 0;
			}

			return 1;
		} else {
			if (deviceInfo.getM_lAlarmHandle().intValue() != -1) {
				if (!hCNetSDK.NET_DVR_CloseAlarmChan_V30(deviceInfo.getM_lAlarmHandle())) {
					System.out.println("Disguarding fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
					deviceInfo.setM_lAlarmHandle(new NativeLong(-1));
					return 0;
				} else {
					deviceInfo.setM_lAlarmHandle(new NativeLong(-1));
				}
			}
		}

		return 1;
	}

	public class FMSGCallBack implements HCNetSDK.FMSGCallBack {
		// alarm info call back function

		@Override
		public void invoke(NativeLong lCommand, NET_DVR_ALARMER pAlarmer, RECV_ALARM pAlarmInfo, int dwBufLen,
				Pointer pUser) {
			// TODO Auto-generated method stub
			// guardAlarmService.init(deviceInfo);

			// LocalDateTime dateTime =LocalDateTime.now();
			// System.out.println("warning1====="+dateTime.getSecond()+"s");
			String sAlarmType = new String();
			String[] newRow = new String[3];
			// 报警时间

			Date today = new Date();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String[] sIP = new String[2];
			sAlarmType = new String("lCommand=") + lCommand.intValue();
			System.out.println(sAlarmType);
			// lCommand是传的报警类型
			switch (lCommand.intValue()) {

			case HCNetSDK.COMM_ALARM_RULE:
				newRow[0] = dateFormat.format(today);
				// 报警类型
				sAlarmType = sAlarmType + new String("ruqinbaojing");
				newRow[1] = sAlarmType;
				// 报警设备IP地址
				sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
				newRow[2] = sIP[0];
				HCNetSDK.NET_VCA_RULE_ALARM rAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
				rAlarm.read();

				String resultPostUrl = "http://192.168.8.211:8333/alert_handle/invasion_alert_api/";

				HttpClientUtil2 clientUtil2 = new HttpClientUtil2();
				Map<String, String> map = new HashMap<>();
				map.put("ip", "192." + sIP[0]);
				map.put("signal", "alarm");
				String rep = clientUtil2.doPost(resultPostUrl, map, "utf-8");

				JSONObject jsonobject = JSONObject.fromObject(rep);
				int status = (int) jsonobject.get("status");

				if (status == 200) {
					boolean isKey = hCNetSDK.NET_DVR_MakeKeyFrameSub(deviceInfo.getM_nUserID(),
							new NativeLong(deviceInfo.getM_lpDeviceInfo().byStartChan));
					System.out.println("isKey==========" + isKey);

				}

				// System.out.println(rep);
				break;

			case HCNetSDK.COMM_ALARM_V30:
				HCNetSDK.NET_DVR_ALARMINFO_V30 strAlarmInfoV30 = new HCNetSDK.NET_DVR_ALARMINFO_V30();
				strAlarmInfoV30.write();
				// Pointer pInfoV30 = strAlarmInfoV30.getPointer();
				// pInfoV30.write(0, pAlarmInfo.getByteArray(0, strAlarmInfoV30.size()), 0,
				// strAlarmInfoV30.size());
				strAlarmInfoV30.read();
				switch (strAlarmInfoV30.dwAlarmType) {
				case 0:
					sAlarmType = sAlarmType + new String("：信号量报警") + "，" + "报警输入口："
							+ (strAlarmInfoV30.dwAlarmInputNumber + 1);
					break;
				case 1:
					sAlarmType = sAlarmType + new String("：硬盘满");
					break;
				case 2:
					sAlarmType = sAlarmType + new String("：信号丢失");
					break;
				case 3:
					sAlarmType = sAlarmType + new String("：移动侦测") + "，" + "报警通道：";
					for (int i = 0; i < 64; i++) {
						if (strAlarmInfoV30.byChannel[i] == 1) {
							sAlarmType = sAlarmType + "ch" + (i + 1) + " ";
						}
					}
					break;
				case 4:
					sAlarmType = sAlarmType + new String("：硬盘未格式化");
					break;
				case 5:
					sAlarmType = sAlarmType + new String("：读写硬盘出错");
					break;
				case 6:
					sAlarmType = sAlarmType + new String("：遮挡报警");
					break;
				case 7:
					sAlarmType = sAlarmType + new String("：制式不匹配");
					break;
				case 8:
					sAlarmType = sAlarmType + new String("：非法访问");
					break;
				}
				newRow[0] = dateFormat.format(today);
				// 报警类型
				newRow[1] = sAlarmType;
				// 报警设备IP地址
				sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
				newRow[2] = sIP[0];
				// alarmTableModel.insertRow(0, newRow);
				System.out.println(sAlarmType);
				break;

			case HCNetSDK.COMM_UPLOAD_PLATE_RESULT:
				HCNetSDK.NET_DVR_PLATE_RESULT strPlateResult = new HCNetSDK.NET_DVR_PLATE_RESULT();
				strPlateResult.write();
				// Pointer pPlateInfo = strPlateResult.getPointer();
				// pPlateInfo.write(0, pAlarmInfo.getByteArray(0, strPlateResult.size()), 0,
				// strPlateResult.size());
				strPlateResult.read();
				try {
					String srt3 = new String(strPlateResult.struPlateInfo.sLicense, "GBK");
					sAlarmType = sAlarmType + "：交通抓拍上传，车牌：" + srt3;
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				newRow[0] = dateFormat.format(today);
				// 报警类型
				newRow[1] = sAlarmType;
				// 报警设备IP地址
				sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
				newRow[2] = sIP[0];
				break;
			case HCNetSDK.COMM_ITS_PLATE_RESULT:

				HCNetSDK.NET_ITS_PLATE_RESULT strItsPlateResult = new HCNetSDK.NET_ITS_PLATE_RESULT();
				strItsPlateResult.write();
				// Pointer pItsPlateInfo = strItsPlateResult.getPointer();
				// pItsPlateInfo.write(0, pAlarmInfo.getByteArray(0, strItsPlateResult.size()),
				// 0, strItsPlateResult.size());
				strItsPlateResult.read();

				String srt3;
				try {
					srt3 = new String(strItsPlateResult.struPlateInfo.sLicense, "GBK").trim().substring(1);
					System.out.println("车牌号:" + srt3);

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				break;

			default:
				newRow[0] = dateFormat.format(today);
				// 报警类型
				newRow[1] = sAlarmType;
				// 报警设备IP地址
				sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
				newRow[2] = sIP[0];
				// alarmTableModel.insertRow(0, newRow);
				System.out.println("未找到匹配模式");
				break;
			}

		}
	}

	public int zhuaTu() {

		HCNetSDK.NET_DVR_SNAPCFG struSnapCfg = new HCNetSDK.NET_DVR_SNAPCFG();
		struSnapCfg.dwSize = struSnapCfg.size();
		struSnapCfg.bySnapTimes = 1;
		struSnapCfg.wSnapWaitTime = 1000;
		struSnapCfg.write();
		if (false == hCNetSDK.NET_DVR_ContinuousShoot(this.deviceInfo.getM_nUserID(), struSnapCfg)) {
			int iErr = hCNetSDK.NET_DVR_GetLastError();
			System.out.println("网络触发失败，错误号：" + iErr);
			return 0;
		} else {
			System.out.println("抓图成功！");
			return 1;
		}

	}

	public int Tu(String pictureName) {

		HCNetSDK.NET_DVR_JPEGPARA jpegpara = new HCNetSDK.NET_DVR_JPEGPARA();
		jpegpara.wPicSize = 2;
		jpegpara.wPicQuality = 1;

		NativeLong ichane = new NativeLong(1);

		if (hCNetSDK.NET_DVR_CaptureJPEGPicture(deviceInfo.getM_nUserID(), ichane, jpegpara, pictureName)) {

			System.out.println("suc");
		} else {
			int iErr = hCNetSDK.NET_DVR_GetLastError();
			System.out.println("realplay err" + iErr);
		}

		return 1;
	}

	public int stopGuard(NativeLong lAlarmHandle) {
		// 报警撤防
		if (lAlarmHandle.intValue() > -1) {
			if (!hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle)) {
				System.out.println("撤防失败");
				return 1;
			} else {
				lAlarmHandle = new NativeLong(-1);
			    
				System.out.println("撤防成功");
			}
		}
		return 0;

	}

}
