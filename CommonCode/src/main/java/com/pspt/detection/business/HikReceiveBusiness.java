package com.pspt.detection.business;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HikReceiveBusiness {

    public String receiveJson(BufferedReader bufferedReader) throws IOException {
        StringBuffer jsonString = new StringBuffer();
        String tempString = null;
        jsonString.append("{");
        while (!(tempString = bufferedReader.readLine()).equals("{")) {
        }
        while (!(tempString = bufferedReader.readLine()).equals("}")) {
            // json内容
            jsonString.append(tempString + "\n");
        }
        jsonString.append("}");
        System.out.println("jsonString=" + jsonString);
        return jsonString.toString();
    }

    public File receiveImage(BufferedReader bufferedReader) throws IOException {
        // 处理图片
        String tempString = null;
        Pattern pattern = null;
        Matcher matcher = null;

        String suffix = null;
        String imageName = null;
        long imageLength = 0;
        List<Byte> byteList = new ArrayList<>();
        boolean imageByte = false;

        while ((tempString = bufferedReader.readLine()) != null) {
            if (tempString.contains("Content-Type:")) {
                pattern = Pattern.compile("Content-Type:(.*)$");
                matcher = pattern.matcher(tempString);
                if (matcher.find()) {
                    suffix = matcher.group(1).trim().split("/")[1];
                }
                continue;
            }

            if (tempString.contains("Content-Length:")) {
                pattern = Pattern.compile("Content-Length:(.*)$");
                matcher = pattern.matcher(tempString);
                if (matcher.find()) {
                    imageLength = Long.valueOf(matcher.group(1).trim());
                }
                continue;
            }

            if (tempString.contains("Content-ID:")) {
                pattern = Pattern.compile("Content-ID:(.*)$");
                matcher = pattern.matcher(tempString);
                if (matcher.find()) {
                    imageName = matcher.group(1).trim();
                }
                imageByte = true;
                continue;
            }

            if (imageByte && Objects.equals("", tempString)) {
                //开始拿取图片byte[]内容
                int readByte = 0;
                while ((readByte = bufferedReader.read()) != '-') {
                    byteList.add((byte) readByte);
                }
                break;
            }
        }

        if (suffix == null || imageName == null || imageLength == 0 || byteList.isEmpty()) {
            return null;
        }

        System.out.println("suffix=" + suffix);
        System.out.println("imageName=" + imageName);
        System.out.println("imageLength=" + imageLength);
        System.out.println("byteList=" + byteList);


        File file = new File("./resources/" + imageName + "." + suffix);
        OutputStream outputStream = new FileOutputStream(file);
        byte[] data = new byte[byteList.size()];
        for (int index = 0; index < byteList.size(); index++) {
            data[index] = byteList.get(index);
        }
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();

        return file;
    }
}
