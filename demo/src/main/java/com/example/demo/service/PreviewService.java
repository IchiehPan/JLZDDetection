package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;
import com.example.demo.utils.FPLAYEsCallBack;

import com.example.demo.utils.PlayCallBack;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;


@Service
public class PreviewService {
	HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
	boolean m_isPlaying = true;
	static NativeLong m_lPreviewHandle = new NativeLong(0);// preview handle
	DeviceInfo deviceInfo;

	public void startPlay(DeviceInfo deviceInfo) {
		
		this.deviceInfo = deviceInfo;
		int iChannelNum = deviceInfo.getM_lpDeviceInfo().byStartChan;

		/*
		 * HCNetSDK.NET_DVR_PREVIEWINFO strPreviewInfo = new
		 * HCNetSDK.NET_DVR_PREVIEWINFO(); strPreviewInfo.lChannel = new
		 * NativeLong(iChannelNum); strPreviewInfo.dwStreamType = 0;
		 * strPreviewInfo.dwLinkMode = 0; strPreviewInfo.hPlayWnd =null;
		 * strPreviewInfo.bBlocked = false; strPreviewInfo.bPassbackRecord=false;
		 * strPreviewInfo.byPreviewMode=0; strPreviewInfo.byProtoType=1;
		 * strPreviewInfo.dwDisplayBufNum=1;
		 * 
		 * strPreviewInfo.write(); StartPlay(deviceInfo.GetNUserID(), strPreviewInfo);
		 */
		HCNetSDK.NET_DVR_CLIENTINFO clientinfo = new HCNetSDK.NET_DVR_CLIENTINFO();
		clientinfo.lChannel = new NativeLong(iChannelNum);
		clientinfo.hPlayWnd = null; // 窗口为空，设备SDK不解码只取流
		clientinfo.lLinkMode = new NativeLong(0); // Main Stream
		clientinfo.sMultiCastIP = null;
		clientinfo.write();
		StartPlay2(deviceInfo.getM_nUserID(), clientinfo);

	}

	private void StartPlay2(NativeLong lUserId, HCNetSDK.NET_DVR_CLIENTINFO clientinfo) {

		/*
		 * if (m_isPlaying) { StopPlay(); }
		 */

		PlayCallBack fBack = new PlayCallBack(deviceInfo.getM_sDVRIP());
		m_lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V30(lUserId, clientinfo, fBack, null, true);
          
		
		if (m_lPreviewHandle.longValue() == -1) {
			int iErr = hCNetSDK.NET_DVR_GetLastError();
			System.out.println("realplay err" + iErr);
		}
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * if(!hCNetSDK.NET_DVR_StopRealPlay(m_lPreviewHandle)) {
		 * 
		 * return; } //注销用户 hCNetSDK.NET_DVR_Logout(deviceInfo.GetNUserID());
		 * hCNetSDK.NET_DVR_Cleanup();
		 */

		/*
		 * if(hCNetSDK.NET_DVR_SetStandardDataCallBack(m_lPreviewHandle,new
		 * FrowDataCallBack(),deviceInfo.GetNUserID().intValue())) {
		 * 
		 * }else { int iErr = hCNetSDK.NET_DVR_GetLastError();
		 * System.out.println("realplay err" + iErr); }
		 */

		m_isPlaying = true;
	}

	@SuppressWarnings("unused")
	private void StartPlay(NativeLong lUserId, HCNetSDK.NET_DVR_PREVIEWINFO struPreviewInfo) {

		if (m_isPlaying) {
			StopPlay();
		}

		PlayCallBack fBack = new PlayCallBack();
		m_lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V40(lUserId, struPreviewInfo, fBack, null);

		if (m_lPreviewHandle.longValue() == -1) {
			int iErr = hCNetSDK.NET_DVR_GetLastError();
			System.out.println("realplay err" + iErr);
		}

		m_isPlaying = true;
	}

	// stop play
	private void StopPlay() {
		if (m_isPlaying) {
			if (m_lPreviewHandle.intValue() > 0)
				this.hCNetSDK.NET_DVR_StopRealPlay(m_lPreviewHandle);

			m_isPlaying = false;
		}
	}

	public void getRealESData(DeviceInfo deviceInfo, HCNetSDK hcNetSDK) {

		this.hCNetSDK = hcNetSDK;
		this.deviceInfo = deviceInfo;
		int iChannelNum = deviceInfo.getM_lpDeviceInfo().byStartChan;
		System.out.println(iChannelNum);
		HCNetSDK.NET_DVR_PREVIEWINFO strPreviewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
		strPreviewInfo.lChannel = new NativeLong(iChannelNum);
		strPreviewInfo.dwStreamType = 1;
		strPreviewInfo.dwLinkMode = 0;
		strPreviewInfo.hPlayWnd = null;
		strPreviewInfo.bBlocked = true;
		strPreviewInfo.bPassbackRecord = false;
		strPreviewInfo.byPreviewMode = 0;
		strPreviewInfo.byProtoType = 1;
		strPreviewInfo.dwDisplayBufNum = 1;
		strPreviewInfo.write();

		m_lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V40(deviceInfo.getM_nUserID(), strPreviewInfo, null, null);

		if (m_lPreviewHandle.longValue() == -1) {
			int iErr = hCNetSDK.NET_DVR_GetLastError();
			System.out.println("realplay err" + iErr);
		}
		Pointer pointer = null;

		boolean IsEsData = hCNetSDK.NET_DVR_SetESRealPlayCallBack(m_lPreviewHandle, new FPLAYEsCallBack(), pointer);

		System.out.println(IsEsData);
		/*
		 * if(IsEsData) { boolean isKey =
		 * hCNetSDK.NET_DVR_MakeKeyFrameSub(deviceInfo.GetNUserID(), new
		 * NativeLong(deviceInfo.GetlpDeviceInfo().byStartChan));
		 * System.out.println(isKey); } else { int iErr =
		 * hCNetSDK.NET_DVR_GetLastError(); System.out.println("realplay err" + iErr); }
		 */
	}

	public void getRealDataCallBack(DeviceInfo deviceInfo, HCNetSDK hcNetSDK) {

	}

}
