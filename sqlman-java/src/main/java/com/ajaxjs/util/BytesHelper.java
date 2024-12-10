package com.ajaxjs.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BytesHelper {
    /**
     * 在字节数组中截取指定长度数组
     *
     * @param data   输入的数据
     * @param off    偏移
     * @param length 长度
     * @return 指定 范围的字节数组
     */
    public static byte[] subBytes(byte[] data, int off, int length) {
        byte[] bs = new byte[length];
        System.arraycopy(data, off, bs, 0, length);

        return bs;
    }

    /**
     * 在字节数组里查找某个字节数组，找到返回&lt;=0，未找到返回-1
     *
     * @param data   被搜索的内容
     * @param search 要搜索内容
     * @param start  搜索起始位置
     * @return 目标位置，找不到返回-1
     */
    public static int byteIndexOf(byte[] data, byte[] search, int start) {
        int len = search.length;

        for (int i = start; i < data.length; i++) {
            int temp = i, j = 0;

            while (data[temp] == search[j]) {
                temp++;
                j++;

                if (j == len)
                    return i;
            }
        }

        return -1;
    }

    /**
     * 在字节数组里查找某个字节数组，找到返回 &lt;=0，未找到返回 -1
     *
     * @param data   被搜索的内容
     * @param search 要搜索内容
     * @return 目标位置，找不到返回 -1
     */
    public static int byteIndexOf(byte[] data, byte[] search) {
        return byteIndexOf(data, search, 0);
    }

    /**
     * 合并两个字节数组
     *
     * @param a 数组a
     * @param b 数组b
     * @return 新合并的数组
     */
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return c;
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /**
     * byte[] 转化为 16 进制字符串输出
     *
     * @param bytes 字节数组
     * @return 16 进制字符串
     */
    public static String bytesToHexStr(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;

            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars, StandardCharsets.UTF_8);
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr 16进制字符串
     * @return 二进制数组
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.isEmpty())
            return null;

        byte[] result = new byte[hexStr.length() / 2];

        for (int i = 0; i < hexStr.length() / 2; i++) {
            // 获取高位和低位的16进制数
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            // 计算二进制数
            result[i] = (byte) (high * 16 + low);
        }

        return result;
    }

    /**
     * char 数组转 byte 数组
     * 将 char 数组转换为 byte 数组需要考虑编码方式的问题
     * <a href="https://houbb.github.io/2023/06/05/java-perf-02-chars-to-bytes">...</a>
     *
     * @param chars 输入的字符数组。
     * @return 转换后的字节数组。
     */
    public static byte[] charToByte(char[] chars) {
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);

        return bytes;
    }
}
