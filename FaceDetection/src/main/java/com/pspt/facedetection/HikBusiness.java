package com.pspt.facedetection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.pspt.detection.HCNetSDK;
import com.pspt.detection.HikCommonBusiness;
import com.pspt.detection.util.FileKit;
import org.apache.log4j.Logger;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class HikBusiness extends HikCommonBusiness {
    private static final Logger logger = Logger.getLogger(HikBusiness.class);

    public long login() {// GEN-FIRST:event_jButtonLoginActionPerformed
        // 注册
        lUserID = hCNetSDK.NET_DVR_Login_V30(mDeviceIP, (short) mPort, mUsername, mPassword, m_strDeviceInfo);
        return lUserID.longValue();
    }

    public boolean logout() {
        // 注销
        return hCNetSDK.NET_DVR_Logout(lUserID);
    }

    public boolean cleanup() {// GEN-FIRST:event_exitMenuItemMouseClicked
        return hCNetSDK.NET_DVR_Cleanup();
    }

    public boolean setDVRMessageCallBack() {
        Pointer pUser = null;
        fMSFCallBack_V31 = new FMSGCallBack_V31();
        return hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser);
    }

    public int setupAlarmChan() {
        HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
        m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
        m_strAlarmInfo.byFaceAlarmDetection = 0;// 人脸侦测报警，设备支持人脸侦测功能的前提下，上传COMM_ALARM_FACE_DETECTION类型报警信息
        m_strAlarmInfo.write();
        lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
        return lAlarmHandle.intValue();
    }

    public boolean closeAlarmChan() {
        return hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle);
    }

    public void alarmDataHandle(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo,
                                int dwBufLen, Pointer pUser) {
        String sAlarmType = new String();
        String[] newRow = new String[3];
        // 报警时间
        String[] sIP = new String[2];

        newRow[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 报警类型
        newRow[1] = sAlarmType;
        // 报警设备IP地址
        sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
        newRow[2] = sIP[0];

        sAlarmType = new String("lCommand=") + lCommand.intValue();
        // lCommand是传的报警类型
        switch (lCommand.intValue()) {
            case HCNetSDK.COMM_ALARM_FACE_DETECTION: // 人脸侦测报警信息
                HCNetSDK.NET_DVR_FACE_DETECTION struFaceDetectionAlarm = new HCNetSDK.NET_DVR_FACE_DETECTION();
                struFaceDetectionAlarm.write();
                Pointer pPlateInfo = struFaceDetectionAlarm.getPointer();
                pPlateInfo.write(0, pAlarmInfo.getByteArray(0, struFaceDetectionAlarm.size()), 0,
                        struFaceDetectionAlarm.size());
                struFaceDetectionAlarm.read();

                String filename = "images/face_" + newRow[2] + "_type" + struFaceDetectionAlarm.byUploadEventDataType
                        + ".jpg";

                // 保存抓拍场景图片
                if (struFaceDetectionAlarm.dwBackgroundPicLen > 0 && struFaceDetectionAlarm.pBackgroundPicpBuffer != null) {
                    FileOutputStream fout;
                    try {
                        fout = new FileOutputStream(filename);
                        // 将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = struFaceDetectionAlarm.pBackgroundPicpBuffer.getByteBuffer(offset,
                                struFaceDetectionAlarm.dwBackgroundPicLen);
                        byte[] bytes = new byte[struFaceDetectionAlarm.dwBackgroundPicLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        fout.write(bytes);
                        fout.close();

                        FileKit.uploadImages(mHikBean.getUploadUrl(), sIP[0], newRow[0], "人脸侦测头像信息", null, filename);
                    } catch (FileNotFoundException e) {
                        logger.error("", e);
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }

                logger.info("人脸侦测报警信息【" + newRow[0] + "】" + sIP[0] + "|" + sAlarmType);
                break;
            case HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT: // 人脸抓拍报警信息
                HCNetSDK.NET_VCA_FACESNAP_RESULT struNetVCAFaceSnapResult = new HCNetSDK.NET_VCA_FACESNAP_RESULT();
                struNetVCAFaceSnapResult.write();
                Pointer pPlateInfo1 = struNetVCAFaceSnapResult.getPointer();
                pPlateInfo1.write(0, pAlarmInfo.getByteArray(0, struNetVCAFaceSnapResult.size()), 0,
                        struNetVCAFaceSnapResult.size());
                struNetVCAFaceSnapResult.read();
                logger.info("人脸抓拍报警信息【" + newRow[0] + "】" + newRow[2] + "|" + sAlarmType);
                MainProgress.frame.insertText("人脸抓拍报警信息【" + newRow[0] + "】" + newRow[2] + "|" + sAlarmType);

                // 保存抓拍场景图片
                if (struNetVCAFaceSnapResult.dwFacePicLen > 0 && struNetVCAFaceSnapResult.pBuffer2 != null) {
                    FileOutputStream fout = null;
                    try {
                        String saveFileName = "images/face_" + newRow[2] + "_type"
                                + struNetVCAFaceSnapResult.byUploadEventDataType + ".jpg";
                        fout = new FileOutputStream(saveFileName);
                        // 将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = struNetVCAFaceSnapResult.pBuffer1.getByteBuffer(offset,
                                struNetVCAFaceSnapResult.dwFacePicLen);
                        byte[] bytes = new byte[struNetVCAFaceSnapResult.dwFacePicLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        fout.write(bytes);
                        fout.close();

                        String saveFileNameBackGroup = "images/face_" + newRow[2] + "_type"
                                + struNetVCAFaceSnapResult.byUploadEventDataType + "_bg.jpg";
                        fout = new FileOutputStream(saveFileNameBackGroup);
                        // 将字节写入文件
                        offset = 0;
                        buffers = struNetVCAFaceSnapResult.pBuffer2.getByteBuffer(offset,
                                struNetVCAFaceSnapResult.dwBackgroundPicLen);
                        bytes = new byte[struNetVCAFaceSnapResult.dwBackgroundPicLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        fout.write(bytes);
                        fout.close();
                        FileKit.uploadImages(mHikBean.getUploadUrl(), sIP[0], newRow[0], "人脸抓拍头像信息", saveFileName,
                                saveFileNameBackGroup);

                    } catch (FileNotFoundException e) {
                        logger.error("", e);
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
                break;
        }
    }

    public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {
        // 报警信息回调函数
        public boolean invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen,
                              Pointer pUser) {
            alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
            return true;
        }
    }

    public class FMSGCallBack implements HCNetSDK.FMSGCallBack {
        // 报警信息回调函数
        public void invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen,
                           Pointer pUser) {
            alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        }
    }
}
