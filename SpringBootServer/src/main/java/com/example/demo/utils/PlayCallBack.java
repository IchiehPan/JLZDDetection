package com.example.demo.utils;

import com.example.demo.entity.HCNetSDK;
import com.example.demo.entity.PlayCtrl;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.W32API.HWND;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.NativeLongByReference;

public class PlayCallBack implements HCNetSDK.FRealDataCallBack_V30 {

	int iPicNum = 0;// Set channel NO.
	NativeLongByReference nPort = new NativeLongByReference();
	PlayCtrl playCtrl = PlayCtrl.INSTANCE;
	HWND hWnd = null;
	@SuppressWarnings("unused")
	private String ip=new String("");
	
    public PlayCallBack(String ip) {
		this.ip = ip;
	}
	
	public PlayCallBack() {
		
	}
	
	@Override
	public void invoke(NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
		// TODO Auto-generated method stub
        
		switch (dwDataType) {
		case HCNetSDK.NET_DVR_SYSHEAD: // 系统头
			if (!playCtrl.PlayM4_GetPort(nPort)) // 获取播放库未使用的通道号
			{
				System.out.println("error1");
				break;
			}
			if (dwBufSize > 0) {
				if (!playCtrl.PlayM4_OpenStream(nPort.getValue(), pBuffer, dwBufSize, 1024 * 1024)) {
					System.out.println("error2");
					break;
				}
				// 设置解码回调函数 只解码不显示
				if (!playCtrl.PlayM4_SetDecCallBack(nPort.getValue(), new FdecCallBack(ip))) {
					System.out.println("error3");
					break;
				}

				if (!playCtrl.PlayM4_SetDecCBStream(nPort.getValue(), 1)) {
					System.out.println("error4");
					break;

				}

				// 设置解码回调函数 解码且显示
				/*
				 * if (!playCtrl.PlayM4_SetDecCallBackEx(nPort.getValue(),new
				 * FdecCallBack(),null,null)) {
				 * 
				 * break; }
				 */
				// 打开视频解码
				if (!playCtrl.PlayM4_Play(nPort.getValue(), hWnd)) {
					System.out.println("error5");
					break;
				}

				// 打开音频解码, 需要码流是复合流
				/*
				 * if (!playCtrl.PlayM4_PlaySound(nPort.getValue())) {
				 * System.out.println("error6"); break; }
				 */
			}
			break;

		case HCNetSDK.NET_DVR_STREAMDATA: // 码流数据
			if (dwBufSize > 0 && nPort.getValue().intValue() != -1) {

				boolean inData = playCtrl.PlayM4_InputData(nPort.getValue(), pBuffer, dwBufSize);
				while (!inData) {

					try {
						Thread.sleep(10);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					inData = playCtrl.PlayM4_InputData(nPort.getValue(), pBuffer, dwBufSize);
					System.out.println("PlayM4_InputData success \n");
				}
			}
			break;

		default:
			if (dwBufSize > 0 && nPort.getValue().intValue() != -1) {

				boolean inData = playCtrl.PlayM4_InputData(nPort.getValue(), pBuffer, dwBufSize);
				while (!inData) {

					try {
						Thread.sleep(10);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					inData = playCtrl.PlayM4_InputData(nPort.getValue(), pBuffer, dwBufSize);
					System.out.println("PlayM4_InputData failed \n");
				}
			}
			break;
		}
	}

}
