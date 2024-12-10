/**
 * Copyright sp42 frank@ajaxjs.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ajaxjs.util.io;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * 流操作助手类
 * 如不满足 可参考 Spring StreamUtils/ResourceUtils/FileCopyUtils/FileSystemUtils
 */
public class StreamHelper {
    /**
     * 读输入的字节流转换到字符流，将其转换为文本（多行）的字节流转换为字符串
     *
     * @param in 输入流，无须手动关闭
     * @return 字符串
     */
    public static String copyToString(InputStream in) {
        return copyToString(in, StandardCharsets.UTF_8);
    }

    /**
     * 读输入的字节流转换到字符流，将其转换为文本（多行）的字节流转换为字符串。可指定字符编码
     *
     * @param in     输入流，无须手动关闭
     * @param encode 字符编码
     * @return 字符串
     */
    public static String copyToString(InputStream in, Charset encode) {// 等价 Spring copyToString.copy(in, charset)
        StringBuffer result = new StringBuffer();

        read(in, encode, line -> {
            result.append(line);
            result.append('\n');
        });

        return result.toString();
    }

    /**
     * 从输入流中读取数据，并对每行数据应用提供的消费函数
     *
     * @param in     输入流读取器，用于读取数据
     * @param encode 字符编码
     * @param fn     消费函数，接收一行数据作为参数，对每行数据进行处理
     */
    public static void read(InputStream in, Charset encode, Consumer<String> fn) {
        /*
         * 装饰器模式，又称为包装器，可以在不修改被包装类的情况下动态添加功能（例如缓冲区功能）
         * 这里使用 BufferedReader 为输入流添加缓冲功能
         */
        try (InputStreamReader inReader = new InputStreamReader(in, encode);
             BufferedReader reader = new BufferedReader(inReader)) {
            String line;

            while ((line = reader.readLine()) != null) // 一次读入一行，直到读入 null 表示文件结束
                // 指定编码集的另外一种写法 line = new String(line.getBytes(), encodingSet);
                fn.accept(line);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 输入流复制到输出流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 复制了多少字节
     */
    public static long write(InputStream in, OutputStream out) {
        try {
            return in.transferTo(out); // 等价 Spring StreamUtils.copy(in, out)
        } catch (IOException e) {
            throw new RuntimeException("复制流的时候失败", e);
        }
    }

    /**
     * 使用内存操作流，读取二进制，也就是将流转换为内存的数据。
     * InputStream 转换到 byte[]. 从输入流中获取数据， 转换到 byte[]
     * 也就是 in 转到内存。虽然大家可能都在内存里面了但还不能直接使用，要转换
     *
     * @param in 输入流
     * @return 内存的数据
     */
    public static byte[] inputStream2Byte(InputStream in) { // 等价于 Spring StreamUtils.copyToByteArray(InputStream in)：将输入流的内容复制到字节数组
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            write(in, out);

            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 送入的 byte[] 转换为输出流。可指定 byte[] 某一部分数据。 注意这函数不会关闭输出流，请记得在适当的时候将其关闭。
     *
     * @param out    输出流
     * @param data   输入的数据
     * @param off    偏移
     * @param length 长度
     */
    public static void bytes2output(OutputStream out, byte[] data, int off, int length) {
        bytes2output(out, data, true, off, length);
    }

    /**
     * 送入的 byte[] 转换为输出流。可指定 byte[] 某一部分数据。 注意这函数不会关闭输出流，请记得在适当的时候将其关闭。
     *
     * @param out        输出流
     * @param data       输入的数据
     * @param isBuffered 是否需要缓冲
     * @param off        偏移
     * @param length     长度
     */
    public static void bytes2output(OutputStream out, byte[] data, boolean isBuffered, int off, int length) {
        try {
            if (isBuffered)
                out = new BufferedOutputStream(out, 1024);

            if (off == 0 && length == 0)
                out.write(data);
            else
                out.write(data, off, length);

            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("byte[] 转换为输出流时候错误", e);
        }
    }
}
