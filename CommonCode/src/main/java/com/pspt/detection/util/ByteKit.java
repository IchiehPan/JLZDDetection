package com.pspt.detection.util;

import com.pspt.detection.business.HikReceiveBusiness;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ByteKit {
    public static void main(String[] args) {
//        for (String s : new File(".").list()) {
//            System.out.println(s);
//        }
        HikReceiveBusiness hikReceiveBusiness = new HikReceiveBusiness();

        try {
//            FileReader fileReader = new FileReader("./resources/face.jpg");
            FileReader fileReader = new FileReader("./resources/file");
            System.out.println("getEncoding()=" + fileReader.getEncoding());
            System.out.println();

            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String jsonString = hikReceiveBusiness.receiveJson(bufferedReader);

            File file = null;
            while ((file = hikReceiveBusiness.receiveImage(bufferedReader)) != null) {
                System.out.println(file.getAbsolutePath());
            }

            bufferedReader.close();

//            char[] buffer = new char[1024];
//            int ch = 0;
//            while ((ch = fileReader.read()) != -1) {
//                System.out.print((char) ch);
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        string2bytes("Content-Type: multipart/form-data; boundary=MIME_boundary\n");
//        System.out.println();
//        string2bytes("\n");
    }

    public static byte[] byteArrayLink(byte[] a, byte[] b) {
        Byte[] resultArray = Stream.of(a, b).flatMap(child -> IntStream.range(0, child.length).mapToObj(i -> child[i]))
                .toArray(Byte[]::new);
        List<Byte> resultList = new ArrayList<>();
        boolean flag = false;
        for (Byte be : resultArray) {
            if (be != 0) {
                // start
                resultList.add(be);
                flag = true;
            }
            if (flag && be == 0) {
                // end
                break;
            }
        }

        Byte[] result = resultList.stream().toArray(Byte[]::new);
        byte[] castResult = new byte[result.length];
        for (int index = 0; index < result.length; index++) {
            castResult[index] = result[index];
        }

        return castResult;
    }

    public static void string2bytes(String str) {
        try {
            byte[] bytes = str.getBytes("utf8");

            for (byte temp : bytes) {
                System.out.print(temp + " ");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
