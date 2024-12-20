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

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random Tools.
 */
public class RandomTools {
    public static int generateRandomNumber() {
        return generateRandomNumber(6);
    }

    /**
     * 生成一个指定位数的随机整数。
     *
     * @param numDigits 指定位数
     * @return 指定位数的随机整数
     */
    public static int generateRandomNumber(int numDigits) {
        if (numDigits <= 0)
            throw new IllegalArgumentException("The number of digits must be greater than zero.");

        int min = (int) Math.pow(10, numDigits - 1);// 计算最小值和最大值
        int max = (int) Math.pow(10, numDigits) - 1;

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static int generateRandomNumberOld() {
        return generateRandomNumberOld(6);
    }

    /**
     * 生成一个指定位数的随机整数。
     *
     * @param numDigits 指定位数
     * @return 指定位数的随机整数
     */
    public static int generateRandomNumberOld(int numDigits) {
        if (numDigits <= 0)
            throw new IllegalArgumentException("The number of digits must be greater than zero.");

        int min = (int) Math.pow(10, numDigits - 1);
        int max = (int) Math.pow(10, numDigits) - 1;

        // 生成并返回指定范围内的随机数
        return new Random().nextInt(max - min + 1) + min;
    }

    /**
     * 随机字符串
     * noinspection SpellCheckingInspection
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * 生成指定长度的随机字符，可能包含数字
     *
     * @return 随机字符
     */
    public static String generateRandomString() {
        return generateRandomString(6);
    }

    /**
     * 生成指定长度的随机字符，可能包含数字
     *
     * @param length 户要求产生字符串的长度
     * @return 随机字符
     */
    public static String generateRandomString(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(STR.charAt(number));
        }

        return sb.toString();
    }

    /**
     * 生成指定长度的随机字符，可能包含数字
     * 另外一个方法 <a href="https://blog.csdn.net/qq_41995919/article/details/115299461">...</a>
     *
     * @param length 户要求产生字符串的长度
     * @return 随机字符
     */
    public static String generateRandomStringOld(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(STR.charAt(number));
        }

        return sb.toString();
    }

    /**
     * 生成一个 UUID，可选择是否去掉其中的 "-" 符号（Copy from Spring，Spring 提供的算法性能远远高于 JDK 的）
     *
     * @param isRemove 是否去掉 "-" 符号
     * @return 生成的 UUID 字符串
     */
    public static String uuid(boolean isRemove) {
        String uuid = UUID.randomUUID().toString();

        return isRemove ? uuid.replace("-", StrUtil.EMPTY_STRING) : uuid;
    }

    /**
     * 生成一个去掉 "-" 字符的 UUID 字符串
     *
     * @return 生成的 UUID 字符串
     */
    public static String uuid() {
        return uuid(true);
    }
}
