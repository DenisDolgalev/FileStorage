package ru.sav.filestorage.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class FileNameGenerator {

    //Generate unique file name for storage
    public static String generateFileName() {
        DateFormat df = new SimpleDateFormat("yyyyDSSS");
        return df.format(new Date()) + generateRandomString(16) + ".txt";
    }

    private static String generateRandomString(Integer len) {
        Random random = new Random();
        String rand = "";
        for (int i = 0; i < len; i++) {
            if (random.nextBoolean()) {
                rand += Character.toString((char) 65 + random.nextInt(26));
            } else {
                rand += Character.toString((char) 97 + random.nextInt(26));
            }
        }
        return rand;
    }
}
