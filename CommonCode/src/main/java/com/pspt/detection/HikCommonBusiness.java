package com.pspt.detection;

import com.sun.jna.NativeLong;

public class HikCommonBusiness {
    public static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    public HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();// 设备信息
    public String mDeviceIP;

    public NativeLong lUserID;// 用户句柄
    public NativeLong lAlarmHandle;// 报警布防句柄
    public NativeLong lListenHandle;// 报警监听句柄

    public static HCNetSDK.FMSGCallBack fMSFCallBack;// 报警回调函数实现
    public static HCNetSDK.FMSGCallBack_V31 fMSFCallBack_V31;// 报警回调函数实现

    public HikBean mHikBean;
    public int mPort;
    public String mUsername;
    public String mPassword;

    public boolean init(String ip, HikBean hikBean) {
        this.mHikBean = hikBean;
        mPort = mHikBean.getPort();
        mUsername = mHikBean.getUsername();
        mPassword = mHikBean.getPassword();
        mDeviceIP = ip;
        lUserID = new NativeLong(-1);
        lAlarmHandle = new NativeLong(-1);

        boolean initSuc = hCNetSDK.NET_DVR_Init();
        if (initSuc != true) {
            return false;
        }
        // 设置连接时间与重连时间
        hCNetSDK.NET_DVR_SetConnectTime(2000, 1);
        hCNetSDK.NET_DVR_SetReconnect(10000, true);

        return true;
    }
}
