package com.pfs.dpc.qrcode.task;

public class PFSTaskParser {
    public static PFSTask parse(String s) {
        PFSTask task = null;
        if (s == null || s.length() == 0) {
            return task;
        } else {
            task = SetPrivateDNSTask.parse(s);
            if (task != null) {
                return task;
            }
            // keep parsing
        }
        return task;
    }
}
