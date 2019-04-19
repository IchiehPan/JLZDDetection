package com.example.demo.utils;

import org.springframework.web.socket.TextMessage;

import com.example.demo.entity.HCNetSDK;
import com.example.demo.entity.PlayCtrl;
import com.example.demo.service.MyHandler;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import com.sun.jna.ptr.ByteByReference;




public class FPlayCallBack implements HCNetSDK.FRealDataCallBack_V30 {

	private HCNetSDK hcNetInter;
	private NativeLong lPlayHandle;
	private boolean loading = false;

	@Override
	public void invoke(NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
		// TODO Auto-generated method stub
		/**
		 * 让 loading 一直保持 为true 说明 还有码流数据 在回传
		 */
		loading = true;
		PlayCtrl playCtrl = PlayCtrl.INSTANCE;
		lPlayHandle=lRealHandle;
		Pointer pointer = null;
		switch (dwDataType) {

		case HCNetSDK.NET_DVR_SYSHEAD: // 系统头数据
			if (dwBufSize > 0) {
				pointer = pBuffer.getPointer();
				try {
					if (pointer != null) {
						try {
							byte[] headByte = pointer.getByteArray(0, dwBufSize);
							System.out.println(bytesToHexString(headByte));
							
						} catch (NullPointerException e) {
							loading = false;
							e.printStackTrace();
							hcNetInter.NET_DVR_StopPlayBack(lPlayHandle);

							System.out.println("关闭连接");
							return;
						}
					}
				} catch (Exception e) {
					loading = false;
					/*hcNetInter.NET_DVR_StopPlayBack(lPlayHandle);*/
					e.printStackTrace();
				}
			}
			break;

		case HCNetSDK.NET_DVR_STREAMDATA: // 码流数据2
			if (dwBufSize > 0) {
				pointer = pBuffer.getPointer();
				try {
					if (pointer != null) {
						try {
							byte[] dataByte = pointer.getByteArray(0, dwBufSize);
							/*System.out.println(bytesToHexString(dataByte));*/
							byte[] h264byte = new byte[dataByte.length];
							int h264byleLength = 0;
							
							boolean isH264 = GetH246FromPS(dataByte,dataByte.length,h264byte,h264byleLength);
                            if(isH264) {
                            
                            MyHandler.sendMessageToUser("192.168.8.65", new TextMessage(bytesToHexString(dataByte)));
                           	/*System.out.println("-------------");*/
                            }

						} catch (NullPointerException e) {
							loading = false;
							e.printStackTrace();
						/*	hcNetInter.NET_DVR_StopPlayBack(lPlayHandle);*/

							System.out.println("关闭连接");
							return;
						}
					}
				} catch (Exception e) {
					loading = false;
					/*hcNetInter.NET_DVR_StopPlayBack(lPlayHandle);*/
					e.printStackTrace();
				}
			}
			break;

		case HCNetSDK.NET_DVR_AUDIOSTREAMDATA: // 码流数据
			if (dwBufSize > 0) {
				pointer = pBuffer.getPointer();
				try {
					if (pointer != null) {
						try {
							byte[] dataByte = pointer.getByteArray(0, dwBufSize);
							byte[] h264byte = new byte[dataByte.length];
							int h264byleLength = 0;
							
							boolean isH264 = GetH246FromPS(dataByte,dataByte.length,h264byte,h264byleLength);
                            if(isH264) {
                            
                            //MyHandler.sendMessageToUser("192.168.8.65", new TextMessage(bytesToHexString(h264byte)));
                           	/*System.out.println("-------------");*/
                            }

						} catch (NullPointerException e) {
							loading = false;
							e.printStackTrace();
							hcNetInter.NET_DVR_StopPlayBack(lPlayHandle);

							System.out.println("关闭连接");
							return;
						}
					}
				} catch (Exception e) {
					loading = false;
					hcNetInter.NET_DVR_StopPlayBack(lPlayHandle);
					e.printStackTrace();
				}
			}
			break;

		case HCNetSDK.NET_DVR_REALPLAYEXCEPTION: // 预览异常
			loading = false;
			break;
		case HCNetSDK.NET_DVR_REALPLAYNETCLOSE: // 预览时连接断开
			loading = false;
			break;
		case HCNetSDK.NET_DVR_REALPLAY5SNODATA: // 预览5s没有收到数据
			loading = false;
			break;
		case HCNetSDK.NET_DVR_PLAYBACKEXCEPTION: // 回放异常
			loading = false;
			break;
		case HCNetSDK.NET_DVR_PLAYBACKNETCLOSE: // 回放时候连接断开
			loading = false;
			break;
		case HCNetSDK.NET_DVR_PLAYBACK5SNODATA: // 回放5s没有收到数据
			loading = false;
			break;
		}
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	public static String bytesToHexString(byte[] src){   
	    StringBuilder stringBuilder = new StringBuilder("");   
	    if (src == null || src.length <= 0) {   
	        return null;   
	    }   
	    for (int i = 0; i < src.length; i++) {   
	        int v = src[i] & 0xFF;   
	        String hv = Integer.toHexString(v);   
	        if (hv.length() < 2) {   
	            stringBuilder.append(0);   
	        }   
	        stringBuilder.append(hv);   
	    }   
	    return stringBuilder.toString();   
	}   
	
	public static byte[] toBytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

	public boolean GetH246FromPS(byte[] pBuffer, int nBufLenth , byte[] pH264 ,int nH264Lenth)
	{
		
	    if (pBuffer==null || nBufLenth <= 0)
	    {
	    	
	        return false;
	    }

	    byte[] pH264Buffer = new byte[pBuffer.length+10];
	    int nHerderLen = 0;
	    
	    if (pBuffer!=null
	        && pBuffer[0]== (byte)0x00
	        && pBuffer[1] == (byte)0x00
	        && pBuffer[2] == (byte)0x01
	        && pBuffer[3] == (byte)0xE0)//E==视频数据(此处E0标识为视频)
	    {
	    	
	        nHerderLen = 9 + pBuffer[8];//9个为固定的数据包头长度，pBuffer[8]为填充头部分的长度
	        pH264Buffer[pBuffer.length+1] = (byte)nHerderLen;
	        pH264Buffer = pBuffer;
	       
	       
	        if (pH264!=null &&pH264Buffer!=null && (nBufLenth - nHerderLen)>0)
	        {
	            
	            System.arraycopy(pH264Buffer, 0, pH264, 0, (nBufLenth - nHerderLen));
	        }
	        nH264Lenth = nBufLenth - nHerderLen;

	        return true;
	    }
	    else if (pBuffer!=null
	        && pBuffer[0] == (byte)0x00
	        && pBuffer[1] == (byte)0x00
	        && pBuffer[2] == (byte)0x01
	        && pBuffer[3] == (byte)0xC0) //C==音频数据？
	    {
	        pH264 = null;
	        nH264Lenth = 0;
	       
	    }
	    else if (pBuffer!=null
	        && pBuffer[0] == (byte)0x00
	        && pBuffer[1] == (byte)0x00
	        && pBuffer[2] == (byte)0x01
	        && pBuffer[3] == (byte)0xBA)//视频流数据包 包头
	    {
	     
	        pH264 = null;
	        nH264Lenth = 0;
	        return false;
	    }
	    return false;
	}
	
}
