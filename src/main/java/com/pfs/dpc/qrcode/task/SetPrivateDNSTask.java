package com.pfs.dpc.qrcode.task;

import com.pfs.dpc.qrcode.task.PFSTask;

public class SetPrivateDNSTask extends PFSTask<SetPrivateDNSTask> {
    private final static String PREFIX = "SetPrivateDNSTask: ";
    private String dnsServer = null;

    public String getDnsServer() {
        return dnsServer;
    }

    public void setDnsServer(String dnsServer) {
        this.dnsServer = dnsServer;
    }

    @Override
    public int compareTo(SetPrivateDNSTask o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return PREFIX + dnsServer;
    }

    public static SetPrivateDNSTask parse(String s) {
        if (s == null) {
            return null;
        } else if (s.startsWith(PREFIX)) {
            String dns = s.substring(PREFIX.length());
            if (dns != null && dns.length() > 0) {
                SetPrivateDNSTask task = new SetPrivateDNSTask();
                task.setDnsServer(dns);
                return task;
            }
        }
        return null;
    }
}
