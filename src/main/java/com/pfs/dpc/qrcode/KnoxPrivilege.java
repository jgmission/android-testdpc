package com.pfs.dpc.qrcode;

public class KnoxPrivilege {
    public final static int allowBluetoothDataTransferIndex = 0;
    public final static int allowFactoryResetIndex = 1;

    // it seems so far Android itself does not support this feature so we have to use Knox.
    public final static int allowFirmwareRecoveryIndex = 2;

    public final static int allowOTAUpgradeIndex = 3;
    public final static int allowSafeModeIndex = 4;
    public final static int allowUsbHostStorageIndex = 5;
    public final static int allowWiFiIndex = 6;

    // wi-fi direct is similar to bluetooth. Wi-Fi Direct (formerly Wi-Fi Peer-to-Peer) is a Wi-Fi
    // standard for peer-to-peer wireless connections[1] that allows two devices to establish a
    // direct Wi-Fi connection without an intermediary wireless access point, router, or Internet
    // connection.
    public final static int allowWiFiDirectIndex = 7;

    // Cellular network cannot be restored once it is disabled.
    // If it is disabled, Wallpapers cannot show images from Internet, phone calls and text messages
    // will not work. NEED TO BE VERY CAUTIOUS WHEN TURNING THIS OFF.
    public final static int setCellularDataIndex = 8;

    // This is for setting password/pin pattern
    public final static int setLockScreenStateIndex = 9;

    public final static int setSdCardStateIndex = 10;
    public final static int setUsbDebuggingEnabledIndex = 11;
    public final static int setWifiTetheringIndex = 12;
    public final static int allowWallpaperChangeIndex = 13;

    public final static String[] privileges = new String[14];

    static {
        privileges[allowBluetoothDataTransferIndex] = "allowBluetoothDataTransfer";
        privileges[allowFactoryResetIndex] = "allowFactoryReset";

        privileges[allowFirmwareRecoveryIndex] = "allowFirmwareRecovery";

        privileges[allowOTAUpgradeIndex] = "allowOTAUpgrade";
        privileges[allowSafeModeIndex] = "allowSafeMode";
        privileges[allowUsbHostStorageIndex] = "allowUsbHostStorage";
        privileges[allowWallpaperChangeIndex] = "allowWallpaperChange";
        privileges[allowWiFiIndex] = "allowWiFi";

        privileges[allowWiFiDirectIndex] = "allowWiFiDirect";

        privileges[setCellularDataIndex] = "setCellularData";
        privileges[setLockScreenStateIndex] = "setLockScreenState";
        privileges[setSdCardStateIndex] = "setSdCardState";
        privileges[setUsbDebuggingEnabledIndex] = "setUsbDebuggingEnabled";
        privileges[setWifiTetheringIndex] = "setWifiTethering";

        for (int i = 0; i < privileges.length; i++) {
            if (privileges[i] == null) {
                System.out.printf("Knox privilege is null: index is %d.\n", i);
                System.exit(-1);
            }
        }
    }

}
