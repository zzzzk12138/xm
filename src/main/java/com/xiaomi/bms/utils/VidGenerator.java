package com.xiaomi.bms.utils;

import java.security.SecureRandom;

public class VidGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int VID_LENGTH = 16;
    private static final SecureRandom random = new SecureRandom();

    public static String generateVid() {
        // 固定前缀"VH"
        StringBuilder vid = new StringBuilder("VH");
        
        // 生成剩余14位随机字符
        for (int i = 0; i < VID_LENGTH - 2; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            vid.append(CHARACTERS.charAt(randomIndex));
        }
        
        return vid.toString();
    }

    public static void main(String[] args) {
        System.out.println(generateVid());
    }
} 