package com.pspt.detection.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ByteKit {
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
}
