package com.example.demo.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.demo.entity.HCNetSDK;
import com.example.demo.entity.HCNetSDK.NET_DVR_ALARMER;
import com.example.demo.entity.HCNetSDK.RECV_ALARM;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class FMSGCallBack implements HCNetSDK.FMSGCallBack{

	@Override
	public void invoke(NativeLong lCommand, NET_DVR_ALARMER pAlarmer, RECV_ALARM pAlarmInfo, int dwBufLen,
			Pointer pUser) {
		// TODO Auto-generated method stub
		String sAlarmType = new String();
        //DefaultTableModel alarmTableModel = ((DefaultTableModel) jTableAlarm.getModel());//获取表格模型
        String[] newRow = new String[3];
        //报警时间
        Date today = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String[] sIP = new String[2];

        sAlarmType = new String("lCommand=") + lCommand.intValue();
        //lCommand是传的报警类型
        System.out.println(sAlarmType);
        switch (lCommand.intValue())
        {
        	case HCNetSDK.COMM_ALARM_RULE:
        		HCNetSDK.NET_VCA_RULE_ALARM rAlarm =new HCNetSDK.NET_VCA_RULE_ALARM();
        		rAlarm.write();
        		Pointer pInfo = rAlarm.getPointer();
         		rAlarm.read();
         		System.out.println("dwRelativeTime"+rAlarm.dwRelativeTime);
         		System.out.println("dwAbsTime"+rAlarm.dwAbsTime);
         		System.out.println(rAlarm.struTargetInfo.struRect.fHeight);
         		
         		System.out.println("dwPicDataLen"+rAlarm.dwPicDataLen);
         		System.out.println("pImage"+rAlarm.pImage);
         		
         		
         		
            case HCNetSDK.COMM_ALARM_V30:                    
                HCNetSDK.NET_DVR_ALARMINFO_V30 strAlarmInfoV30 = new HCNetSDK.NET_DVR_ALARMINFO_V30();
                strAlarmInfoV30.write();
                Pointer pInfoV30 = strAlarmInfoV30.getPointer();
                //pInfoV30.write(0, pAlarmInfo.getByteArray(0, strAlarmInfoV30.size()), 0, strAlarmInfoV30.size());
                strAlarmInfoV30.read();
                switch (strAlarmInfoV30.dwAlarmType)
                {
                    case 0:
                        sAlarmType = sAlarmType + new String("：信号量报警") + "，"+ "报警输入口：" + (strAlarmInfoV30.dwAlarmInputNumber+1);
                        break;
                    case 1:
                        sAlarmType = sAlarmType + new String("：硬盘满");
                        break;
                    case 2:
                        sAlarmType = sAlarmType + new String("：信号丢失");
                        break;
                    case 3:
                        sAlarmType = sAlarmType + new String("：移动侦测") + "，"+ "报警通道：";
                         for (int i=0; i<64; i++) 
                         {
                            if (strAlarmInfoV30.byChannel[i] == 1)
                            {
                               sAlarmType=sAlarmType + "ch"+(i+1)+" ";
                           }
                        }
                        break;
                    case 4:
                        sAlarmType = sAlarmType + new String("：硬盘未格式化");
                        break;
                    case 5:
                        sAlarmType = sAlarmType + new String("：读写硬盘出错");
                        break;
                    case 6:
                        sAlarmType = sAlarmType + new String("：遮挡报警");
                        break;
                    case 7:
                        sAlarmType = sAlarmType + new String("：制式不匹配");
                        break;
                    case 8:
                        sAlarmType = sAlarmType + new String("：非法访问");
                        break;
                }
                newRow[0] = dateFormat.format(today);
                //报警类型
                newRow[1] = sAlarmType;
                //报警设备IP地址
                sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                newRow[2] = sIP[0];
                //alarmTableModel.insertRow(0, newRow);
                break;
           
            case HCNetSDK.COMM_UPLOAD_PLATE_RESULT:
                HCNetSDK.NET_DVR_PLATE_RESULT strPlateResult = new HCNetSDK.NET_DVR_PLATE_RESULT();
                strPlateResult.write();
                Pointer pPlateInfo = strPlateResult.getPointer();
                //pPlateInfo.write(0, pAlarmInfo.getByteArray(0, strPlateResult.size()), 0, strPlateResult.size());
                strPlateResult.read();
                try {
                    String srt3=new String(strPlateResult.struPlateInfo.sLicense,"GBK");
                    sAlarmType = sAlarmType + "：交通抓拍上传，车牌："+ srt3;
                }
                 catch (UnsupportedEncodingException e1) {
                     e1.printStackTrace();
                 } catch (IOException e) {
					e.printStackTrace();
                 }

                newRow[0] = dateFormat.format(today);
                //报警类型
                newRow[1] = sAlarmType;
                //报警设备IP地址
                sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                newRow[2] = sIP[0];
                break;
            case HCNetSDK.COMM_ITS_PLATE_RESULT:
            	
            	HCNetSDK.NET_ITS_PLATE_RESULT strItsPlateResult = new HCNetSDK.NET_ITS_PLATE_RESULT();
                strItsPlateResult.write();
                Pointer pItsPlateInfo = strItsPlateResult.getPointer();
                //pItsPlateInfo.write(0, pAlarmInfo.getByteArray(0, strItsPlateResult.size()), 0, strItsPlateResult.size());
                strItsPlateResult.read();
              
			String srt3;
			try {
				srt3 = new String(strItsPlateResult.struPlateInfo.sLicense,"GBK").trim().substring(1);
				System.out.println("车牌号:"+srt3);

				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
                break;
           
            default:
                newRow[0] = dateFormat.format(today);
                //报警类型
                newRow[1] = sAlarmType;
                //报警设备IP地址
                sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                newRow[2] = sIP[0];
                //alarmTableModel.insertRow(0, newRow);
                System.out.println("未找到匹配模式");
                break;
        }
        
       
	}

	
	
}
