package org.feitian.dpc;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.UserManager;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.afwsamples.testdpc.BuildConfig;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.pfs.dpc.entity.EntityInfo;
import com.pfs.dpc.entity.PFSPublic;
import com.pfs.dpc.qrcode.Profile;
import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.restriction.RestrictionPolicy;

import org.feitian.dpc.util.Store;

import java.util.Date;

public class PFSProfileActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pfs_profile);
        getProfile();
     }

     private void getProfile() {
         getEntityInfo();
         getKnoxPrivileges();
         getUserRestrictions();
         getAppStage();
         getMisc();
     }

    private void getAppStage() {
        String format = "<p><b>%s:</b> %s</p>";
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        TextView textView = findViewById(R.id.profileTextViewAppStage);

        StringBuffer buffer = new StringBuffer();
        String s = getString(R.string.profile_app_stage);
        buffer.append(s);
        String str = String.format(format, "App Version", BuildConfig.VERSION_NAME);
        buffer.append(str);
        str = String.format(format, "Knox License Activated", pref.getBoolean(Profile.KNOX_LICENSE_ACTIVATED, false) ? "true" : "false");
        buffer.append(str);
        str = String.format(format, "Knox License Skipped", pref.getBoolean(Profile.KNOX_LICENSE_SKIPPED, false) ? "true" : "false");
        buffer.append(str);
        str = String.format(format, "Built-in Apps Installed", pref.getBoolean(Profile.BUILT_IN_APPS_INSTALLED, false) ? "true" : "false");
        buffer.append(str);
        str = String.format(format, "Policy Deployed", pref.getBoolean(Profile.POLICY_DEPLOYED, false) ? "true" : "false");
        buffer.append(str);
        textView.setText(Html.fromHtml(buffer.toString(), Html.FROM_HTML_MODE_COMPACT));
    }

    private void getMisc() {
        String format = "<p><b>%s:</b> %s</p>";
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        TextView textView = findViewById(R.id.profileTextViewMisc);

        StringBuffer buffer = new StringBuffer();
        String s = getString(R.string.profile_misc);
        buffer.append(s);

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        long time = pref.getLong(Profile.INITIAL_PROVISION, 0);
        String str = String.format(format, "Initial Provision", time > 0 ? (new Date(time)).toString() : "N/A");
        buffer.append(str);
        str = String.format(format, "Device Owner", devicePolicyManager.isDeviceOwnerApp(getPackageName()) ? "true" : "<font color=\"red\">false</font>");
        buffer.append(str);
        String pattern = pref.getString(Profile.CELLPHONE_PASSWORD_PIN_PATTERN, null);
        str = String.format(format, "Cellphone Password Pattern",  pattern == null ? "<font color=\"red\">null</font>" : pattern);
        buffer.append(str);
        str = String.format(format, "App Password Protection",  pref.getBoolean(Profile.APP_PASSWORD_PROTECTION, false) ? "true" : "false");
        buffer.append(str);
        str = String.format(format, "Lock Screen Image", pref.getString(Profile.LOCK_SCRREN_IMAGE, FirstActivity.LOGO_FILE_NAME));
        buffer.append(str);
        str = String.format(format, "Public Key", pref.getString(Profile.PUBLIC_KEY, getPublicKey()));
        buffer.append(str);
        str = String.format(format, "UUID", pref.getString(Profile.UUID, null));
        buffer.append(str);
        textView.setText(Html.fromHtml(buffer.toString(), Html.FROM_HTML_MODE_COMPACT));
    }

    private void getEntityInfo() {
         SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
         TextView textView = findViewById(R.id.profileTextViewEntityInfo);

         String s = getString(R.string.profile_entity_info);
         EntityInfo entityInfo = Store.loadEntityInfo(pref);
         if (entityInfo == null) {
             entityInfo = EntityInfo.getDefaultEntityInfo();
         }
         String str = String.format(s, entityInfo.getName(), entityInfo.getAbbr(), entityInfo.getAddress(), entityInfo.getTag(), entityInfo.getPasswordHashForManualProvisionUI() == null ? "missing" : "present");

         textView.setText(Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT));
     }

    private void getKnoxPrivileges() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        TextView textView = findViewById(R.id.profileTextViewKnoxPrivilege);

        boolean activated = pref.getBoolean(Profile.KNOX_LICENSE_ACTIVATED, false);

        if (activated) {
            boolean allowBluetoothDataTransfer = false;
            boolean allowFactoryReset = false;
            boolean allowFirmwareRecovery = false;
            boolean allowOTAUpgrade = false;
            boolean allowSafeMode = false;
            boolean allowUsbHostStorage = false;
            boolean allowWiFi = false;
            boolean allowWiFiDirect = false;
            boolean allowWallpaperChange = false;
            boolean setCellularData = false;
            boolean setLockScreenState = false;
            boolean setSdCardState = false;
            boolean setUsbDebuggingEnabled = false;
            boolean setWifiTethering = false;

            // Instantiate the EnterpriseDeviceManager class - this line has error if obfuscation is in use, even we use -keep class com.samsung.** { *; }
            EnterpriseDeviceManager enterpriseDeviceManager = EnterpriseDeviceManager.getInstance(this.getApplicationContext());
            // Get the RestrictionPolicy class where the setCameraState method lives
            RestrictionPolicy restrictionPolicy = enterpriseDeviceManager.getRestrictionPolicy();
            if (restrictionPolicy != null) {
                allowBluetoothDataTransfer = enterpriseDeviceManager.getBluetoothPolicy().getAllowBluetoothDataTransfer();
                allowFactoryReset = restrictionPolicy.isFactoryResetAllowed();
                allowFirmwareRecovery = restrictionPolicy.isFirmwareRecoveryAllowed(false);
                allowOTAUpgrade = restrictionPolicy.isOTAUpgradeAllowed();
                allowSafeMode = restrictionPolicy.isSafeModeAllowed();
                allowUsbHostStorage = restrictionPolicy.isUsbHostStorageAllowed();
                allowWiFi = restrictionPolicy.isWiFiEnabled(false);
                allowWiFiDirect = restrictionPolicy.isWifiDirectAllowed();
                allowWallpaperChange = restrictionPolicy.isWallpaperChangeAllowed();
                setCellularData = restrictionPolicy.isCellularDataAllowed();
                setLockScreenState = restrictionPolicy.isLockScreenEnabled(false);
                setSdCardState = restrictionPolicy.isSdCardEnabled();
                setUsbDebuggingEnabled = restrictionPolicy.isUsbDebuggingEnabled();
                setWifiTethering = restrictionPolicy.isWifiTetheringEnabled();

                String s = getString(R.string.profile_knox_privilege);
                String str = String.format(s,
                        allowBluetoothDataTransfer ? "<font color=\"red\">true</font>" : "false",
                        allowFactoryReset ? "<font color=\"red\">true</font>" : "false",
                        allowFirmwareRecovery ? "<font color=\"red\">true</font>" : "false",
                        allowOTAUpgrade ? "true" : "false",
                        allowSafeMode ? "<font color=\"red\">true</font>" : "false",
                        allowUsbHostStorage ? "<font color=\"red\">true</font>" : "false",
                        allowWiFi ? "<font color=\"red\">true</font>" : "false",
                        allowWiFiDirect ? "<font color=\"red\">true</font>" : "false",
                        setCellularData ? "true" : "<font color=\"red\">false</font>",
                        setLockScreenState ? "<font color=\"red\">true</font>" : "false",
                        setSdCardState ? "<font color=\"red\">true</font>" : "false",
                        setUsbDebuggingEnabled ? "<font color=\"red\">true</font>" : "false",
                        setWifiTethering ? "<font color=\"red\">true</font>" : "false",
                        allowWallpaperChange ? "<font color=\"red\">true</font>" : "false"
                        );

                textView.setText(Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT));
            } else {
                String s = getString(R.string.profile_knox_privilege_error);
                textView.setText(Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT));
            }
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void getUserRestrictions() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        boolean knoxActivated = pref.getBoolean(Profile.KNOX_LICENSE_ACTIVATED, false);
        boolean knoxSkipped = pref.getBoolean(Profile.KNOX_LICENSE_SKIPPED, false);

        TextView textView = findViewById(R.id.profileTextViewUserRestriction);

        StringBuffer buffer = new StringBuffer();
        String s = getString(R.string.profile_user_restriction);
        buffer.append(s);

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponentName = DeviceAdminReceiver.getComponentName(this);
        try {
            String format = "<p>%d. %s: %s</p>";
            Bundle bundle = devicePolicyManager.getUserRestrictions(adminComponentName);
            int index = 1;
            for (String key : UserRestrictions.restrictons) {
                String str = String.format(format, index++, key, bundle.getBoolean(key) ? "true" : "<font color=\"red\">false</font>");
                buffer.append(str);
            }
            buffer.append("<br>");
            for (String key : UserRestrictions.noRestrictons) {
                if (key.equals(UserManager.DISALLOW_FACTORY_RESET) || key.equals(UserManager.DISALLOW_SAFE_BOOT)) {
                    boolean usingKnoxPrivilege = false;
                    if (usingKnoxPrivilege) {
                        if (knoxActivated) {
                            String str = String.format(format, index++, key, bundle.getBoolean(key) ? "<font color=\"red\">true</font>" : "false");
                            buffer.append(str);
                        } else if (knoxSkipped) {
                            String str = String.format(format, index++, key, bundle.getBoolean(key) ? "true" : "<font color=\"red\">false</font>");
                            buffer.append(str);
                        } else {
                            String str = String.format(format, index++, key, bundle.getBoolean(key) ? "true" : "<font color=\"red\">false</font>");
                            buffer.append(str);
                        }
                    } else {
                        String str = String.format(format, index++, key, bundle.getBoolean(key) ? "true" : "<font color=\"red\">false</font>");
                        buffer.append(str);
                    }
                } else {
                    String str = String.format(format, index++, key, bundle.getBoolean(key) ? "<font color=\"red\">true</font>" : "false");
                    buffer.append(str);
                }
            }

            textView.setText(Html.fromHtml(buffer.toString(), Html.FROM_HTML_MODE_COMPACT));
        } catch (Exception ex) {
            String str = getString(R.string.profile_user_restriction_error);
            textView.setText(Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private String getPublicKey() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        String s = pref.getString(Profile.PUBLIC_KEY, PFSPublic.PUBLIC_KEY);
        return s;
    }
}
