package com.geekster.blog.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class PasswordEncryptor {
    public static String encrypt(String unhashedPassword) throws NoSuchAlgorithmException {

        MessageDigest md5 = MessageDigest.getInstance("MD5");

        md5.update(unhashedPassword.getBytes());
        byte[] digested = md5.digest();

        Formatter formatter = new Formatter();
        for (byte b : digested) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
