package com.swust.common.utils;

import java.security.SecureRandom;

public class SaltGenerator {

    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 生成指定长度的随机盐。
     *
     * @param length 盐的长度
     * @return 随机生成的盐
     */
    public static String generateSalt(int length) {
        StringBuilder salt = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(SALT_CHARS.length());
            salt.append(SALT_CHARS.charAt(randomIndex));
        }
        return salt.toString();
    }

    /**
     * 使用SecureRandom生成指定字节长度的盐，并以十六进制字符串形式返回。
     *
     * @param byteLength 字节长度
     * @return 十六进制表示的盐
     */
    public static String generateHexSalt(int byteLength) {
        byte[] saltBytes = new byte[byteLength];
        secureRandom.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * 将字节数组转换为十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) {
        // 生成一个长度为16的随机盐
        System.out.println("Random Salt: " + generateSalt(16));

        // 生成一个16字节长的盐，并以十六进制字符串形式展示
        System.out.println("Hex Salt: " + generateHexSalt(16));
    }
}