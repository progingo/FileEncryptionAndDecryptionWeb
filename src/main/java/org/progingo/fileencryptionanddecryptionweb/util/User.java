package org.progingo.fileencryptionanddecryptionweb.util;

import java.security.MessageDigest;

public class User {
    private static String name;
    private static String workPath;
    private static String password;

    public User(String name, String password, String workPath) {
        this.name = getMD5(name);
        this.password = getMD5(password);
        this.workPath = workPath;
    }

    public static String getName() {
        return name;
    }

    public static String getWorkPath() {
        return workPath;
    }

    public static String getPassword() {
        return password;
    }

    private String getMD5(String password){
        String hex = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            //转换成十六进制字符串
            hex = bytesToHex(digest);
        }catch (Exception e){
            System.out.println(e);
        }

        return hex;
    }
    private String bytesToHex(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes){
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                builder.append('0');
            builder.append(hex);
        }
        return builder.toString();
    }
}
