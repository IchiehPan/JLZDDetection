package com.example.demo.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import com.example.demo.entity.HCNetSDK.NET_DVR_PDC_ALRAM_INFO;
import com.example.demo.entity.HCNetSDK.NET_DVR_PLATE_RESULT;
import com.example.demo.entity.HCNetSDK.struAlarmChannel;
import com.example.demo.entity.HCNetSDK.struAlarmHardDisk;
import com.example.demo.entity.HCNetSDK.struIOAlarm;
import com.example.demo.entity.HCNetSDK.struStartTime;
import com.example.demo.entity.HCNetSDK.struStatFrame;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class AlarmThread extends Thread {

	private AlarmThread() {

	}

	private static AlarmThread m_alarmThread = null;
	private static boolean m_bStart;
	private static Queue<QueueMessage> m_queueMessage;

	private HCNetSDK.RECV_ALARM m_palarmInfo;
	private NativeLong m_command;
	private HCNetSDK.NET_DVR_ALARMER m_pAlarmer;

	/**
	 * @param palarmInfo 缓存区
	 * @param command
	 * @param pAlarmer   报警设备信息
	 */
	public AlarmThread(HCNetSDK.RECV_ALARM palarmInfo, NativeLong command, HCNetSDK.NET_DVR_ALARMER pAlarmer) {
		m_palarmInfo = palarmInfo;
		m_command = command;
		m_pAlarmer = pAlarmer;
		m_bStart = false;
	}

	public void SetStart(boolean bStart) {
		m_bStart = bStart;
	}

	public boolean GetStart() {
		return m_bStart;
	}

	public static AlarmThread getInstance() {
		if (m_alarmThread == null) {
			m_alarmThread = new AlarmThread();
			m_queueMessage = new LinkedList<QueueMessage>();
		}
		return m_alarmThread;
	}

	public void AddMessage(QueueMessage queueMessage) {
		synchronized (this) {
			System.out.println("add message");
			m_queueMessage.add(queueMessage);
		}

	}

	public QueueMessage RemoveMessage() {
		synchronized (this) {
			System.out.println("remove message");
			return m_queueMessage.remove();
		}

	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(m_queueMessage.isEmpty());

			if (!m_queueMessage.isEmpty()) {
				System.out.println(m_queueMessage.isEmpty());
				QueueMessage queueMessage = RemoveMessage();
				NativeLong command = queueMessage.getM_command();
				HCNetSDK.RECV_ALARM palarmInfo = queueMessage.getM_palarmInfo();
				HCNetSDK.NET_DVR_ALARMER pAlarmer = queueMessage.getM_pAlarmer();
				// NativeLong command = m_command;
				// HCNetSDK.RECV_ALARM palarmInfo = m_palarmInfo;
				// HCNetSDK.NET_DVR_ALARMER pAlarmer = m_pAlarmer;
				// System.out.println("------------2");
				switch (command.intValue()) {

				case HCNetSDK.COMM_ALARM_V40:
					HCNetSDK.NET_DVR_ALARMINFO_V40 struAlarmInfoV40 = new HCNetSDK.NET_DVR_ALARMINFO_V40();
					struAlarmInfoV40.write();
					Pointer pInfoV40 = struAlarmInfoV40.getPointer();
					pInfoV40.write(0, palarmInfo.RecvBuffer, 0, struAlarmInfoV40.size());
					struAlarmInfoV40.read();
					HandleAlarmV40(struAlarmInfoV40, pAlarmer);
					break;
				case HCNetSDK.COMM_ALARM_V30:
					HCNetSDK.NET_DVR_ALARMINFO_V30 strAlarmInfoV30 = new HCNetSDK.NET_DVR_ALARMINFO_V30();
					strAlarmInfoV30.write();
					Pointer pInfoV30 = strAlarmInfoV30.getPointer();
					pInfoV30.write(0, palarmInfo.RecvBuffer, 0, strAlarmInfoV30.size());
					strAlarmInfoV30.read();
					HandleAlarmV30(strAlarmInfoV30, pAlarmer);
					break;
				case HCNetSDK.COMM_ALARM:
					HCNetSDK.NET_DVR_ALARMINFO strAlarmInfo = new HCNetSDK.NET_DVR_ALARMINFO();
					strAlarmInfo.write();
					Pointer pInfo = strAlarmInfo.getPointer();
					pInfo.write(0, palarmInfo.RecvBuffer, 0, strAlarmInfo.size());
					strAlarmInfo.read();
					HandleAlarm(strAlarmInfo, pAlarmer);
					break;

				// Traffic statistics
				case HCNetSDK.COMM_ALARM_PDC:
					NET_DVR_PDC_ALRAM_INFO struPdcInfo = new NET_DVR_PDC_ALRAM_INFO();
					struPdcInfo.write();
					Pointer pPdcInfo = struPdcInfo.getPointer();
					pPdcInfo.write(0, palarmInfo.RecvBuffer, 0, struPdcInfo.size());
					struPdcInfo.read();
					HandleAlarmPDC(struPdcInfo, pAlarmer);
					break;

				// License plate recognition
				case HCNetSDK.COMM_UPLOAD_PLATE_RESULT:
					NET_DVR_PLATE_RESULT struResult = new NET_DVR_PLATE_RESULT();
					struResult.write();
					Pointer pResult = struResult.getPointer();
					pResult.write(0, palarmInfo.RecvBuffer, 0, struResult.size());
					struResult.read();
					HandlePlateResult(struResult, pAlarmer);
					break;

				default:
					// log.info("unknow type");
					break;
				}
			}
		}
	}

	public void HandleAlarmV40(HCNetSDK.NET_DVR_ALARMINFO_V40 struAlarmInfo, HCNetSDK.NET_DVR_ALARMER pAlarmer) {
		boolean bDisk = false;
		@SuppressWarnings("unused")
		boolean bAlarmOut = false;
		String sAlarmType = "";
		String sAlarmInfo = "";
		int iLen = 0;
		int arr[] = new int[iLen];
		int dwOffSetLen = 0;
		arr = struAlarmInfo.pAlarmData.getIntArray(0, iLen);
		switch (struAlarmInfo.struAlarmFixedHeader.dwAlarmType) {
		case 0:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struIOAlarm.class);
			sAlarmInfo = "Switch alarm inpu No."
					+ (struAlarmInfo.struAlarmFixedHeader.ustruAlarm.struioAlarm.dwAlarmInputNo + 1);
			sAlarmType = "Invoke Alarm Out:";
			iLen += struAlarmInfo.struAlarmFixedHeader.ustruAlarm.struioAlarm.dwTrigerAlarmOutNum;
			for (int i = 0; i < struAlarmInfo.struAlarmFixedHeader.ustruAlarm.struioAlarm.dwTrigerAlarmOutNum; i++) {
				if (arr[i] != 0xffffffff) {
					sAlarmInfo += sAlarmType + " " + arr[i];
				}
			}
			sAlarmInfo += "Invoke record Channel:";
			dwOffSetLen += struAlarmInfo.struAlarmFixedHeader.ustruAlarm.struioAlarm.dwTrigerAlarmOutNum;
			for (int i = 0; i < struAlarmInfo.struAlarmFixedHeader.ustruAlarm.struioAlarm.dwTrigerRecordChanNum; i++) {
				if (arr[i + dwOffSetLen] != 0xffffffff) {
					sAlarmInfo += " " + arr[i + dwOffSetLen];
					dwOffSetLen++;
				}
			}
			break;
		case 1:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmHardDisk.class);
			sAlarmInfo = "HD is full:";
			for (int i = 0; i < struAlarmInfo.struAlarmFixedHeader.ustruAlarm.strualarmHardDisk.dwAlarmHardDiskNum; i++) {
				if (arr[i + dwOffSetLen] != 0xffffffff) {
					sAlarmInfo += " " + arr[i + dwOffSetLen];
					dwOffSetLen++;
				}
			}
			break;
		case 4:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmHardDisk.class);
			sAlarmInfo = "not formatted";
			for (int i = 0; i < struAlarmInfo.struAlarmFixedHeader.ustruAlarm.strualarmHardDisk.dwAlarmHardDiskNum; i++) {
				int iTemp = arr[i + dwOffSetLen];
				if (iTemp > 0) {
					sAlarmInfo += " HDD " + iTemp;
					dwOffSetLen++;
				} else {
					break;
				}
			}
			break;
		case 5:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmHardDisk.class);
			for (int i = 0; i < struAlarmInfo.struAlarmFixedHeader.ustruAlarm.strualarmHardDisk.dwAlarmHardDiskNum; i++) {
				int iTemp = arr[i + dwOffSetLen];
				if (iTemp > 0) {
					bDisk = true;
					sAlarmInfo += "failed to read and write HDD HDD" + iTemp;
					dwOffSetLen++;
				} else {
					break;
				}
			}
			if (bDisk == false) {
				sAlarmInfo = "No HDD";
			}
			bDisk = false;
			break;
		case 2:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "The signal lost";
			break;
		case 3:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "motion detection";
			break;
		case 6:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "Mask alarm";
			break;
		case 9:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "video exception";
			break;
		case 10:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "record exception";
			break;
		case 11:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "Vca scene change";
			break;
		case 13:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "resolution mismatch";
			break;
		case 15:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "VCA Detection";
			break;
		case 16:
			struAlarmInfo.struAlarmFixedHeader.ustruAlarm.setType(struAlarmChannel.class);
			bAlarmOut = true;
			sAlarmInfo = "host record alarm";
			break;

		case 7: // type unmatched
			sAlarmInfo = "input and output video standard not match";
			break;
		case 8:
			sAlarmInfo = "illegal access";
			break;
		case 12:
			sAlarmInfo = "Array exception";
			break;
		case 14:
			sAlarmInfo = "Alloc decoede resource fail";
			break;
		case 17:
			sAlarmInfo = "hot spare alarm";
			break;
		default:
			sAlarmInfo = "Unknow";
			break;
		}

		AddAlarmInfo(sAlarmInfo, pAlarmer);
	}

	public void HandleAlarmV30(HCNetSDK.NET_DVR_ALARMINFO_V30 struAlarmInfo, HCNetSDK.NET_DVR_ALARMER pAlarmer) {
		boolean bDisk = false;
		String sAlarmInfo = "";
		switch (struAlarmInfo.dwAlarmType) {
		case 0:
			sAlarmInfo = "Switch alarm input" + (struAlarmInfo.dwAlarmInputNumber + 1);
			sAlarmInfo += "Invoke record Channel:";
			for (int i = 0; i < HCNetSDK.MAX_CHANNUM_V30; i++) {
				if (struAlarmInfo.byAlarmRelateChannel[i] == 1) {
					sAlarmInfo += "" + i;
				}
			}
			break;
		case 1:
			sAlarmInfo = "HD is full:";
			for (int i = 0; i < HCNetSDK.MAX_DISKNUM_V30; i++) {
				if (struAlarmInfo.byDiskNumber[i] == 1) {
					sAlarmInfo += " HDD " + (i + 1);
				}
			}
		case 2:
			sAlarmInfo = "The signal lost";
			break;
		case 3:
			sAlarmInfo = "motion detection";
			break;
		case 4:
			sAlarmInfo = "not formatted";
			for (int i = 0; i < HCNetSDK.MAX_DISKNUM_V30; i++) {
				if (struAlarmInfo.byDiskNumber[i] == 1) {
					sAlarmInfo += " HDD" + i;
				}
			}
			break;
		case 5:
			for (int i = 0; i < HCNetSDK.MAX_DISKNUM_V30; i++) {
				if (struAlarmInfo.byDiskNumber[i] == 1) {
					bDisk = true;
					sAlarmInfo = "failed to read and write HDD" + (i + 1);
					AddAlarmInfo(sAlarmInfo, pAlarmer);
				}
			}
			if (bDisk == false) {
				sAlarmInfo = "No HDD";
			}
			bDisk = false;
			break;
		case 6:
			sAlarmInfo = "Mask alarm";
			break;
		case 7:
			sAlarmInfo = "input and output video standard not match";
			break;
		case 8:
			sAlarmInfo = "illegal access";
			break;
		case 9:
			sAlarmInfo = "video exception";
			break;
		case 10:
			sAlarmInfo = "record exception";
			break;
		case 11:
			sAlarmInfo = "Vca scene change";
			break;
		case 12:
			sAlarmInfo = "Array exception";
			break;
		case 13:
			sAlarmInfo = "resolution mismatch ";
			break;
		case 14:
			sAlarmInfo = "Alloc decoede resource fail";
			break;
		case 15:
			sAlarmInfo = "VCA Detction";
			break;
		case 16:
			sAlarmInfo = "POE Power Exception";
			break;
		case 17:
			sAlarmInfo = "Flash anomaly";
			break;
		case 18:
			sAlarmInfo = "Disk full load anomaly";
			break;
		case 19:
			sAlarmInfo = "The audio signal lost";
			break;
		default:
			sAlarmInfo = "Unknown Alarm Type:" + struAlarmInfo.dwAlarmType;
			break;
		}
		AddAlarmInfo(sAlarmInfo, pAlarmer);

	}

	public void HandleAlarm(HCNetSDK.NET_DVR_ALARMINFO strAlarmInfo, HCNetSDK.NET_DVR_ALARMER pAlarmer) {
		String sAlarmType = "";
		boolean bDisk = false;
		switch (strAlarmInfo.dwAlarmType) {
		case 0:
			sAlarmType = "Switch alarm input";
			break;
		case 1:
			sAlarmType = "HDD is Full:";
			for (int i = 0; i < HCNetSDK.MAX_DISKNUM; i++) {
				if (strAlarmInfo.dwDiskNumber[i] == 1) {
					sAlarmType += " HDD" + i;
				}
			}
			break;
		case 2:
			sAlarmType = "The video signal lost";
			break;
		case 3:
			sAlarmType = "motion detection";
			break;
		case 4:
			sAlarmType = "HDD not formatted:";
			for (int i = 0; i < HCNetSDK.MAX_DISKNUM; i++) {
				if (strAlarmInfo.dwDiskNumber[i] == 1) {
					sAlarmType += " HDD" + i;
				}
			}
			break;
		case 5:
			for (int i = 0; i < HCNetSDK.MAX_DISKNUM; i++) {
				if (strAlarmInfo.dwDiskNumber[i] == 1) {
					bDisk = true;
					sAlarmType = "failed to read and write HDD HDD" + i;
					AddAlarmInfo(sAlarmType, pAlarmer);
				}
			}
			if (bDisk == false) {
				sAlarmType = "no HDD";
			}
			bDisk = false;
			break;
		case 6:
			sAlarmType = "Mask alarm";
			break;
		case 7:
			sAlarmType = "input and output video standard not match";
			break;
		case 8:
			sAlarmType = "illegal access";
			break;
		case 9:
			sAlarmType = "video exception";
			break;
		case 10:
			sAlarmType = "record exception";
			break;
		case 11:
			sAlarmType = "Vca scene change";
			break;
		default:
			sAlarmType = "Unknown Alarm Type";
			break;
		}
	}

	public void HandleAlarmPDC(HCNetSDK.NET_DVR_PDC_ALRAM_INFO struAlarmInfo, HCNetSDK.NET_DVR_ALARMER pAlarmer) {
		String sAlarmInfo = "";
		if (struAlarmInfo.byMode == 0) {
			struAlarmInfo.ustateModeParam.setType(struStatFrame.class);
			HCNetSDK.NET_DVR_TIME struAbsTime = new HCNetSDK.NET_DVR_TIME();
			struAbsTime.dwYear = struAlarmInfo.ustateModeParam.strustatFrame.dwAbsTime >> 26 + 2000;
			struAbsTime.dwMonth = struAlarmInfo.ustateModeParam.strustatFrame.dwAbsTime >> 22 & 15;
			struAbsTime.dwDay = struAlarmInfo.ustateModeParam.strustatFrame.dwAbsTime >> 17 & 31;
			struAbsTime.dwHour = struAlarmInfo.ustateModeParam.strustatFrame.dwAbsTime >> 12 & 31;
			struAbsTime.dwMinute = struAlarmInfo.ustateModeParam.strustatFrame.dwAbsTime >> 6 & 63;
			struAbsTime.dwSecond = struAlarmInfo.ustateModeParam.strustatFrame.dwAbsTime >> 0 & 63;

			HCNetSDK.NET_DVR_TIME struRelativeTime = new HCNetSDK.NET_DVR_TIME();
			struRelativeTime.dwYear = struAlarmInfo.ustateModeParam.strustatFrame.dwRelativeTime >> 26 + 2000;
			struRelativeTime.dwMonth = struAlarmInfo.ustateModeParam.strustatFrame.dwRelativeTime >> 22 & 15;
			struRelativeTime.dwDay = struAlarmInfo.ustateModeParam.strustatFrame.dwRelativeTime >> 17 & 31;
			struRelativeTime.dwHour = struAlarmInfo.ustateModeParam.strustatFrame.dwRelativeTime >> 12 & 31;
			struRelativeTime.dwMinute = struAlarmInfo.ustateModeParam.strustatFrame.dwRelativeTime >> 6 & 63;
			struRelativeTime.dwSecond = struAlarmInfo.ustateModeParam.strustatFrame.dwRelativeTime >> 0 & 63;

			String.format(
					"Abs[%4.4d%2.2d%2.2d%2.2d%2.2d%2.2d] Rel[%4.4d%2.2d%2.2d%2.2d%2.2d%2.2d] DevIP:[%s]Port[%d]Channel[%d]"
							+ "single frame:Channel[%d]LeaveNum[%d]EnterNum[%d]Smart[%d]",
					struAbsTime.dwYear, struAbsTime.dwMonth, struAbsTime.dwDay, struAbsTime.dwHour,
					struAbsTime.dwMinute, struAbsTime.dwSecond, struRelativeTime.dwYear, struRelativeTime.dwMonth,
					struRelativeTime.dwDay, struRelativeTime.dwHour, struRelativeTime.dwMinute,
					struRelativeTime.dwSecond, struAlarmInfo.struDevInfo.struDevIP.sIpV4,
					struAlarmInfo.struDevInfo.wPort, struAlarmInfo.struDevInfo.byChannel, struAlarmInfo.byChannel,
					struAlarmInfo.dwLeaveNum, struAlarmInfo.dwEnterNum, struAlarmInfo.bySmart);
		} else if (struAlarmInfo.byMode == 1) {
			struAlarmInfo.ustateModeParam.setType(struStartTime.class);
			String.format(
					"DevIP:[%s]Port[%d]Channel[%d]:time range:Start Y[%d]M[%d]D[%d]H[%d]M[%d]S[%d]  End Y[%d]M[%d]D[%d]H[%d]M[%d]S[%d]Channel[%d]LeaveNum[%d]EnterNum[%d]Smart[%d]",
					struAlarmInfo.struDevInfo.struDevIP.sIpV4, struAlarmInfo.struDevInfo.wPort,
					struAlarmInfo.struDevInfo.byChannel, struAlarmInfo.ustateModeParam.strustartTime.tmStart.dwYear,
					struAlarmInfo.ustateModeParam.strustartTime.tmStart.dwMonth,
					struAlarmInfo.ustateModeParam.strustartTime.tmStart.dwDay,
					struAlarmInfo.ustateModeParam.strustartTime.tmStart.dwHour,
					struAlarmInfo.ustateModeParam.strustartTime.tmStart.dwMinute,
					struAlarmInfo.ustateModeParam.strustartTime.tmStart.dwSecond,

					struAlarmInfo.ustateModeParam.strustartTime.tmEnd.dwYear,
					struAlarmInfo.ustateModeParam.strustartTime.tmEnd.dwMonth,
					struAlarmInfo.ustateModeParam.strustartTime.tmEnd.dwDay,
					struAlarmInfo.ustateModeParam.strustartTime.tmEnd.dwHour,
					struAlarmInfo.ustateModeParam.strustartTime.tmEnd.dwMinute,
					struAlarmInfo.ustateModeParam.strustartTime.tmEnd.dwSecond,

					struAlarmInfo.byChannel, struAlarmInfo.dwLeaveNum, struAlarmInfo.dwEnterNum, struAlarmInfo.bySmart);
		}

		AddAlarmInfo(sAlarmInfo, pAlarmer);
	}

	public void HandlePlateResult(HCNetSDK.NET_DVR_PLATE_RESULT struAlarmInfo, HCNetSDK.NET_DVR_ALARMER pAlarmer) {
		String sAlarmInfo = "";
		sAlarmInfo += "byResult[" + struAlarmInfo.byResultType + "]byChanIndex[" + struAlarmInfo.byChanIndex
				+ "]wAlarmRecordID[" + struAlarmInfo.wAlarmRecordID + "]dwRelativeTime[" + struAlarmInfo.dwRelativeTime
				+ "]dwPicLen[" + struAlarmInfo.dwPicLen + "]dwPicPlateLen[" + struAlarmInfo.dwPicPlateLen
				+ "]dwVideoLen[" + struAlarmInfo.dwVideoLen + "]byTrafficLight[" + struAlarmInfo.byTrafficLight
				+ "]byPicNum[" + struAlarmInfo.byPicNum + "]byDvriveChan[" + struAlarmInfo.byDriveChan
				+ "]byVehicleType[" + struAlarmInfo.byVehicleType + "]dwBinPicLen[" + struAlarmInfo.dwBinPicLen;
		AddAlarmInfo(sAlarmInfo, pAlarmer);
	}

	public void AddAlarmInfo(String strInfo, HCNetSDK.NET_DVR_ALARMER pAlarmer) {
		// insert data
		String[] newRow = new String[3];
		// alarm time
		Date today = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String[] sIP = new String[2];
		newRow[0] = dateFormat.format(today);
		// alarm info
		newRow[1] = strInfo;
		// device ip
		sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
		newRow[2] = sIP[0];
		System.out.println(newRow[0] + newRow[1] + newRow[2]);
	}

}
