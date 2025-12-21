package com.pfs.dpc.qrcode;

import com.pfs.dpc.entity.EntityInfo;
import com.pfs.dpc.qrcode.task.PFSTask;
import com.pfs.dpc.qrcode.task.PFSTaskParser;
import com.pfs.dpc.qrcode.task.SetPrivateDNSTask;
import com.pfs.util.Base64;
import com.pfs.util.CryptoHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.*;

public class QRCodeAndroid {
    private final static String APKS_AND_IMAGE = "aai";
    private final static String APPS_TO_DISABLE = "atd";
    private final static String APPS_TO_ENABLE = "ate";
    private final static String APPS_TO_HIDE = "ath";
    private final static String APPS_TO_UNHIDE = "atu";
    private final static String CLEAR_APP_PASSWORD = "cap";
    private final static String DISABLE_APP_PASSWORD_PROTECTION = "dapp";
    private final static String ENABLE_APP_PASSWORD_PROTECTION = "eapp";
    private final static String EXPIRATION_DATE = "ed";
    private final static String ENTITY_INFO = "ei";
    private final static String INITIALIZE_DEFAULT_KNOX_AND_USER_RESTRICTIONS = "idkaur";
    private final static String KNOX_ACTIVATE_LICENSE = "kal";
    private final static String KNOX_DEACTIVATE_LICENSE = "kdl";
    private final static String KNOX_LICENSE_KEY_OBFUSCATED = "klko";
    private final static String KNOX_MAP = "km";
    private final static String KNOX_SKIP_LICENSE = "ksl";
    private final static String PACKAGES_TO_UNINSTALL = "ptu";
    private final static String REMOVE_ALL_KNOX_AND_USER_RESTRICTIONS = "rakaur";
    private final static String RESET_PASSWORD = "rp";
    private final static String REMOVE_THIS_DEVICE_OWNER = "rtdo";
    private final static String SIGNATURE = "s";
    private final static String SHOW_PROFILE = "sp";
    private final static String TASKS = "t";
    private final static String UI_CODE = "uc";
    private final static String USER_RESTRICTIONS_TO_ADD = "urta";
    private final static String USER_RESTRICTIONS_TO_REMOVE = "urtr";

    protected int uiCode = DPCUICode.NONE;
    protected boolean showProfile = false;
    protected boolean clearAppPassword = false;
    protected boolean enableAppPasswordProtection = false;
    protected boolean disableAppPasswordProtection = false;
    protected boolean knoxActivateLicense = false;
    protected String knoxLicenseKeyObfuscated = null;
    protected boolean knoxSkipLicense = false;
    protected boolean knoxDeactivateLicense = false;
    protected boolean initializeDefaultKnoxAndUserRestrictions = false;
    protected boolean removeAllKnoxAndUserRestrictions = false;
    protected boolean removeThisDeviceOwner = false;
    protected String[] appsToDisable = {}; // not sure if the DPC works for this feature
    protected String[] appsToEnable = {}; // not sure if the DPC works for this feature
    protected String[] appsToHide = {};
    protected String[] appsToUnhide = {};
    protected String[] userRestrictionsToAdd = {};
    protected String[] userRestrictionsToRemove = {};
    // apks either from assets or from Internet; image is from internet. APKs will be installed,
    // and the image will be set as lock screen wallpaper (not home screen).
    protected String[] apksAndImage = {};
    protected String[] packagesToUninstall = {};
    protected String resetPassword = null;
    //this is for knox. Key is Integer defined in KnoxPrivilege class
    protected Map<Integer, Boolean> knoxMap = new HashMap();
    protected EntityInfo entityInfo;
    protected List<PFSTask> tasks = new LinkedList<>();
    protected Date expireDate = null;
    protected byte[] signature = null;

    public int getUiCode() {
        return uiCode;
    }

    public boolean isShowProfile() {
        return showProfile;
    }

    public void setShowProfile(boolean showProfile) {
        this.showProfile = showProfile;
    }

    public void setUiCode(int uiCode) {
        this.uiCode = uiCode;
    }

    public boolean isClearAppPassword() {
        return clearAppPassword;
    }

    public void setClearAppPassword(boolean clearAppPassword) {
        this.clearAppPassword = clearAppPassword;
    }

    public boolean isEnableAppPasswordProtection() {
        return enableAppPasswordProtection;
    }

    public void setEnableAppPasswordProtection(boolean enableAppPasswordProtection) {
        this.enableAppPasswordProtection = enableAppPasswordProtection;
    }

    public boolean isDisableAppPasswordProtection() {
        return disableAppPasswordProtection;
    }

    public void setDisableAppPasswordProtection(boolean disableAppPasswordProtection) {
        this.disableAppPasswordProtection = disableAppPasswordProtection;
    }

    public boolean isKnoxActivateLicense() {
        return knoxActivateLicense;
    }

    public void setKnoxActivateLicense(boolean knoxActivateLicense) {
        this.knoxActivateLicense = knoxActivateLicense;
    }

    public boolean isKnoxSkipLicense() {
        return knoxSkipLicense;
    }

    public void setKnoxSkipLicense(boolean knoxSkipLicense) {
        this.knoxSkipLicense = knoxSkipLicense;
    }

    public String getKnoxLicenseKeyObfuscated() {
        return knoxLicenseKeyObfuscated;
    }

    public void setKnoxLicenseKeyObfuscated(String knoxLicenseKeyObfuscated) {
        this.knoxLicenseKeyObfuscated = knoxLicenseKeyObfuscated;
    }

    public boolean isKnoxDeactivateLicense() {
        return knoxDeactivateLicense;
    }

    public void setKnoxDeactivateLicense(boolean knoxDeactivateLicense) {
        this.knoxDeactivateLicense = knoxDeactivateLicense;
    }

    public boolean isInitializeDefaultKnoxAndUserRestrictions() {
        return initializeDefaultKnoxAndUserRestrictions;
    }

    public void setInitializeDefaultKnoxAndUserRestrictions(boolean initializeDefaultKnoxAndUserRestrictions) {
        this.initializeDefaultKnoxAndUserRestrictions = initializeDefaultKnoxAndUserRestrictions;
    }

    public boolean isRemoveAllKnoxAndUserRestrictions() {
        return removeAllKnoxAndUserRestrictions;
    }

    public void setRemoveAllKnoxAndUserRestrictions(boolean removeAllKnoxAndUserRestrictions) {
        this.removeAllKnoxAndUserRestrictions = removeAllKnoxAndUserRestrictions;
    }

    public boolean isRemoveThisDeviceOwner() {
        return removeThisDeviceOwner;
    }

    public void setRemoveThisDeviceOwner(boolean removeThisDeviceOwner) {
        this.removeThisDeviceOwner = removeThisDeviceOwner;
    }

    public String[] getAppsToDisable() {
        return appsToDisable;
    }

    public void setAppsToDisable(String[] appsToDisable) {
        this.appsToDisable = appsToDisable;
    }

    public String[] getAppsToEnable() {
        return appsToEnable;
    }

    public void setAppsToEnable(String[] appsToEnable) {
        this.appsToEnable = appsToEnable;
    }

    public String[] getAppsToHide() {
        return appsToHide;
    }

    public void setAppsToHide(String[] appsToHide) {
        this.appsToHide = appsToHide;
    }

    public String[] getAppsToUnhide() {
        return appsToUnhide;
    }

    public void setAppsToUnhide(String[] appsToUnhide) {
        this.appsToUnhide = appsToUnhide;
    }

    public String[] getUserRestrictionsToAdd() {
        return userRestrictionsToAdd;
    }

    public void setUserRestrictionsToAdd(String[] userRestrictionsToAdd) {
        this.userRestrictionsToAdd = userRestrictionsToAdd;
    }

    public String[] getUserRestrictionsToRemove() {
        return userRestrictionsToRemove;
    }

    public void setUserRestrictionsToRemove(String[] userRestrictionsToRemove) {
        this.userRestrictionsToRemove = userRestrictionsToRemove;
    }

    public String[] getApksAndImage() {
        return apksAndImage;
    }

    public void setApksAndImage(String[] apksAndImage) {
        this.apksAndImage = apksAndImage;
    }

    public String[] getPackagesToUninstall() {
        return packagesToUninstall;
    }

    public void setPackagesToUninstall(String[] packagesToUninstall) {
        this.packagesToUninstall = packagesToUninstall;
    }

    public String getResetPassword() {
        return resetPassword;
    }

    public void setResetPassword(String resetPassword) {
        if (resetPassword != null) {
            resetPassword = resetPassword.trim();
            if (resetPassword.length() > 0) {
                this.resetPassword = resetPassword;
            } else {
                this.resetPassword = null;
            }
        } else {
            this.resetPassword = null;
        }
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Map<Integer, Boolean> getKnoxMap() {
        return knoxMap;
    }

    public void setKnoxMap(Map<Integer, Boolean> knoxMap) {
        this.knoxMap = knoxMap;
    }

    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    public void setEntityInfo(EntityInfo entityInfo) {
        this.entityInfo = entityInfo;
    }

    public List<PFSTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<PFSTask> tasks) {
        this.tasks = tasks;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }


    private byte[] getBytesForSignature() {
        // we do not want to include every attribute for signature. If we do, there will be much more backward
        // compatibility issues with the QR code when new attributes are introduced.
        StringBuilder s = new StringBuilder();
        if (uiCode != DPCUICode.NONE) {
            s.append(UI_CODE).append("/").append(uiCode).append("|");
        }
        if (showProfile) {
            s.append(SHOW_PROFILE).append("/").append(showProfile).append("|");
        }
        if (clearAppPassword) {
            s.append(CLEAR_APP_PASSWORD).append("/").append(clearAppPassword).append("|");
        }
        if (disableAppPasswordProtection) {
            s.append(DISABLE_APP_PASSWORD_PROTECTION).append("/").append(disableAppPasswordProtection).append("|");
        }
        if (enableAppPasswordProtection) {
            s.append(ENABLE_APP_PASSWORD_PROTECTION).append("/").append(enableAppPasswordProtection).append("|");
        }
        if (knoxActivateLicense) {
            s.append(KNOX_ACTIVATE_LICENSE).append("/").append(knoxActivateLicense).append("|");
        }
        if (knoxLicenseKeyObfuscated != null) {
            s.append(KNOX_LICENSE_KEY_OBFUSCATED).append("/").append(knoxLicenseKeyObfuscated).append("|");
        }
        if (knoxSkipLicense) {
            s.append(KNOX_SKIP_LICENSE).append("/").append(knoxSkipLicense).append("|");
        }
        if (knoxDeactivateLicense) {
            s.append(KNOX_DEACTIVATE_LICENSE).append("/").append(knoxDeactivateLicense).append("|");
        }
        if (initializeDefaultKnoxAndUserRestrictions) {
            s.append(INITIALIZE_DEFAULT_KNOX_AND_USER_RESTRICTIONS).append("/").append(initializeDefaultKnoxAndUserRestrictions).append("|");
        }
        if (removeAllKnoxAndUserRestrictions) {
            s.append(REMOVE_ALL_KNOX_AND_USER_RESTRICTIONS).append("/").append(removeAllKnoxAndUserRestrictions).append("|");
        }
        if (removeThisDeviceOwner) {
            s.append(REMOVE_THIS_DEVICE_OWNER).append("/").append(removeThisDeviceOwner).append("|");
        }
        append(s, APPS_TO_DISABLE, appsToDisable);
        append(s, APPS_TO_ENABLE, appsToEnable);
        append(s, APPS_TO_HIDE, appsToHide);
        append(s, APPS_TO_UNHIDE, appsToUnhide);
        append(s, APKS_AND_IMAGE, apksAndImage);
        append(s, PACKAGES_TO_UNINSTALL, packagesToUninstall);
        append(s, USER_RESTRICTIONS_TO_ADD, userRestrictionsToAdd);
        append(s, USER_RESTRICTIONS_TO_REMOVE, userRestrictionsToRemove);
        if (resetPassword != null) {
            s.append(RESET_PASSWORD).append("/").append(resetPassword).append("|");
        }
        if (knoxMap != null && knoxMap.size() > 0) {
            s.append(KNOX_MAP).append("/");
            // make sure they are sorted alphabatically
            Collection<Integer> keySet = knoxMap.keySet();
            List<Integer> keys = new ArrayList<>(keySet);
            Collections.sort(keys);
            for (Integer key : keys) {
                s.append(key).append(":").append(knoxMap.get(key).booleanValue()).append("|");
            }
        }
        if (entityInfo != null) {
            s.append(ENTITY_INFO).append("/").append(entityInfo.toStringForSignature());
        }
        if (tasks != null && tasks.size() > 0) {
            Collections.sort(tasks);
            s.append(TASKS).append("/");
            for (PFSTask task : tasks) {
                s.append(task.toString()).append("|");
            }
        }
        if (expireDate != null) {
            s.append(EXPIRATION_DATE).append("/").append(expireDate.getTime()).append("|");
        }

        String string = s.toString();
        System.out.printf("String for signature: *%s*\n", string);
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

        try {
            MessageDigest md = null;
            md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return digest;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private void append(StringBuilder s, String key, String[] strings) {
        if (strings != null && strings.length > 0) {
            s.append(key).append("/");
            // make sure they are sorted alphabatically
            List<String> list = Arrays.asList(strings);
            Collections.sort(list);
            for (String s1 : list) {
                s.append(s1).append("|");
            }
        }
    }

    public void generateSignature(PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance(CryptoHelper.SIGNATURE_ALGORITHM);
        privateSignature.initSign(privateKey);
        privateSignature.update(getBytesForSignature());
        this.signature = privateSignature.sign();
    }

    public boolean validateSignatire(PublicKey publicKey) {
        try {
            Signature publicSignature = Signature.getInstance(CryptoHelper.SIGNATURE_ALGORITHM);
            publicSignature.initVerify(publicKey);
            publicSignature.update(getBytesForSignature());
            return publicSignature.verify(this.signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
    }

    private JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (uiCode != DPCUICode.NONE) {
                jsonObject.put(UI_CODE, uiCode);
            }
            if (showProfile) {
                jsonObject.put(SHOW_PROFILE, showProfile);
            }
            if (clearAppPassword) {
                jsonObject.put(CLEAR_APP_PASSWORD, clearAppPassword);
            }
            if (disableAppPasswordProtection) {
                jsonObject.put(DISABLE_APP_PASSWORD_PROTECTION, disableAppPasswordProtection);
            }
            if (enableAppPasswordProtection) {
                jsonObject.put(ENABLE_APP_PASSWORD_PROTECTION, enableAppPasswordProtection);
            }
            if (knoxActivateLicense) {
                jsonObject.put(KNOX_ACTIVATE_LICENSE, knoxActivateLicense);
            }
            if (knoxSkipLicense) {
                jsonObject.put(KNOX_SKIP_LICENSE, knoxSkipLicense);
            }
            if (knoxDeactivateLicense) {
                jsonObject.put(KNOX_DEACTIVATE_LICENSE, knoxDeactivateLicense);
            }
            if (knoxActivateLicense || knoxDeactivateLicense) {
                if (knoxLicenseKeyObfuscated != null) {
                    jsonObject.put(KNOX_LICENSE_KEY_OBFUSCATED, knoxLicenseKeyObfuscated);
                }
            }
            if (initializeDefaultKnoxAndUserRestrictions) {
                jsonObject.put(INITIALIZE_DEFAULT_KNOX_AND_USER_RESTRICTIONS, initializeDefaultKnoxAndUserRestrictions);
            }
            if (removeAllKnoxAndUserRestrictions) {
                jsonObject.put(REMOVE_ALL_KNOX_AND_USER_RESTRICTIONS, removeAllKnoxAndUserRestrictions);
            }
            if (removeThisDeviceOwner) {
                jsonObject.put(REMOVE_THIS_DEVICE_OWNER, removeThisDeviceOwner);
            }
            if (resetPassword != null) {
                jsonObject.put(RESET_PASSWORD, resetPassword);
            }
            if (expireDate != null) {
                jsonObject.put(EXPIRATION_DATE, expireDate.getTime()); // timezone???
            }
            if (signature != null) {
                jsonObject.put(SIGNATURE, Base64.encodeToString(signature, Base64.DEFAULT));
            }
            if (entityInfo != null) {
                jsonObject.put(ENTITY_INFO, entityInfo.toString());
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        if (knoxMap != null && knoxMap.size() > 0) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (Map.Entry<Integer, Boolean> entry : knoxMap.entrySet()) {
                    JSONObject object = new JSONObject();
                    object.put("key", entry.getKey().intValue());
                    object.put("value", entry.getValue().booleanValue());
                    jsonArray.put(object);
                }
                jsonObject.put(KNOX_MAP, jsonArray);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        putIntoJSONObject(jsonObject, APPS_TO_DISABLE, appsToDisable);
        putIntoJSONObject(jsonObject, APPS_TO_ENABLE, appsToEnable);
        putIntoJSONObject(jsonObject, APPS_TO_HIDE, appsToHide);
        putIntoJSONObject(jsonObject, APPS_TO_UNHIDE, appsToUnhide);
        putIntoJSONObject(jsonObject, USER_RESTRICTIONS_TO_ADD, userRestrictionsToAdd);
        putIntoJSONObject(jsonObject, USER_RESTRICTIONS_TO_REMOVE, userRestrictionsToRemove);
        putIntoJSONObject(jsonObject, APKS_AND_IMAGE, apksAndImage);
        putIntoJSONObject(jsonObject, PACKAGES_TO_UNINSTALL, packagesToUninstall);

        // tasks
        if (tasks != null && tasks.size() > 0) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (PFSTask task : tasks) {
                    jsonArray.put(task.toString());
                }
                jsonObject.put(TASKS, jsonArray);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return jsonObject;
    }

    private final static void putIntoJSONObject(JSONObject jsonObject, String key, String[] values) {
        if (values != null && values.length > 0) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (String value : values) {
                    jsonArray.put(value);
                }
                jsonObject.put(key, jsonArray);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    private final static String[] getFromJSONObject(JSONObject jsonObject, String key) {
        String[] values = {};
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray(key);
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
            return values;
        }

        try {
            if (jsonArray != null && jsonArray.length() > 0) {
                values = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    values[i] = jsonArray.getString(i);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return values;
    }

    private static void handleJSONExceptionFromGet(JSONException ex) {
        if (ex.getMessage() != null && (ex.getMessage().contains("not found") || ex.getMessage().contains("No value for"))) {
            // skip
        } else {
            ex.printStackTrace();
        }
    }
    private static QRCodeAndroid parse(JSONObject jsonObject) {
        QRCodeAndroid qrCodeAndroid = new QRCodeAndroid();
        try {
            qrCodeAndroid.setUiCode(jsonObject.getInt(UI_CODE));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setShowProfile(jsonObject.getBoolean(SHOW_PROFILE));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setClearAppPassword(jsonObject.getBoolean(CLEAR_APP_PASSWORD));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setDisableAppPasswordProtection(jsonObject.getBoolean(DISABLE_APP_PASSWORD_PROTECTION));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setEnableAppPasswordProtection(jsonObject.getBoolean(ENABLE_APP_PASSWORD_PROTECTION));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setKnoxActivateLicense(jsonObject.getBoolean(KNOX_ACTIVATE_LICENSE));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setKnoxSkipLicense(jsonObject.getBoolean(KNOX_SKIP_LICENSE));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setKnoxDeactivateLicense(jsonObject.getBoolean(KNOX_DEACTIVATE_LICENSE));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        if (qrCodeAndroid.isKnoxActivateLicense() || qrCodeAndroid.isKnoxDeactivateLicense()) {
            try {
                qrCodeAndroid.setKnoxLicenseKeyObfuscated(jsonObject.getString(KNOX_LICENSE_KEY_OBFUSCATED));
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        try {
            qrCodeAndroid.setInitializeDefaultKnoxAndUserRestrictions(jsonObject.getBoolean(INITIALIZE_DEFAULT_KNOX_AND_USER_RESTRICTIONS));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setRemoveAllKnoxAndUserRestrictions(jsonObject.getBoolean(REMOVE_ALL_KNOX_AND_USER_RESTRICTIONS));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setRemoveThisDeviceOwner(jsonObject.getBoolean(REMOVE_THIS_DEVICE_OWNER));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            qrCodeAndroid.setResetPassword(jsonObject.getString(RESET_PASSWORD));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            long time = jsonObject.getLong(EXPIRATION_DATE);
            qrCodeAndroid.setExpireDate(new Date(time));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            String s = jsonObject.getString(SIGNATURE);
            qrCodeAndroid.setSignature(Base64.decode(s, Base64.DEFAULT));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }
        try {
            String s = jsonObject.getString(ENTITY_INFO);
            qrCodeAndroid.setEntityInfo(EntityInfo.parse(s));
        } catch (JSONException ex) {
            handleJSONExceptionFromGet(ex);
        }

        // knox map
        {
            Map<Integer, Boolean> map = new HashMap<>();
            JSONArray jsonArray = null;
            try {
                jsonArray = jsonObject.getJSONArray(KNOX_MAP);
            } catch (JSONException ex) {
                handleJSONExceptionFromGet(ex);
            }
            try {
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        map.put(object.getInt("key"), object.getBoolean("value"));
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            qrCodeAndroid.setKnoxMap(map);
        }

        qrCodeAndroid.setAppsToDisable(getFromJSONObject(jsonObject, APPS_TO_DISABLE));
        qrCodeAndroid.setAppsToEnable(getFromJSONObject(jsonObject, APPS_TO_ENABLE));
        qrCodeAndroid.setAppsToHide(getFromJSONObject(jsonObject, APPS_TO_HIDE));
        qrCodeAndroid.setAppsToUnhide(getFromJSONObject(jsonObject, APPS_TO_UNHIDE));
        qrCodeAndroid.setUserRestrictionsToAdd(getFromJSONObject(jsonObject, USER_RESTRICTIONS_TO_ADD));
        qrCodeAndroid.setUserRestrictionsToRemove(getFromJSONObject(jsonObject, USER_RESTRICTIONS_TO_REMOVE));
        qrCodeAndroid.setApksAndImage(getFromJSONObject(jsonObject, APKS_AND_IMAGE));
        qrCodeAndroid.setPackagesToUninstall(getFromJSONObject(jsonObject, PACKAGES_TO_UNINSTALL));

        // get tasks
        {
            String[] values = {};
            JSONArray jsonArray = null;
            try {
                jsonArray = jsonObject.getJSONArray(TASKS);
                try {
                    if (jsonArray != null && jsonArray.length() > 0) {
                        // all the tasks
                        List<PFSTask> tasks = new LinkedList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            PFSTask task = PFSTaskParser.parse(jsonArray.getString(i));
                            if (task != null) {
                                tasks.add(task);
                            }
                        }
                        qrCodeAndroid.setTasks(tasks);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            } catch (JSONException ex) {
                handleJSONExceptionFromGet(ex);
            }
        }
        return qrCodeAndroid;
    }

    public static QRCodeAndroid parse(String s) {
        QRCodeAndroid qrCodeAndroid = null;
        try {
            JSONObject jsonObject = new JSONObject(s);
            qrCodeAndroid = parse(jsonObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return qrCodeAndroid;
    }

    public static QRCodeAndroid fromEntityInfo(EntityInfo entityInfo) {
        QRCodeAndroid code = new QRCodeAndroid();
        code.setEntityInfo(entityInfo);
        // the following attributes are not serialized in EntityInfo and have to be transferred to QRCodeAndroid
        code.setResetPassword(entityInfo.getPasswordPattern());
        code.setEnableAppPasswordProtection(entityInfo.isAppPasswordProtection());
        List<String> files = new LinkedList<>();
        for (String file : entityInfo.getApks()) {
            files.add("https://phoneforschool.com/" + entityInfo.getFolderName() + "/" + file);
        }
        files.add("https://phoneforschool.com/" + entityInfo.getFolderName() + "/logo.jpg");
        files.add("https://phoneforschool.com/" + entityInfo.getFolderName() + "/public.key");
        String[] array = new String[files.size()];
        files.toArray(array);
        code.setApksAndImage(array);

        // private dns
        if (entityInfo.getPrivateDNS() != null) {
            SetPrivateDNSTask task = new SetPrivateDNSTask();
            task.setDnsServer("dns.stevedns.com");
            List<PFSTask> tasks = new LinkedList<>();
            tasks.add(task);
            code.setTasks(tasks);
        }

        // we cannot set this because this will prevent apps from being installed, even the apks are installed in front
        // of the restrictions being set.
        // code.setInitializeDefaultKnoxAndUserRestrictions(true);
        return code;
    }
}