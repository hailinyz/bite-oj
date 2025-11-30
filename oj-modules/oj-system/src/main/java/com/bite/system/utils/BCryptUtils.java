package com.bite.system.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptUtils {
    /**
     * ⽣成加密后密⽂
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    //123456  adjkshfiuhnfjkdshslaiudhdsm  不能再用equals 相比

    /**
     * 判断密码是否相同
     *
     * @param rawPassword 真实密码  用户输入的密码
     * @param encodedPassword 加密后密⽂  从数据库中查出来的加密后密码
     * @return 结果
     */

    //根据数据库中查出的加密后得到的密码，提取出当时加密后的盐值（提取盐值），有了这个盐值，就可以根据用户输入的密码，生成加密后的密⽂，然后和数据库中查出来的密⽂进行比较
    public static boolean matchesPassword(String rawPassword, String
            encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword); // 判断密码是否相同（提取盐值）
    }

    public static void main(String[] args) {
        System.out.println(encryptPassword("123456"));
    }
}

