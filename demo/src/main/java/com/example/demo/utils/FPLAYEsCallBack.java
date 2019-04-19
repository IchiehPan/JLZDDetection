package com.example.demo.utils;



import com.example.demo.entity.HCNetSDK;
import com.example.demo.entity.HCNetSDK.NET_DVR_PACKET_INFO_EX;
import com.example.demo.entity.PlayCtrl;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class FPLAYEsCallBack implements HCNetSDK.FPlayESCallBack {
	
	@Override
	public void invoke(NativeLong lPreviewHandle, NET_DVR_PACKET_INFO_EX pstruPackInfo, Pointer pUser) {
		// TODO Auto-generated method stub
		
		
		byte[] str = pstruPackInfo.dwPacketBuffer.getPointer().getByteArray(0, pstruPackInfo.dwPacketSize);
	    for (int i = 0; i < str.length; i++) {
			System.out.print(str[i]+"");
		}
	}
	
	

}
