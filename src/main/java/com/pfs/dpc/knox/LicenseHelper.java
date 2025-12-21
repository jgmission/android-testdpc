package com.pfs.dpc.knox;

public class LicenseHelper {
    private static String source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static String target = "Q5A8ZWS0XEDC6RFVT9GBY4HNU3J2MI1KO7LP";

    public static String obfuscate(String s) {
        char[] result = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int index = source.indexOf(c);
            if (index >= 0) {
                result[i] = target.charAt(index);
            } else {
                result[i] = c;
            }
        }
        return new String(result);
    }

    public static String unobfuscate(String s) {
        char[] result = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int index = target.indexOf(c);
            if (index >= 0) {
                result[i] = source.charAt(index);
            } else {
                result[i] = c;
            }
        }
        return new String(result);
    }
}
