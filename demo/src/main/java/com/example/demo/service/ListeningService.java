package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.entity.AlarmThread;
import com.example.demo.entity.DeviceInfo;
import com.example.demo.entity.HCNetSDK;
import com.example.demo.entity.QueueMessage;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;



@Service
public class ListeningService {
	
	
	static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
	FMSGCallBack fMSFCallBack;// alarm callback
	private AlarmThread paAlarmThread;
	private String m_ip;
	private DeviceInfo deviceInfo;
	private String ListenPotr ;
	
	public void startListen(String ip,DeviceInfo deviceInfo,String port) {
		m_ip =ip;
		ListenPotr=port;
		this.deviceInfo= deviceInfo;
		actionPerformedStop();
		actionPerformedListeing();
	}
	
	
	public void actionPerformedListeing() {
		NativeLong lUserID = deviceInfo.getM_nUserID();
		if (lUserID.intValue() == -1)
		{
			System.out.println("Please login");
			
			return;
		}
		
		String sIP = GetHostIP();
		System.out.println("sip="+sIP);
		if (deviceInfo.getM_lListenHandle().intValue() == -1)
		{
			if (fMSFCallBack == null)
			{
				fMSFCallBack = new FMSGCallBack();
			}
			Pointer pUser = null;
			if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V30(fMSFCallBack, pUser))
			{
				System.out.println("Set callback fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
				return;
			}
			paAlarmThread = AlarmThread.getInstance();
			if(!paAlarmThread.GetStart())
			{
				
				paAlarmThread.start();
				paAlarmThread.SetStart(true);
			}
			int iPort = Integer.parseInt(ListenPotr);
			NativeLong lListenHandle = hCNetSDK.NET_DVR_StartListen_V30(sIP, (short) iPort, fMSFCallBack, null);
			deviceInfo.setM_lListenHandle(lListenHandle);
			if (lListenHandle.intValue() == -1)
			{
				System.out.println("Starting listening fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
				return;
			}
		}
		
		
	}
	
	public void actionPerformedStop()
	{
		
		if (deviceInfo.getM_lListenHandle().intValue() != -1)
        {
            if (!hCNetSDK.NET_DVR_StopListen_V30(deviceInfo.getM_lListenHandle()))
            {
            	System.out.println("Stop listening fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
                deviceInfo.setM_lListenHandle(new NativeLong(-1)); 
                return;
            }
            else
            {
            	deviceInfo.setM_lListenHandle(new NativeLong(-1));                    
            }
        }
	}

	
	public String GetHostIP()
	{
		byte[]sAllIP = new byte[16*16];
		IntByReference ptrdwIpNum = new IntByReference(0);
		HCNetSDK.Bind b = new HCNetSDK.Bind();
		b.write();
		Pointer ptr = b.getPointer();		
		ByteByReference ptrbBind = new ByteByReference((byte)0);
		if(!hCNetSDK.NET_DVR_GetLocalIP(sAllIP, ptrdwIpNum, ptrbBind))
		{
			System.out.println("get ip fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
			return "";
		}
		b.read();
		
		System.out.println("num="+ptrdwIpNum.getValue());
		String ip="";
		for(int i=0;i<ptrdwIpNum.getValue();i++)
		{
			String str = new String(sAllIP, i*16, 16);
			ip=ip+str;
		}
		return ip;
	}
	
	
	public class FMSGCallBack implements HCNetSDK.FMSGCallBack
	{
		// alarm info call back function

		@Override
		public void invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, HCNetSDK.RECV_ALARM pAlarmInfo,
				int dwBufLen, Pointer pUser)
		{
			QueueMessage queueMessage = new QueueMessage(pAlarmInfo, lCommand, pAlarmer);
			paAlarmThread.AddMessage(queueMessage);
			System.out.println("------------1");
		}
	}
	
}
