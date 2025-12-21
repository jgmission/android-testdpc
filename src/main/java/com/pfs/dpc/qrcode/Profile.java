package com.pfs.dpc.qrcode;

public class Profile {
    // The following attributes apply to all entities
    public final static String KNOX_LICENSE_ACTIVATED = "KNOX_LICENSE_ACTIVATED";
    public final static String KNOX_LICENSE_SKIPPED = "KNOX_LICENSE_SKIPPED";
    public final static String BUILT_IN_APPS_INSTALLED = "BUILT_IN_APPS_INSTALLED";
    public final static String POLICY_DEPLOYED = "POLICY_DEPLOYED";

    // The following will be entity specific attributes
    public final static String ENTITY_INFO = "ENTITY_INFO";
    public final static String INITIAL_PROVISION = "INITIAL_PROVISION";
    public final static String UUID = "UUID";
    // for cellphone password pattern
    public final static String CELLPHONE_PASSWORD_PIN_PATTERN = "PASSWORD_PIN_PATTERN";
    // for app password protection
    public final static String APP_PASSWORD_PROTECTION = "APP_PASSWORD_PROTECTION";
    public final static String APP_PASSWORD = "APP_PASSWORD";
    public final static String APP_PASSWORD_HASH = "APP_PASSWORD_HASH";
    public final static String LOCK_SCRREN_IMAGE = "LOCK_SCRREN_IMAGE";
    public final static String PUBLIC_KEY = "PUBLIC_KEY";
}
