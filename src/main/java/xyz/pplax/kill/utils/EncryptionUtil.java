package xyz.pplax.kill.utils;

import org.springframework.util.DigestUtils;

public class EncryptionUtil {

    public static String getMD5 (String str) {
        // md5盐
        String salt = "PPLAXHATETHEWORLD";
        String base = str + "/" + salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

}
