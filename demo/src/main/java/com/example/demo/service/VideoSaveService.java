package com.example.demo.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;
import com.example.demo.entity.HCNetSDK.NET_DVR_PLAYCOND;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;





@Service
public class VideoSaveService {

	@Value("${VideoPlay.ip}")
	private  String url ;
	
	private HCNetSDK hcNetSDK =HCNetSDK.INSTANCE;
	
	private NativeLong m_lLoadHandle;
	
	Timer Downloadtimer;
	Timer Playbacktimer;
	
	public  void getVideoByTime(String starTime,String EndTime,DeviceInfo deviceInfo,HCNetSDK hcNetSDK,String filePath) {
		
		
		
		DateTimeFormatter dFormat =DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime starDate = LocalDateTime.parse(starTime, dFormat);
		LocalDateTime endData = LocalDateTime.parse(EndTime, dFormat);
		
		
		
		HCNetSDK.NET_DVR_TIME struStartTime = new HCNetSDK.NET_DVR_TIME();
        HCNetSDK.NET_DVR_TIME struStopTime= new HCNetSDK.NET_DVR_TIME();
        
        
        struStartTime.dwYear = starDate.getYear();			struStopTime.dwYear = endData.getYear();
        struStartTime.dwMonth =  starDate.getMonthValue();  struStopTime.dwMonth =  endData.getMonthValue();  
        struStartTime.dwDay =starDate.getDayOfMonth(); 		struStopTime.dwDay =endData.getDayOfMonth();
        struStartTime.dwHour = starDate.getHour(); 			struStopTime.dwHour = endData.getHour();
        struStartTime.dwMinute =starDate.getMinute(); 		struStopTime.dwMinute =endData.getMinute();
        struStartTime.dwSecond = starDate.getSecond();  	struStopTime.dwSecond = endData.getSecond();
        struStartTime.write(); struStopTime.write();
  
        NET_DVR_PLAYCOND struDownloadCond = new NET_DVR_PLAYCOND();
        struDownloadCond.dwChannel = 1;
        struDownloadCond.struStartTime = struStartTime;
        struDownloadCond.struStopTime = struStopTime;
        struDownloadCond.write();
        NativeLong m_lUserID = deviceInfo.getM_nUserID();
        
		if(filePath == "") {
			
			 String baseUrl ="./src/main/resources/static/video";
			File mulu;
			try {
				mulu = new File(baseUrl);
				if(!mulu.exists()) {	
					System.out.println("no find");
					mulu.mkdirs();
				}
			}
			catch (Exception e) {
				// TODO: handle exception
			}
			
			filePath = baseUrl+"/" +struStartTime.toStringTitle() + struStopTime.toStringTitle() + ".mp4";
		}
		
        
        
        m_lLoadHandle = hcNetSDK.NET_DVR_GetFileByTime_V40(m_lUserID,filePath,struDownloadCond);
         
        String fileName =struStartTime.toStringTitle() + struStopTime.toStringTitle() + ".mp4";

        System.out.println(url+"/video/"+fileName);
        Pointer pointer =Pointer.createConstant(0);
       
        IntByReference nPos = new IntByReference(0);
        IntByReference outPos = new IntByReference(1024);
        if (m_lLoadHandle.intValue() >= 0)
        {
        	if(hcNetSDK.NET_DVR_PlayBackControl_V40(m_lLoadHandle, HCNetSDK.NET_DVR_PLAYSTART,nPos.getPointer(),0,null,outPos)) {
        		
        		 	Downloadtimer = new Timer();
	                Downloadtimer.schedule(new DownloadTask(), 0, 5000);
        		
        		
        	}else {
        		
        		  System.out.println("laste error " + hcNetSDK.NET_DVR_GetLastError());
			}
        	

        	
        } else
        {
        	
            System.out.println("laste error " + hcNetSDK.NET_DVR_GetLastError());
            return;
        }
        
	}
	
	
	public  void findFileCount(String starTime,String EndTime,DeviceInfo deviceInfo,HCNetSDK hcNetSDK) {
		
		DateTimeFormatter dFormat =DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime starDate = LocalDateTime.parse(starTime, dFormat);
		LocalDateTime endData = LocalDateTime.parse(EndTime, dFormat);
		
		HCNetSDK.NET_DVR_FILECOND_V40 filecond_V40 =new HCNetSDK.NET_DVR_FILECOND_V40();
		
		 NativeLong Iusrid = deviceInfo.getM_nUserID();
	     filecond_V40.lChannel = new NativeLong(deviceInfo.getM_lpDeviceInfo().byStartChan);
	     filecond_V40.dwFileType = 0xFF;
	     filecond_V40.dwUseCardNo = 0;
	     
	     HCNetSDK.NET_DVR_TIME struStartTime = new HCNetSDK.NET_DVR_TIME();
	     HCNetSDK.NET_DVR_TIME struStopTime= new HCNetSDK.NET_DVR_TIME();
        
        
        struStartTime.dwYear = starDate.getYear();			struStopTime.dwYear = endData.getYear();
        struStartTime.dwMonth =  starDate.getMonthValue();  struStopTime.dwMonth =  endData.getMonthValue();  
        struStartTime.dwDay =starDate.getDayOfMonth(); 		struStopTime.dwDay =endData.getDayOfMonth();
        struStartTime.dwHour = starDate.getHour(); 			struStopTime.dwHour = endData.getHour();
        struStartTime.dwMinute =starDate.getMinute(); 		struStopTime.dwMinute =endData.getMinute();
        struStartTime.dwSecond = starDate.getSecond();  	struStopTime.dwSecond = endData.getSecond();
        struStartTime.write(); struStopTime.write();
        
       
        filecond_V40.dwIsLocked = 0xff; 
        filecond_V40.struStartTime = struStartTime;
        filecond_V40.struStopTime = struStopTime;
       
        filecond_V40.write();
        
        System.out.println(filecond_V40.struStartTime);
        System.out.println(filecond_V40.struStopTime);
        NativeLong lFindFile=hcNetSDK.NET_DVR_FindFile_V40(Iusrid, filecond_V40);
		HCNetSDK.NET_DVR_FINDDATA_V40 strFile=new HCNetSDK.NET_DVR_FINDDATA_V40();
		strFile.dwFileSize = strFile.size();
		NativeLong lNext;
		while(true)
		{
			
			lNext=hcNetSDK.NET_DVR_FindNextFile_V40(lFindFile, strFile);
			System.out.println(lNext+"lNext");
			if(lNext.longValue() == HCNetSDK.NET_DVR_FILE_SUCCESS)
			{
				
				Vector<String> newRow=new Vector<>();
				
				
				String [] s=new String[2];
				s=new String(strFile.sFileName).split("\0",2);
				newRow.add(new String(s[0]));
				
				int iTemp;
				String str;
				if(strFile.dwFileSize < 1024*1024)
				{
					iTemp=(strFile.dwFileSize)/1024;
					str=iTemp+"K";
				}
				else
				{
					iTemp=(strFile.dwFileSize)/(1024*1024);
					str=iTemp+"M ";
					iTemp=((strFile.dwFileSize)%(1024*1024))/1024;
					str+=iTemp+"K";
				}
				newRow.add(str);
				newRow.add(strFile.struStartTime.toStringTime());
				newRow.add(strFile.struStopTime.toStringTime());
				
			}
			else
			{
				if(lNext.longValue()==HCNetSDK.NET_DVR_ISFINDING)
				{
					continue;
				}
				else
				{
					if(lNext.longValue()==HCNetSDK.NET_DVR_FILE_NOFIND)
					{
						System.out.println("find no file");
						
						break;
					}
					else
					{
						boolean flag=hcNetSDK.NET_DVR_FindClose_V30(lFindFile);
						if(flag==false)
						{
							System.out.println("search ending failed,error code:"+hcNetSDK.NET_DVR_GetLastError());
							
						}
						return;
					}
				}
			}
		}
		boolean flag=hcNetSDK.NET_DVR_FindClose_V30(lFindFile);
		if(flag==false)
		{
			System.out.println("search ending failed,error code:"+hcNetSDK.NET_DVR_GetLastError());
			
		}

	}
	
	public void timeQuit(DeviceInfo deviceInfo,HCNetSDK hcNetSDK) {
		HCNetSDK.NET_DVR_RECORD_TIME_SPAN_INQUIRY time_SPAN_INQUIRY =new HCNetSDK.NET_DVR_RECORD_TIME_SPAN_INQUIRY();
		time_SPAN_INQUIRY.dwSize=time_SPAN_INQUIRY.size();
		time_SPAN_INQUIRY.byType=0;
		time_SPAN_INQUIRY.write();
		HCNetSDK.NET_DVR_RECORD_TIME_SPAN time_SPAN = new HCNetSDK.NET_DVR_RECORD_TIME_SPAN();
		time_SPAN.dwSize = time_SPAN.size();
		time_SPAN.write();
		if(hcNetSDK.NET_DVR_InquiryRecordTimeSpan(deviceInfo.getM_nUserID(),deviceInfo.getM_lpDeviceInfo().byStartChan,time_SPAN_INQUIRY,time_SPAN)) {
			
			time_SPAN.read();	
			System.out.println("suces");
		}else {
			System.out.println("error " + hcNetSDK.NET_DVR_GetLastError());
		}
		
	}
	
	
	public  void findBycalend(DeviceInfo deviceInfo,HCNetSDK hcNetSDK) {
	
		NativeLong lUserID =deviceInfo.getM_nUserID();
		
		HCNetSDK.NET_DVR_MRD_SEARCH_PARAM scDvr_MRD_SEARCH_PARAM =new HCNetSDK.NET_DVR_MRD_SEARCH_PARAM();
		scDvr_MRD_SEARCH_PARAM.dwSize = scDvr_MRD_SEARCH_PARAM.size();
		STATUS_LIST_1 m_struStatusList = new STATUS_LIST_1();
		scDvr_MRD_SEARCH_PARAM.wYear = (short)1970;
		scDvr_MRD_SEARCH_PARAM.byMonth =1 ;
		scDvr_MRD_SEARCH_PARAM.byDrawFrame=0;
		scDvr_MRD_SEARCH_PARAM.byStreamType=0;
		scDvr_MRD_SEARCH_PARAM.write();
		HCNetSDK.NET_DVR_MRD_SEARCH_RESULT result  = new HCNetSDK.NET_DVR_MRD_SEARCH_RESULT();
		result.dwSize = result.size();
		result.write();
		if(hcNetSDK.NET_DVR_GetDeviceConfig(lUserID,6164,0,scDvr_MRD_SEARCH_PARAM.getPointer(),scDvr_MRD_SEARCH_PARAM.size(),
				m_struStatusList.getPointer(),result.getPointer(),result.size())) {
			result.read();
			System.out.println("succ");
			
		}else {
			
			System.out.println(hcNetSDK.NET_DVR_GetLastError());
		}
		
		
	}
	
	public static class STATUS_LIST_1 extends Structure{
		
		public int iStatusList1 = 0;
		
	}

	class DownloadTask extends java.util.TimerTask
    {
        
        @Override
        public void run()
        {
            IntByReference nPos = new IntByReference(0); 
            
            hcNetSDK.NET_DVR_PlayBackControl_V40(m_lLoadHandle, HCNetSDK.NET_DVR_PLAYGETPOS, null,0,nPos.getPointer() ,nPos);
            System.out.println(nPos.getValue());
            if (nPos.getValue() > 100)
            {
            	hcNetSDK.NET_DVR_StopGetFile(m_lLoadHandle);
                m_lLoadHandle.setValue(-1);
               
                Downloadtimer.cancel();
                System.out.println(nPos.getValue());
                System.out.println("NetWork or DVR busing,download exception stop");
				
            }
            if (nPos.getValue() == 100)
            {
            	hcNetSDK.NET_DVR_StopGetFile(m_lLoadHandle);
                m_lLoadHandle.setValue(-1);
                
                Downloadtimer.cancel();

                System.out.println("Download by time end");
				
            }
        }
    }

	
}
