package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.AlarmPoint;
import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

@Service
public class RegionPointService {

	HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;

	private STATUS_LIST_1 m_struStatusList = new STATUS_LIST_1();
	private NET_DVR_CHANNEL_GROUP_1 channel_GROUP_1 = new NET_DVR_CHANNEL_GROUP_1();
	private NET_DVR_MULTI_STREAM_COMPRESSIONCFG_1 multi_STREAM_COMPRESSIONCFG_1 = new NET_DVR_MULTI_STREAM_COMPRESSIONCFG_1();
	private NET_VCA_MULTI_TRAVERSE_PLANE_DETECTION multi_TRAVERSE_PLANE_DETECTION = new NET_VCA_MULTI_TRAVERSE_PLANE_DETECTION();
	public void Getpoint(DeviceInfo deviceInfo, HCNetSDK hcNetSDK) {

		@SuppressWarnings("unused")
		HCNetSDK.NET_DVR_REGION_ENTRANCE_DETECTION entrance_DETECTION = new HCNetSDK.NET_DVR_REGION_ENTRANCE_DETECTION();

		HCNetSDK.NET_DVR_SMART_REGION_COND smart_REGION_COND = new HCNetSDK.NET_DVR_SMART_REGION_COND();
		smart_REGION_COND.dwSize = smart_REGION_COND.size();
		smart_REGION_COND.dwChannel = deviceInfo.getM_lpDeviceInfo().byStartChan;
		smart_REGION_COND.dwRegion = 1;
		smart_REGION_COND.write();

		HCNetSDK.NET_DVR_REGIONENTRANCE_REGION region = new HCNetSDK.NET_DVR_REGIONENTRANCE_REGION();

		HCNetSDK.NET_DVR_STD_CONFIG stdConfInfo = new HCNetSDK.NET_DVR_STD_CONFIG();
		stdConfInfo.IpCondBuffer = smart_REGION_COND.getPointer();
		stdConfInfo.dwCondSize = smart_REGION_COND.size();
		stdConfInfo.IpOutBuffer = region.getPointer();
		stdConfInfo.dwOutSize = region.size();
		stdConfInfo.byDataType = 0;
		stdConfInfo.dwXmlSize = 2048;

		stdConfInfo.write();

		NativeLong IUserID = deviceInfo.getM_nUserID();

		if (hcNetSDK.NET_DVR_GetSTDConfig(IUserID, HCNetSDK.NET_DVR_GET_REGION_ENTR_REGION, stdConfInfo)) {
			stdConfInfo.read();
			region.read();

			for (HCNetSDK.NET_VCA_POINT point : region.struRegion.struPos) {
				System.out.println("=====" + point.fX + "=====" + point.fY);
			}

		} else {
			int iErr = hcNetSDK.NET_DVR_GetLastError();
			System.out.println("fail" + iErr);
		}

	}
	
	//getTraversPoint
	public List<AlarmPoint> GetTraversPont(DeviceInfo deviceInfo,int no) {
		NativeLong lUserID =deviceInfo.getM_nUserID();
		InitVideoCfg();
		
		Pointer lpMultiStreamCompressionCond = channel_GROUP_1.getPointer();
		Pointer lmultiTraversecfg = multi_TRAVERSE_PLANE_DETECTION.getPointer();
		Pointer lpStatusList = m_struStatusList.getPointer();
		
		channel_GROUP_1.write();
		multi_TRAVERSE_PLANE_DETECTION.write();
		m_struStatusList.write();
		
		if(hcNetSDK.NET_DVR_GetDeviceConfig(lUserID,HCNetSDK.NET_DVR_GET_TRAVERSE_PLANE_DETECTION , 1, lpMultiStreamCompressionCond, 
				channel_GROUP_1.size(), lpStatusList, lmultiTraversecfg, multi_TRAVERSE_PLANE_DETECTION.size())) {
			
			multi_TRAVERSE_PLANE_DETECTION.read();
			
			List<AlarmPoint> alarmPoints = new ArrayList<>();
			
			String x1 =  multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].struPlaneBottom.struStart.fX+"";
			String y1 =  multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].struPlaneBottom.struStart.fY+"";
			String x2 =  multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].struPlaneBottom.struEnd.fX+"";
			String y2 =  multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].struPlaneBottom.struEnd.fY+"";
			alarmPoints.add(new AlarmPoint(x1, y1));
			alarmPoints.add(new AlarmPoint(x2, y2));
			//System.out.println(multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.byEnale);
			//System.out.println(multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[0].dwCrossDirection);
			return alarmPoints;
			
		}else {
			int iErr = hcNetSDK.NET_DVR_GetLastError(); 
			System.out.println("fail"+iErr );
			return null;
		}
		
	
		
		
	}
	
	// setTraversPoint  CrossDirection 0:a<->b  1:a->b   2:a<-b  
	public boolean SetTraversPoint(DeviceInfo deviceInfo, String x1, String y1, String x2, String y2,int CrossDirection,int no) {

		NativeLong lUserID = deviceInfo.getM_nUserID();
		InitVideoCfg();

		Pointer lpMultiStreamCompressionCond = channel_GROUP_1.getPointer();
		Pointer lmultiTraversecfg = multi_TRAVERSE_PLANE_DETECTION.getPointer();
		Pointer lpStatusList = m_struStatusList.getPointer();
		
		channel_GROUP_1.write();
		multi_TRAVERSE_PLANE_DETECTION.write();
		m_struStatusList.write();

		if (hcNetSDK.NET_DVR_GetDeviceConfig(lUserID,HCNetSDK.NET_DVR_GET_TRAVERSE_PLANE_DETECTION , 1, lpMultiStreamCompressionCond, 
				channel_GROUP_1.size(), lpStatusList, lmultiTraversecfg, multi_TRAVERSE_PLANE_DETECTION.size())) {

			multi_TRAVERSE_PLANE_DETECTION.read();
			multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.byEnale = 1;
			multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].read();
			
			multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].struPlaneBottom.struStart.fX=Float.parseFloat(x1);
			multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].struPlaneBottom.struStart.fY=Float.parseFloat(y1);
			multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].struPlaneBottom.struEnd.fX=Float.parseFloat(x2);
			multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].struPlaneBottom.struEnd.fY=Float.parseFloat(y2);
			multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.struAlertParam[no].dwCrossDirection = CrossDirection;
			multi_TRAVERSE_PLANE_DETECTION.write();
			if (!hcNetSDK.NET_DVR_SetDeviceConfig(lUserID, HCNetSDK.NET_DVR_SET_TRAVERSE_PLANE_DETECTION, 1,
					lpMultiStreamCompressionCond, channel_GROUP_1.size(), lpStatusList, lmultiTraversecfg,
					multi_TRAVERSE_PLANE_DETECTION.size())) {

				System.out.println(hcNetSDK.NET_DVR_GetLastError());
				return false;

			}

		} else {
			int iErr = hcNetSDK.NET_DVR_GetLastError();
			System.out.println("fail" + iErr);
			return false;

		}

		return true;

	}
	
	

	// getPonit
	public List<AlarmPoint> GetFieldpoint(DeviceInfo deviceInfo,int num) {
		
		NativeLong lUserID =deviceInfo.getM_nUserID();
		InitVideoCfg();
		
		Pointer lpMultiStreamCompressionCond = channel_GROUP_1.getPointer();
		Pointer lpMultiStreamCompressionCfg = multi_STREAM_COMPRESSIONCFG_1.getPointer();
		Pointer lpStatusList = m_struStatusList.getPointer();
		
		channel_GROUP_1.write();
		multi_STREAM_COMPRESSIONCFG_1.write();
		m_struStatusList.write();
		
		if(hcNetSDK.NET_DVR_GetDeviceConfig(lUserID,HCNetSDK.NET_DVR_GET_FIELD_DETECTION , 1, lpMultiStreamCompressionCond, 
				channel_GROUP_1.size(), lpStatusList, lpMultiStreamCompressionCfg, multi_STREAM_COMPRESSIONCFG_1.size())) {
			
			multi_STREAM_COMPRESSIONCFG_1.read();
			
			List<AlarmPoint> alarmPoints = new ArrayList<>();
			
			String x1 =  multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[num].struRegion.struPos[0].fX+"";
			String y1 =  multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[num].struRegion.struPos[0].fY+"";
			String x2 =  multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[num].struRegion.struPos[1].fX+"";
			String y2 =  multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[num].struRegion.struPos[1].fY+"";	
			String x3 =  multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[num].struRegion.struPos[2].fX+"";
			String y3 =  multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[num].struRegion.struPos[2].fY+"";
			String x4 =  multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[num].struRegion.struPos[3].fX+"";
			String y4 =  multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[num].struRegion.struPos[3].fY+"";
			
			alarmPoints.add(new AlarmPoint(x1, y1));
			alarmPoints.add(new AlarmPoint(x2, y2));
			alarmPoints.add(new AlarmPoint(x3, y3));
			alarmPoints.add(new AlarmPoint(x4, y4));
			return alarmPoints;
			
		}else {
			int iErr = hcNetSDK.NET_DVR_GetLastError(); 
			System.out.println("fail"+iErr );
			return null;
		}
		
	
		
		
	}
	
	

	// setPonit
	public boolean SetFieldpoint(DeviceInfo deviceInfo, String x1, String y1, String x2, String y2
			 ,String x3, String y3,String x4, String y4,String num) {

		NativeLong lUserID = deviceInfo.getM_nUserID();
		InitVideoCfg();
		int no = Integer.parseInt(num);
		Pointer lpMultiStreamCompressionCond = channel_GROUP_1.getPointer();
		Pointer lpMultiStreamCompressionCfg = multi_STREAM_COMPRESSIONCFG_1.getPointer();
		Pointer lpStatusList = m_struStatusList.getPointer();

		channel_GROUP_1.write();
		multi_STREAM_COMPRESSIONCFG_1.write();
		m_struStatusList.write();

		if (hcNetSDK.NET_DVR_GetDeviceConfig(lUserID, HCNetSDK.NET_DVR_GET_FIELD_DETECTION, 1,
				lpMultiStreamCompressionCond, channel_GROUP_1.size(), lpStatusList, lpMultiStreamCompressionCfg,
				multi_STREAM_COMPRESSIONCFG_1.size())) {

			multi_STREAM_COMPRESSIONCFG_1.read();
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.byEnable = 1;
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.dwSize = multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.size();
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].read();
			
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.dwPointNum=4;
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.struPos[0].fX = Float
					.parseFloat(x1);
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.struPos[0].fY = Float
					.parseFloat(y1);
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.struPos[1].fX = Float
					.parseFloat(x2);
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.struPos[1].fY = Float
					.parseFloat(y2);
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.struPos[2].fX = Float
					.parseFloat(x3);
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.struPos[2].fY = Float
					.parseFloat(y3);
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.struPos[3].fX = Float
					.parseFloat(x4);
			multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struIntrusion[no].struRegion.struPos[3].fY = Float
					.parseFloat(y4);

			multi_STREAM_COMPRESSIONCFG_1.write();
			
			if (!hcNetSDK.NET_DVR_SetDeviceConfig(lUserID, HCNetSDK.NET_DVR_SET_FIELD_DETECTION, 1,
					lpMultiStreamCompressionCond, channel_GROUP_1.size(), lpStatusList, lpMultiStreamCompressionCfg,
					multi_STREAM_COMPRESSIONCFG_1.size())) {

				System.out.println(hcNetSDK.NET_DVR_GetLastError());
				return false;

			}

		} else {
			int iErr = hcNetSDK.NET_DVR_GetLastError();
			System.out.println("fail" + iErr);
			return false;

		}

		return true;

	}

	private void InitVideoCfg() {
		channel_GROUP_1.sChannel_GROUP.dwSize = channel_GROUP_1.sChannel_GROUP.size();
		channel_GROUP_1.sChannel_GROUP.dwChannel = 1;
		channel_GROUP_1.sChannel_GROUP.dwGroup = 0;
		channel_GROUP_1.sChannel_GROUP.dwPositionNo = 0;

		multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.dwSize = multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP
				.size();
		/*
		 * multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.dwMaxRelRecordChanNum = 1;
		 * multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.dwRelRecordChanNum = 1;
		 * multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struHandleException.dwHandleType = 0x00;
		 * multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struHandleException.dwMaxRelAlarmOutChanNum = 1;
		 * multi_STREAM_COMPRESSIONCFG_1.fielddetecion_GROUP.struHandleException.dwRelAlarmOutChanNum = 1;
		 */
		multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.dwSize = multi_TRAVERSE_PLANE_DETECTION.traverse_plane_defection.size();
		
	}

	public static class STATUS_LIST_1 extends Structure {
		public int iStatusList1 = 0;

	}

	public static class NET_DVR_CHANNEL_GROUP_1 extends Structure {
		public HCNetSDK.NET_DVR_CHANNEL_GROUP sChannel_GROUP = new HCNetSDK.NET_DVR_CHANNEL_GROUP();

	}

	public static class NET_DVR_MULTI_STREAM_COMPRESSIONCFG_1 extends Structure {
		public HCNetSDK.NET_VCA_FIELDDETECION fielddetecion_GROUP = new HCNetSDK.NET_VCA_FIELDDETECION();

	}
	
	public static class NET_VCA_MULTI_TRAVERSE_PLANE_DETECTION extends Structure{
		
		public HCNetSDK.NET_VCA_TRAVERSE_PLANE_DETECTION traverse_plane_defection = new HCNetSDK.NET_VCA_TRAVERSE_PLANE_DETECTION();
	}
	
}
