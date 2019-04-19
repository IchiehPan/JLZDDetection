package com.example.demo.utils;


import com.example.demo.entity.HCNetSDK;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class ClientDemo {

	static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;//设备信息   

    public NativeLong lUserID;//用户句柄
    public NativeLong lAlarmHandle;//报警布防句柄
    public NativeLong lListenHandle;//报警监听句柄

    FMSGCallBack fMSFCallBack;//报警回调函数实现

    
    public String m_sDeviceIP;//已登录设备的IP地址
    public String username; //设备用户名
    public String password;//设备登录密码
    public int iPort;//设备端口号
    
    public ClientDemo() {
        //initComponents();
        lUserID = new NativeLong(-1);
        lAlarmHandle = new NativeLong(-1);
        lListenHandle = new NativeLong(-1);
        fMSFCallBack = null;
        
    }
    //
    public void CameraInit(){
    	//初始化
        boolean initSuc = hCNetSDK.NET_DVR_Init();
        if (initSuc != true){                 
            System.out.println("初始化失败");
        }else{
        	System.out.println("初始化成功");
        }
    }
    //注册
    public NativeLong register(String username,String password,String m_sDeviceIP){
    	//注册之前先注销已注册的用户,预览情况下不可注销     
        if (lUserID.longValue() > -1) {
            //先注销
            hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = new NativeLong(-1);
        }

        //注册
        m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int iPort = 8000;
        System.out.println("注册，设备IP："+m_sDeviceIP);
        lUserID = hCNetSDK.NET_DVR_Login_V30(m_sDeviceIP,(short) iPort, username, password, m_strDeviceInfo);

        long userID = lUserID.longValue();
        if (userID == -1) {            
            System.out.println("注册失败");
        } else {            
            System.out.println("注册成功,lUserID:"+userID);
        }
        return lUserID;
    }
    //注销
    public void Logout(){
    	//报警撤防
        if (lAlarmHandle.intValue() > -1)
        {
            if(!hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle))
            {
                 System.out.println("撤防失败");
            }else{
                lAlarmHandle = new NativeLong(-1);
                System.out.println("撤防成功");
            }
        }

        //注销
        if (lUserID.longValue() > -1) {            
            if(hCNetSDK.NET_DVR_Logout(lUserID))
            {
                System.out.println("注销成功");
                lUserID = new NativeLong(-1);
            }
        }
    }
  
    //布防
    public long SetupAlarmChan() {
    	long AlarmHandle = 0;
        if (lUserID.intValue() == -1)
        {
            System.out.println("请先注册");
            return 0;
        }
      
         if (lAlarmHandle.intValue() < 0)//尚未布防,需要布防
         {
                if (fMSFCallBack == null)
                {
                    fMSFCallBack = new FMSGCallBack();
                    Pointer pUser = null;
                    if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V30(fMSFCallBack, pUser))
                    {
                        System.out.println("设置回调函数失败!");
                    }
                }
                HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
                m_strAlarmInfo.dwSize=m_strAlarmInfo.size();
                m_strAlarmInfo.byLevel=1;
                m_strAlarmInfo.byAlarmInfoType=0;
                m_strAlarmInfo.write();
                lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
                AlarmHandle = lAlarmHandle.longValue();
                if (lAlarmHandle.intValue() == -1)
                {
                    System.out.println("布防失败");
                    System.out.println("错误代码："+hCNetSDK.NET_DVR_GetLastError());
                    return AlarmHandle;
                }else{
                	System.out.println("布防成功");    
                	return AlarmHandle;
                }
          }else{
        	  System.out.println("已经布防，不要重复操作");
        	  
        	  return AlarmHandle;
          }
    }
    //撤防
    public void CloseAlarmChan() {
        //报警撤防
        if (lAlarmHandle.intValue() > -1)
        {
            if(hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle))
            {
                System.out.println("撤防成功");
                lAlarmHandle = new NativeLong(-1);
            }
        }
    }
    
    
    
    //开始监听
    public void StartAlarmListen() {
        int iListenPort = 8000;
        Pointer pUser = null;
//       hCNetSDK.NET_DVR_GetDVRConfig(lUserID, hCNetSDK.NET_DVR_SET_NETCFG_V30, "0XFFFFFFFF", hCNetSDK.NET_DVR_GET_NETCFG, dwOutBufferSize, lpBytesReturned);
        if (fMSFCallBack == null)
        {
             fMSFCallBack = new FMSGCallBack();
        }
		lListenHandle = hCNetSDK.NET_DVR_StartListen_V30(m_sDeviceIP, (short)iListenPort, fMSFCallBack, pUser);
        if(lListenHandle.intValue() < 0)
        {
            System.out.println("启动监听失败"+hCNetSDK.NET_DVR_GetLastError());
        }else{
             System.out.println("启动监听成功");
        }
    }

    //停止监听
    public void StopAlarmListen() {
        if(lListenHandle.intValue() < 0)
        {
            return;
        }

        if(!hCNetSDK.NET_DVR_StopListen_V30(lListenHandle))
        {
            System.out.println("停止监听失败");
            System.out.println("停止监听失败，错误："+hCNetSDK.NET_DVR_GetLastError());
        }else{
             
             System.out.println("停止监听成功");
        }
    }
    
   
    
    //测试抓图
    public int zhuaTu() {

        HCNetSDK.NET_DVR_SNAPCFG struSnapCfg = new HCNetSDK.NET_DVR_SNAPCFG();
        struSnapCfg.dwSize=struSnapCfg.size();
        struSnapCfg.bySnapTimes =1;
        struSnapCfg.wSnapWaitTime =1000;
        struSnapCfg.wIntervalTime[0] =1000; 
        //struSnapCfg.write();

        if (false == hCNetSDK.NET_DVR_ContinuousShoot(lUserID, struSnapCfg)){
            int iErr = hCNetSDK.NET_DVR_GetLastError();            
            System.out.println("网络触发失败，错误号：" + iErr);
            return 0;
        }else{
        	System.out.println("抓图成功！");
        	return 1;
        }

      
    }
	
}
