package org.feitian.dpc;

/**
 * WE SHOULD NOT COMMENT OUT ANY RESTRICTION; OTHERWISE THE REMOVE ALL USER
 * RESTRICTIONS FEATURE WILL NOT WORK BECAUSE ANY COMMENTED OUT RESTRICTION CANNOT
 * BE KNOWN TO THE APP.
 */
public class UserRestrictions {
    public final static String[] installRestrictions = {
            android.os.UserManager.DISALLOW_INSTALL_APPS,
            android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY,
    };

    public final static String[] restrictons = {
            android.os.UserManager.DISALLOW_ADD_MANAGED_PROFILE,
            android.os.UserManager.DISALLOW_ADD_USER,
            android.os.UserManager.DISALLOW_AUTOFILL,

            // other regular phones should not share content with our phones.
            android.os.UserManager.DISALLOW_BLUETOOTH_SHARING,

            android.os.UserManager.DISALLOW_CONFIG_CELL_BROADCASTS,
            android.os.UserManager.DISALLOW_CONFIG_CREDENTIALS,

            // QR code has expiration time, changing date and time will affect the usage of QR code.
            android.os.UserManager.DISALLOW_CONFIG_DATE_TIME,

            android.os.UserManager.DISALLOW_CONFIG_LOCATION,

            android.os.UserManager.DISALLOW_CONFIG_PRIVATE_DNS,
            android.os.UserManager.DISALLOW_CONFIG_TETHERING,
            android.os.UserManager.DISALLOW_CONFIG_VPN,
            android.os.UserManager.DISALLOW_CONTENT_SUGGESTIONS,

            android.os.UserManager.DISALLOW_DEBUGGING_FEATURES,

            android.os.UserManager.DISALLOW_FUN,

            android.os.UserManager.DISALLOW_INSTALL_APPS,
            android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY,

            android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS,
            android.os.UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
            android.os.UserManager.DISALLOW_OUTGOING_BEAM,
            android.os.UserManager.DISALLOW_PRINTING,
            android.os.UserManager.DISALLOW_REMOVE_MANAGED_PROFILE,
            android.os.UserManager.DISALLOW_REMOVE_USER,

            android.os.UserManager.DISALLOW_SET_WALLPAPER,
            android.os.UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE,
            android.os.UserManager.DISALLOW_SHARE_LOCATION,
            android.os.UserManager.DISALLOW_UNINSTALL_APPS,
            android.os.UserManager.DISALLOW_USB_FILE_TRANSFER,
            android.os.UserManager.DISALLOW_USER_SWITCH,
    };
    public final static String[] noRestrictons = {
            android.os.UserManager.DISALLOW_ADJUST_VOLUME,

            // this restriction is to enable or diable system apps
            android.os.UserManager.DISALLOW_APPS_CONTROL,

            android.os.UserManager.DISALLOW_AIRPLANE_MODE,
            android.os.UserManager.DISALLOW_AMBIENT_DISPLAY,
            android.os.UserManager.DISALLOW_CONFIG_BRIGHTNESS,
            android.os.UserManager.DISALLOW_CONFIG_LOCALE,

            // need this to make international calls
            android.os.UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,

            android.os.UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT,
            android.os.UserManager.DISALLOW_CONTENT_CAPTURE,
            android.os.UserManager.DISALLOW_CREATE_WINDOWS,
            android.os.UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE,

            // need this to make international calls
            android.os.UserManager.DISALLOW_DATA_ROAMING,

            android.os.UserManager.DISALLOW_OUTGOING_CALLS,
            android.os.UserManager.DISALLOW_SET_USER_ICON,
            android.os.UserManager.DISALLOW_SMS,
            android.os.UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS,
            android.os.UserManager.DISALLOW_UNIFIED_PASSWORD,
            android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE,

            // Removed these restrictions to prevent bricking the phone.
            // We use separate QR codes to set these restrictions.
            // WE SHOULD NOT COMMENT OUT THESE RESTRICTIONS; OTHERWISE THE REMOVE ALL USER
            // RESTRICTIONS FEATURE WILL NOT WORK BECAUSE ANY COMMENTED OUT RESTRICTION CANNOT
            // BE KNOWN TO THE APP. IN FACT WE SHOULD NOT COMMENT OUT ANY RESTRICTION.
            android.os.UserManager.DISALLOW_FACTORY_RESET,
            android.os.UserManager.DISALLOW_SAFE_BOOT,

            // We will use separate QR code to process these restrictions
            android.os.UserManager.DISALLOW_BLUETOOTH,
            android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH,
            android.os.UserManager.DISALLOW_CONFIG_WIFI,
            android.os.UserManager.DISALLOW_NETWORK_RESET,
    };

    public static void main(String[] args) {
        for (String s : restrictons) {
            System.out.println("\"" + s + "\", //restricted");
        }
        for (String s : noRestrictons) {
            System.out.println("\"" + s + "\", //not restricted");
        }
    }
}
