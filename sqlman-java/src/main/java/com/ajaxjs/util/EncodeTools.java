/*
 * Copyright (C) 2025 Frank Cheung<frank@ajaxjs.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * String encoder
 */
public class EncodeTools {
    /**
     * URL 网址的中文乱码处理。 如果 Tomcat 过滤器设置了 UTF-8 那么这里就不用重复转码了
     *
     * @param str 通常是 URL 的 Query String 参数
     * @return 中文
     */
    public static String urlChinese(String str) {
        return StrUtil.byte2String(str.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * URL 编码。 适合 GET 请求时候用
     * <p>
     * <a href="https://www.cnblogs.com/del88/p/6496825.html">...</a>
     *
     * @param str 输入的字符串
     * @return 编码后的字符串
     */
    public static String urlEncodeQuery(String str) {
        str = str.replaceAll(" ", "%20");
        return urlEncode(str);
    }

    /**
     * UTF-8 字符串而已
     */
    public static final String UTF8_SYMBOL = "UTF-8";

    /**
     * URL 编码
     *
     * @param str 输入的字符串
     * @return URL 编码后的字符串
     */
    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * URL 解码
     *
     * @param str 输入的字符串
     * @return URL 解码后的字符串
     */
    public static String urlDecode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    // ---------------------- ENCODE Base64 -----------------------------

    /**
     * BASE64 编码
     * 这是核心编码方法，无关编码
     *
     * @param bytes 待编码的字符串 bytes
     * @return 已编码的字符串 bytes
     */
    public static byte[] base64Encode(byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }

    /**
     * BASE64 编码
     *
     * @param bytes 待编码的字符串 bytes
     * @return 已编码的字符串。使用 ISO-8859-1 字符集，请注意区别
     */
    public static String base64EncodeToString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * BASE64 编码
     *
     * @param str 待编码的字符串，使用 ISO-8859-1 字符集，请注意区别
     * @return 已编码的字符串。使用 ISO-8859-1 字符集，请注意区别
     */
    public static String base64EncodeToString(String str) {
        return base64EncodeToString(str.getBytes());
    }

    /**
     * BASE64 编码
     *
     * @param bytes 待编码的字符串 bytes
     * @return 已编码的字符串。使用 UTF-8 字符集，请注意区别
     */
    public static String base64EncodeToStringUtf8(byte[] bytes) {
        return StrUtil.byte2String(base64Encode(bytes));
    }

    /**
     * BASE64 编码
     *
     * @param str 待编码的字符串，使用 UTF-8 字符集，请注意区别
     * @return 已编码的字符串。使用 UTF-8 字符集，请注意区别
     */
    public static String base64EncodeToStringUtf8(String str) {
        return base64EncodeToStringUtf8(StrUtil.getUTF8_Bytes(str));
    }

    // ---------------------- DECODE Base64 -----------------------------

    /**
     * BASE64 解码
     * 这是核心解码方法，无关编码
     *
     * @param bytes 待解码的字符串 bytes
     * @return 已解码的字符串 bytes
     */
    public static byte[] base64Decoder(byte[] bytes) {
        return Base64.getDecoder().decode(bytes);
    }

    /**
     * BASE64 解码
     *
     * @param str 待解码的字符串 bytes，使用 ISO-8859-1 字符集，请注意区别
     * @return 已解码的字符串数据
     */
    public static byte[] base64Decode(String str) {
        return Base64.getDecoder().decode(str);
    }

    /**
     * BASE64 解码
     *
     * @param str 待解码的字符串 bytes，使用 ISO-8859-1 字符集，请注意区别
     * @return 已解码的字符串数据
     */
    public static String base64DecodeToString(String str) {
        return new String(base64Decode(str), StandardCharsets.ISO_8859_1);
    }

    /**
     * BASE64 解码
     *
     * @param bytes 待解码的字符串 bytes
     * @return 已解码的字符串。使用 UTF-8 字符集，请注意区别
     */
    public static String base64DecodeToStringUtf8(byte[] bytes) {
        return StrUtil.byte2String(base64Decoder(bytes));
    }

    /**
     * BASE64 解码
     *
     * @param str 待解码的字符串，使用 UTF-8 字符集，请注意区别
     * @return 已解码的字符串。使用 UTF-8 字符集，请注意区别
     */
    public static String base64DecodeToStringUtf8(String str) {
        return base64DecodeToStringUtf8(StrUtil.getUTF8_Bytes(str));
    }
}
