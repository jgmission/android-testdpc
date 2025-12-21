package com.pfs.dpc.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class EntityInfo {
    private final static String ABBR = "ab";
    private final static String ADDRESS = "ad";
    private final static String NAME = "n";
    private final static String PASSWORD_HASH_ForManualProvisionUI = "p1";
    private final static String PASSWORD_HASH_ForRemoveDefaultSystemAndUserRestrictions = "p2";
    private final static String TAG = "t";

    private final static EntityInfo defaultEntityInfo = new EntityInfo("PhoneForStudents.com", "PFS", "Bellevue, WA USA", null);

    private String name;
    private String abbr;
    private String address;
    private String tag;
    private String passwordHashForManualProvisionUI;
    private String passwordHashForRemoveDefaultSystemAndUserRestrictions;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // THE FOLLOWING ATTRIBUTES ARE NOT SERIALIZED. THEY ARE USED FOR CONSTRUCTING THE EntityInfo OBJECTS IN THE
    // EntityInfoRepo.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // unique private and public keys for this entity
    private String privateKeyBase64;
    private String publicKeyBase64;
    // two passwords for the invisible buttons in PFS UI
    private String passwordForManualProvisionUI;
    private String passwordForRemoveDefaultSystemAndUserRestrictions;
    // the folder which contains files for apks, policy, logo, public key, etc.
    private String folderName = null;
    // apks for this entity
    private String[] apks = null;
    // this is for the PIN pattern on lock screen of the cellphone
    private String passwordPattern = null;
    // the app password protection
    private boolean appPasswordProtection = false;
    private String privateDNS = null;

    public EntityInfo(String name, String abbr, String address, String tag) {
        this.name = name;
        this.abbr = abbr;
        this.address = address;
        this.tag = tag;
    }

    public String getKey() {
        return getName() + ", " + getAddress();
    }

    public static EntityInfo getDefaultEntityInfo() {
        return defaultEntityInfo;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPasswordHashForManualProvisionUI() {
        return passwordHashForManualProvisionUI;
    }

    public void setPasswordHashForManualProvisionUI(String passwordHashForManualProvisionUI) {
        this.passwordHashForManualProvisionUI = passwordHashForManualProvisionUI;
    }

    public String getPasswordHashForRemoveDefaultSystemAndUserRestrictions() {
        return passwordHashForRemoveDefaultSystemAndUserRestrictions;
    }

    public void setPasswordHashForRemoveDefaultSystemAndUserRestrictions(String passwordHashForRemoveDefaultSystemAndUserRestrictions) {
        this.passwordHashForRemoveDefaultSystemAndUserRestrictions = passwordHashForRemoveDefaultSystemAndUserRestrictions;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String[] getApks() {
        return apks;
    }

    public void setApks(String[] apks) {
        this.apks = apks;
    }

    public String getPasswordPattern() {
        return passwordPattern;
    }

    public void setPasswordPattern(String passwordPattern) {
        this.passwordPattern = passwordPattern;
    }

    public boolean isAppPasswordProtection() {
        return appPasswordProtection;
    }

    public void setAppPasswordProtection(boolean appPasswordProtection) {
        this.appPasswordProtection = appPasswordProtection;
    }

    public String getPrivateDNS() {
        return privateDNS;
    }

    public void setPrivateDNS(String privateDNS) {
        this.privateDNS = privateDNS;
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
    }

    private JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (address != null) {
                jsonObject.put(ADDRESS, address);
            }
            if (name != null) {
                jsonObject.put(NAME, name);
            }
            if (abbr != null) {
                jsonObject.put(ABBR, abbr);
            }
            if (tag != null) {
                jsonObject.put(TAG, tag);
            }
            if (passwordHashForManualProvisionUI != null) {
                jsonObject.put(PASSWORD_HASH_ForManualProvisionUI, passwordHashForManualProvisionUI);
            }
            if (passwordHashForRemoveDefaultSystemAndUserRestrictions != null) {
                jsonObject.put(PASSWORD_HASH_ForRemoveDefaultSystemAndUserRestrictions, passwordHashForRemoveDefaultSystemAndUserRestrictions);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsonObject;
    }

    private static EntityInfo parse(JSONObject jsonObject) {
        String address = null, name = null, abbr = null, tag = null;
        try {
            address = jsonObject.getString(ADDRESS);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        try {
            name = jsonObject.getString(NAME);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        try {
            abbr = jsonObject.getString(ABBR);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        try {
            tag = jsonObject.getString(TAG);
        } catch (JSONException ex) {
        }
        EntityInfo entityInfo = new EntityInfo(name, abbr, address, tag);
        try {
            String s = jsonObject.getString(PASSWORD_HASH_ForManualProvisionUI);
            entityInfo.setPasswordHashForManualProvisionUI(s);
        } catch (JSONException ex) {
        }
        try {
            String s = jsonObject.getString(PASSWORD_HASH_ForRemoveDefaultSystemAndUserRestrictions);
            entityInfo.setPasswordHashForRemoveDefaultSystemAndUserRestrictions(s);
        } catch (JSONException ex) {
        }

        return entityInfo;
    }

    public static EntityInfo parse(String s) {
        EntityInfo entityInfo = null;
        try {
            JSONObject jsonObject = new JSONObject(s);
            entityInfo = parse(jsonObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return entityInfo;
    }

    // do not use getStringForSignature; otherwise need to tweak Jackson
    public String toStringForSignature() {
        StringBuffer s = new StringBuffer();
        s.append(name).append("|");
        s.append(abbr).append("|");
        s.append(address).append("|");
        s.append(tag).append("|");
        s.append(passwordHashForManualProvisionUI).append("|");
        s.append(passwordHashForRemoveDefaultSystemAndUserRestrictions).append("|");
        return s.toString();
    }

    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }

    public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }

    public String getPasswordForManualProvisionUI() {
        return passwordForManualProvisionUI;
    }

    public void setPasswordForManualProvisionUI(String passwordForManualProvisionUI) {
        this.passwordForManualProvisionUI = passwordForManualProvisionUI;
    }

    public String getPasswordForRemoveDefaultSystemAndUserRestrictions() {
        return passwordForRemoveDefaultSystemAndUserRestrictions;
    }

    public void setPasswordForRemoveDefaultSystemAndUserRestrictions(String passwordForRemoveDefaultSystemAndUserRestrictions) {
        this.passwordForRemoveDefaultSystemAndUserRestrictions = passwordForRemoveDefaultSystemAndUserRestrictions;
    }
}