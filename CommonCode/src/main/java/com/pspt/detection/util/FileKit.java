package com.pspt.detection.util;

import com.pspt.detection.HikCache;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FileKit {
    private static final Logger logger = Logger.getLogger(FileKit.class);

    public static void main(String[] args) {
        FileKit.uploadImages("http://10.10.18.56/vehicle_manage/plate_recognition_record_add_api/", "10.10.18.106", "无车牌",
                "2018-11-20 15:44", "", new ArrayList<>(),
                "C:\\Users\\JHD\\IdeaProjects\\CarDetection\\images\\10.10.18.105_type1.jpg");
    }

    public static void byteArrayToFile(byte[] bfile, String fileURI) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        File dir = null;
        try {
            logger.info(fileURI.substring(0, fileURI.lastIndexOf("/")));
            dir = new File(fileURI.substring(0, fileURI.lastIndexOf("/") + 1).replace("/", "\\"));
            if (!dir.exists() && dir.isDirectory()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(fileURI);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

    /**
     * @param url
     * @param vehicle_plate 车牌号
     * @param record_time
     * @param device_ip
     * @param plateList
     * @param picture
     */
    public static void uploadImages(String url, String device_ip, String vehicle_plate, String record_time, String color, List<String> plateList, String picture) {
        logger.info("url:" + url + ", vehicle_plate:" + vehicle_plate + ", record_time:" + record_time + ", color:" + color
                + ", device_ip:" + device_ip + ", plateList:" + plateList + ", picture:" + picture);
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);// 建立HttpPost对象,改成自己的地址
            httpPost.addHeader("Authorization", HikCache.mHikBean.getAuthorization()); //认证token

            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
                    .addTextBody("vehicle_plate", vehicle_plate, ContentType.create("multipart/form-data", "UTF-8"))
                    .addTextBody("record_time", record_time, ContentType.create("multipart/form-data", "UTF-8"))
                    .addTextBody("device_ip", device_ip, ContentType.create("multipart/form-data", "UTF-8"))
                    .addTextBody("color", color, ContentType.create("multipart/form-data", "UTF-8"));
            if (plateList != null) {
                for (String filepath : plateList) {
                    File file = new File(filepath);
                    if (!file.exists()) {// 判断文件是否存在
                        logger.error("文件不存在");
                    }
                    FileBody bin = new FileBody(file, ContentType.create("image/png", "UTF-8"));// 创建图片提交主体信息
                    builder.addPart("plate", bin);// 添加到请求
                }
            }

            if (picture != null && !"".equals(picture)) {
                File file = new File(picture);
                if (!file.exists()) {// 判断文件是否存在
                    logger.error("文件不存在");
                }
                FileBody bin = new FileBody(file, ContentType.create("image/png", "UTF-8"));// 创建图片提交主体信息
                builder.addPart("picture", bin);// 添加到请求
            }
            HttpEntity entity = builder.build();

            httpPost.setEntity(entity);
            HttpResponse response = null;// 发送Post,并返回一个HttpResponse对象

            response = httpClient.execute(httpPost);
            logger.info("正在传送 vehicle_plate:" + vehicle_plate + ",device_ip:" + device_ip);
            if (response.getStatusLine().getStatusCode() == 200) {// 如果状态码为200,就是正常返回
                String result = EntityUtils.toString(response.getEntity());
                logger.info("发送成功 getStatusCode:" + result);
            }
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public static void uploadImages(String url, String device_ip, String start_time, String content, String filepath, String backgroundFilePath) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);// 建立HttpPost对象,改成自己的地址
            httpPost.addHeader("Authorization", HikCache.mHikBean.getAuthorization()); //认证token
            FileBody bin = null;
            if (filepath != null) {
                File file = new File(filepath);
                if (!file.exists()) {// 判断文件是否存在
                    logger.error("文件不存在");
                }
                bin = new FileBody(file, ContentType.create("image/png", "UTF-8"));// 创建图片提交主体信息
            }

            File backgroundFile = new File(backgroundFilePath);
            if (!backgroundFile.exists()) {// 判断文件是否存在
                logger.error("文件不存在");
            }
            FileBody backgroundBin = new FileBody(backgroundFile, ContentType.create("image/png", "UTF-8"));// 创建图片提交主体信息

            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setCharset(Charset.forName("utf-8"))
                    .addPart("snapshot", backgroundBin)// 添加到请求
                    .addTextBody("device_ip", device_ip, ContentType.create("multipart/form-data", "UTF-8"))
                    .addTextBody("start_time", start_time, ContentType.create("multipart/form-data", "UTF-8"))
                    .addTextBody("content", content, ContentType.create("multipart/form-data", "UTF-8"));
            if (bin != null) {
                builder.addPart("file", bin);// 添加到请求
            }
            HttpEntity entity = builder.build();

            httpPost.setEntity(entity);
            HttpResponse response = null;// 发送Post,并返回一个HttpResponse对象

            response = httpClient.execute(httpPost);
            logger.info("正在传送 filepath:" + filepath + ",device_ip:" + device_ip);
            if (response.getStatusLine().getStatusCode() == 200) {// 如果状态码为200,就是正常返回
                String result = EntityUtils.toString(response.getEntity());
                logger.info(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties p = null;

    public static String getConfig(String configFilePath, String str) {
        if (p == null) {
            p = new Properties();
        }
        try {
            p.load(new BufferedInputStream(new FileInputStream(configFilePath)));
            return p.getProperty(str).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
