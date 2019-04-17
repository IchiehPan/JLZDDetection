package com.pspt.cardetection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pspt.detection.HCNetSDK;
import com.pspt.detection.HikBean;
import com.pspt.detection.HikCommonBusiness;
import com.pspt.detection.util.ByteKit;
import com.pspt.detection.util.FileKit;
import com.sun.jna.NativeLong;
//7aport com.sun.jna.Pointer;7a
import com.sun.jna.Pointer;
import org.apache.log4j.Logger;

public class HikBusiness extends HikCommonBusiness {
    private static final Logger logger = Logger.getLogger(HikBusiness.class);

    public boolean login() {// GEN-FIRST:event_jButtonLoginActionPerformed
        // 注册
        lUserID = hCNetSDK.NET_DVR_Login_V30(mDeviceIP, (short) mPort, mUsername, mPassword, m_strDeviceInfo);
        if (lUserID.longValue() < 0) {
            getLastError("NET_DVR_Login_V30");
            cleanup();
            return false;
        }

        return true;
    }

    public boolean setDVRMessageCallBack() {
        Pointer pUser = null;
        if (fMSFCallBack_V31 == null) {
            fMSFCallBack_V31 = new FMSGCallBack_V31();// 报警回调函数实现
        }
        return hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser);
    }

    public boolean setupAlarmChan() {
        HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
        m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
        m_strAlarmInfo.byLevel = 1; //布防优先级：0- 一等级（高），1- 二等级（中）
        m_strAlarmInfo.byAlarmInfoType = 1; //上传报警信息类型: 0- 老报警信息(NET_DVR_PLATE_RESULT), 1- 新报警信息(NET_ITS_PLATE_RESULT)
        m_strAlarmInfo.write();
        lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
        if (lAlarmHandle.intValue() < 0) {
            getLastError("NET_DVR_SetupAlarmChan_V41");
            logout(lUserID);
            cleanup();
            return false;
        }
        return true;
    }

    public void getLastError(String methodName) {
        int iErr = hCNetSDK.NET_DVR_GetLastError();
        logger.error(methodName + " failed, error code: " + iErr);
    }

    public void continuesShoot() {
        // ---------------------------------------
        // 网络触发抓拍

        HCNetSDK.NET_DVR_SNAPCFG struSnapCfg = new HCNetSDK.NET_DVR_SNAPCFG();

        //结构体大小
        struSnapCfg.dwSize = struSnapCfg.size();

        //线圈抓拍次数，0-不抓拍，非0-连拍次数，目前最大5次
        struSnapCfg.bySnapTimes = 3;

        //抓拍等待时间，单位ms，取值范围[0,60000]
        struSnapCfg.wSnapWaitTime = 1000;

        //连拍间隔时间，单位ms，取值范围[67,60000]
        struSnapCfg.wIntervalTime[0] = 1000;
        struSnapCfg.wIntervalTime[1] = 1000;

        //触发IO关联的车道号，取值范围[0,9]
        struSnapCfg.byRelatedDriveWay = 0;

        struSnapCfg.write();

        if (false == hCNetSDK.NET_DVR_ContinuousShoot(lUserID, struSnapCfg)) {
            getLastError("NET_DVR_ContinuousShoot");
        }

        logger.info("触发成功，ip：" + mDeviceIP);

    }

    public boolean closeAlarmChan() {
        // 停止监听
        if (!hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle)) {
            getLastError("closeAlarmChan");
            logout(lUserID);
            cleanup();
            return false;
        }
        return true;
    }

    public boolean logout(NativeLong lUserID) {
        // 注销
        return hCNetSDK.NET_DVR_Logout(lUserID);
    }

    public boolean cleanup() {
        return hCNetSDK.NET_DVR_Cleanup();
    }

    public boolean reboot(NativeLong lUserID) {
        return hCNetSDK.NET_DVR_RebootDVR(lUserID);
    }

    public void msgDataHandle(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        String sAlarmType = new String();
        String[] newRow = new String[3];
        // 报警时间
        Date today = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String[] sIP = new String[2];

        sAlarmType = new String("lCommand=") + lCommand.intValue();
        // lCommand是传的报警类型
        switch (lCommand.intValue()) {
            case HCNetSDK.COMM_ITS_PLATE_RESULT:
                HCNetSDK.NET_ITS_PLATE_RESULT strItsPlateResult = new HCNetSDK.NET_ITS_PLATE_RESULT();
                strItsPlateResult.write();
                Pointer pItsPlateInfo = strItsPlateResult.getPointer();
                pItsPlateInfo.write(0, pAlarmInfo.getByteArray(0, strItsPlateResult.size()), 0, strItsPlateResult.size());
                strItsPlateResult.read();

                byte[] sLicense = strItsPlateResult.struPlateInfo.sLicense;
                byte[] byBelieve = strItsPlateResult.struPlateInfo.byBelieve;

                String realLiscense = "";
                String color = "";
                try {
                    realLiscense = new String(ByteKit.byteArrayLink(sLicense, byBelieve), "GBK");
                    if (!"无车牌".equals(realLiscense)) {
                        color = realLiscense.substring(0, 1);
                        realLiscense = realLiscense.substring(1);
                    }

                    sAlarmType = sAlarmType + ",车辆类型：" + strItsPlateResult.byVehicleType + ",交通抓拍上传，顏色：" + color + ",车牌："
                            + realLiscense;
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }

                newRow[0] = dateFormat.format(today);
                // 报警类型
                newRow[1] = sAlarmType;
                // 报警设备IP地址
                sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                newRow[2] = sIP[0];

                String filename = "";
                String picture = "";
                List<String> plateList = new ArrayList<>();

                int iNum = 0;
                for (int i = 0; i < strItsPlateResult.dwPicNum; i++) {
                    logger.info("车牌号: " + realLiscense);

                    try {

                        //保存场景图
                        if ((strItsPlateResult.struPicInfo[i].dwDataLen != 0) && (strItsPlateResult.struPicInfo[i].byType == 1) || (strItsPlateResult.struPicInfo[i].byType == 2)) {
                            filename = "images/" + sIP[0] + "_type" + strItsPlateResult.struPicInfo[i].byType + ".jpg";
                            picture = new String(filename);
                        }

                        //车牌小图片
                        if ((strItsPlateResult.struPicInfo[i].dwDataLen != 0) && (strItsPlateResult.struPicInfo[i].byType == 0)) {
                            filename = "images/" + sIP[0] + "_type" + strItsPlateResult.struPicInfo[i].byType + "_small.jpg";
                        }

                        FileOutputStream fos = new FileOutputStream(filename);
                        // 将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = strItsPlateResult.struPicInfo[i].pBuffer.getByteBuffer(offset,
                                strItsPlateResult.struPicInfo[i].dwDataLen);
                        byte[] bytes = new byte[strItsPlateResult.struPicInfo[i].dwDataLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        fos.write(bytes);
                        fos.close();
                        iNum++;


                    } catch (FileNotFoundException e) {
                        logger.error(e);
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }

                logger.info("准备发送照片");
                if (!"".equals(picture)) {
                    logger.info("发送照片中");
                    FileKit.uploadImages(mHikBean.getUploadUrl(), sIP[0], realLiscense, newRow[0], color, plateList, picture);
                    logger.info("发送照片完成");
                }

                break;
//            case HCNetSDK.COMM_ITS_PLATE_RESULT:
//                HCNetSDK.NET_ITS_PLATE_RESULT strItsPlateResult = new HCNetSDK.NET_ITS_PLATE_RESULT();
//                strItsPlateResult.write();
//                Pointer pItsPlateInfo = strItsPlateResult.getPointer();
//                pItsPlateInfo.write(0, pAlarmInfo.getByteArray(0, strItsPlateResult.size()), 0, strItsPlateResult.size());
//                strItsPlateResult.read();
//
//                byte[] sLicense = strItsPlateResult.struPlateInfo.sLicense;
//                byte[] byBelieve = strItsPlateResult.struPlateInfo.byBelieve;
//
//                System.out.println(strItsPlateResult.struPlateInfo.byColor);
//
//                String realLiscense = "";
//                String color = "";
//                try {
//                    realLiscense = new String(ByteKit.byteArrayLink(sLicense, byBelieve), "GBK");
//                    if (!"无车牌".equals(realLiscense)) {
//                        color = realLiscense.substring(0, 1);
//                        realLiscense = realLiscense.substring(1);
//                    }
//
//                    sAlarmType = sAlarmType + ",车辆类型：" + strItsPlateResult.byVehicleType + ",交通抓拍上传，顏色：" + color + ",车牌："
//                            + realLiscense;
//                } catch (UnsupportedEncodingException e1) {
//                    e1.printStackTrace();
//                }
//
//                newRow[0] = dateFormat.format(today);
//                // 报警类型
//                newRow[1] = sAlarmType;
//                // 报警设备IP地址
//                sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
//                newRow[2] = sIP[0];
//
//                String filename = "";
//                String picture = "";
//                List<String> plateList = new ArrayList<>();
//
//                for (int i = 0; i < strItsPlateResult.dwPicNum; i++) {
//                    if (i < strItsPlateResult.struPicInfo.length && strItsPlateResult.struPicInfo[i].dwDataLen > 0) {
//                        FileOutputStream fout = null;
//                        try {
//                            filename = "images/" + sIP[0] + "_type" + strItsPlateResult.struPicInfo[i].byType + ".jpg";
//                            if (strItsPlateResult.struPicInfo[i].byType == 1) {
//                                picture = filename;
//                            } else {
//                                plateList.add(filename);
//                            }
//                            fout = new FileOutputStream(filename);
//                            // 将字节写入文件
//                            long offset = 0;
//                            ByteBuffer buffers = strItsPlateResult.struPicInfo[i].pBuffer.getByteBuffer(offset,
//                                    strItsPlateResult.struPicInfo[i].dwDataLen);
//                            byte[] bytes = new byte[strItsPlateResult.struPicInfo[i].dwDataLen];
//                            buffers.rewind();
//                            buffers.get(bytes);
//                            fout.write(bytes);
//                            fout.close();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                logger.info("接收报警， 预测车牌为：" + realLiscense + ", id: " + sIP[0]);
//                if (!"无车牌".equals(realLiscense) && !"".equals(picture)) {
//                    FileKit.uploadImages(mHikBean.getUploadUrl(), realLiscense, newRow[0], color, sIP[0], plateList,
//                            picture);
//                }
//
//                break;
        }
    }

    public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {
        // 报警信息回调函数
        public boolean invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
            msgDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
            return true;
        }
    }
}
