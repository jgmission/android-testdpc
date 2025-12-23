package org.feitian.dpc;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.pfs.dpc.qrcode.Profile.APP_PASSWORD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.UserManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuCompat;

import com.afwsamples.testdpc.BuildConfig;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.PolicyManagementActivity;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.AppInfoArrayAdapter;
import com.afwsamples.testdpc.common.PackageInstallationUtils;
import com.afwsamples.testdpc.policy.UserRestrictionsDisplayFragment;
import com.pfs.dpc.entity.EntityInfo;
import com.pfs.dpc.entity.PFSPublic;
import com.pfs.dpc.knox.LicenseHelper;
import com.pfs.dpc.qrcode.DPCUICode;
import com.pfs.dpc.qrcode.KnoxPrivilege;
import com.pfs.dpc.qrcode.Profile;
import com.pfs.dpc.qrcode.QRCodeAndroid;
import com.pfs.dpc.qrcode.task.PFSTask;
import com.pfs.dpc.qrcode.task.SetPrivateDNSTask;
import com.pfs.util.Base64;
import com.pfs.util.CryptoHelper;
import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.application.ApplicationPolicy;
import com.samsung.android.knox.bluetooth.BluetoothPolicy;
import com.samsung.android.knox.devicesecurity.PasswordPolicy;
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager;
import com.samsung.android.knox.license.LicenseResult;
import com.samsung.android.knox.license.LicenseResultCallback;
import com.samsung.android.knox.restriction.RestrictionPolicy;

import org.feitian.dpc.util.Store;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class FirstActivity extends Activity {
    public final static String PACKAGE_NAME = "com.afwsamples.testdpc";
    public final static String ZXING = "com.google.zxing.client.android";
    private enum DialogAction {manualProvision, removeRestrictions, login, setAppPassword};

    private final static String TAG = FirstActivity.class.getName();
    private final static int REQUEST_ZXING = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int SELECT_PICTURE = 300;

    private final static String defaultWallpaper = "31"; // "1" should be "01";
    public final static String LOGO_FILE_NAME = "logo.jpg";

    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;
    private UserManager mUserManager;
    private ComponentName mAdminComponentName;
    EntityInfo entityInfo = null;

    private String appToInstallFromInternetURL = null;

    private boolean activateKnoxLicense = false;
    private boolean deactivateKnoxLicense = false;
    private String knoxLicense = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        entityInfo = Store.loadEntityInfo(pref);
        if (entityInfo == null) {
            entityInfo = EntityInfo.getDefaultEntityInfo();
        }

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mPackageManager = getPackageManager();
        mUserManager = (UserManager) getSystemService(Context.USER_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
        // remove the app password saved in previous usage of the app
        removeAppPassword();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        setActivityTitle();

        boolean setup= pref.getBoolean(Profile.BUILT_IN_APPS_INSTALLED, false);
        if (!setup) {
            toast("Copying images to Gallary ...", ToastType.info);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(Profile.BUILT_IN_APPS_INSTALLED, true);
            editor.commit();

            //save images to gallery for wallpaper setup
            saveImagesToGallary();

            toast("Installing apps ...", ToastType.info);
            setUserRestrictionsForInstall(false);
            // apk files should be put into assets folder. Now we can download and install app
            // from the Internet, so this feature is not that useful anymore. But we can still use
            // it for barcode scanner.
            String[] apks = {
                    "com.google.zxing.client.android.apk"
            };
            for (String apk : apks) {
                installAppFromAssets(apk);
                toast("Installed " + apk, ToastType.info);
            }
            toast("Installed all apps.", ToastType.info);
        }

        //hide manual provision button
        findViewById(R.id.provision).setVisibility(View.GONE);

        //knox license button
        setup = pref.getBoolean(Profile.KNOX_LICENSE_ACTIVATED, false) | pref.getBoolean(Profile.KNOX_LICENSE_SKIPPED, false);
        Button button = findViewById(R.id.knoxlicensebutton);
        if (!setup) {
            //if knox license is not activated or skipped, show the button
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }

        // hide some buttons
        findViewById(R.id.appsbutton).setVisibility(View.GONE);
        findViewById(R.id.appprotectionbutton).setVisibility(View.GONE);
        findViewById(R.id.hideappsbtn).setVisibility(View.GONE);
        findViewById(R.id.unhideappbtns).setVisibility(View.GONE);
        findViewById(R.id.uninstallappsbtn).setVisibility(View.GONE);

        //setup restrictions button
        //initialize knox stuff - we cannot initialize knox just after the call of knox license activation
        // because activation goes to internet and takes time, and it is asynchronous. The knox initialization
        //will fail because activation is not complete yet. There is a broadcast intent to show that
        // activation is complete and we should implement in that way - but we run out of time.

        if (isAppPasswordProtected()) {
            // app password protection is enabled
            button = findViewById(R.id.appprotectionbutton);
            button.setVisibility(View.VISIBLE);
            String appPasswordHash = getAppPasswordHash();
            if (appPasswordHash == null) {
                // password is not set yet
                button.setText("Set Password");
                button.setBackgroundResource(R.drawable.button2_red);
            } else {
                if (isLoggedIn()) {
                    // password is set and logged in
                    button.setText("Logout");
                    button.setBackgroundResource(R.drawable.button2_red);
                    findViewById(R.id.hideappsbtn).setVisibility(View.VISIBLE);
                    findViewById(R.id.unhideappbtns).setVisibility(View.VISIBLE);
                    findViewById(R.id.uninstallappsbtn).setVisibility(View.VISIBLE);
                } else {
                    // password is set but not logged in yet
                    button.setText("Login");
                    button.setBackgroundResource(R.drawable.button2);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (knoxLicense != null) {
            try {
                if (activateKnoxLicense) {
                    activateKnoxLicense = false;
                    activateKnoxLicense(knoxLicense);
                } else if (deactivateKnoxLicense) {
                    deactivateKnoxLicense = false;
                    deactivateKnoxLicense(knoxLicense);
                }
            } finally {
                knoxLicense = null;
            }
        }
    }

    private String getAppPasswordHash() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        String appPasswordHash = pref.getString(Profile.APP_PASSWORD_HASH, null);
        return appPasswordHash;
    }

    private boolean isLoggedIn() {
        String appPasswordHash = getAppPasswordHash();
        if (appPasswordHash == null) {
            return false;
        } else {
            boolean inAlready = false;
            try {
                inAlready = appPasswordHash.equals(CryptoHelper.getSHA256HashHex(getAppPassword()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return inAlready;
        }
    }

    private boolean isAppPasswordProtected () {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        boolean appPasswordProtection = pref.getBoolean(Profile.APP_PASSWORD_PROTECTION, false);
        return appPasswordProtection;
    }

    private void setActivityTitle() {
        // Customize activity title
        if (entityInfo.getAbbr() != null && entityInfo.getAbbr().trim().length() > 0 && !entityInfo.getAbbr().trim().equals("PFS")) {
            this.setTitle(entityInfo.getAbbr().trim() + " PFS " + BuildConfig.VERSION_NAME);
        } else {
            this.setTitle("PFS " + BuildConfig.VERSION_NAME);
        }
    }

    public void onProvision(View view) {
        currentSavedPasswordHash = entityInfo.getPasswordHashForManualProvisionUI();
        showPasswordDialog(DialogAction.manualProvision);
    }

    public void onQrCode(View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            intent.putExtra("SAVE_HISTORY",false);
            // portrait orientation does not work
            //intent.putExtra("preferences_orientation", true);
            startActivityForResult(intent, REQUEST_ZXING);
        } catch (Exception e) {
            toastLong("zxing is installed. Tap the button again to scan QR code.", ToastType.warning);
        }
    }

    private boolean isPackageExisted(String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private void uninstallPackages(String[] packages) {
        try {
            for (String packageName : packages) {
                try {
                    PackageInstallationUtils.uninstallPackage(getApplicationContext(), packageName);
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private PublicKey getPublicKey() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        String s = pref.getString(Profile.PUBLIC_KEY, PFSPublic.PUBLIC_KEY);
        return CryptoHelper.getPublicKey(s);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ZXING) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                try {
                    QRCodeAndroid qrCodeAndroid = QRCodeAndroid.parse(contents);
                    if (qrCodeAndroid == null) {
                        toast("QR code cannot be parsed!", ToastType.error);
                    } else if (qrCodeAndroid.validateSignatire(getPublicKey())) {
                        if (qrCodeAndroid.getExpireDate().getTime() > System.currentTimeMillis()) {
                            toast("QR code is valid!", ToastType.info);
                            processQRCode(qrCodeAndroid);
                        } else {
                            toast("QR code expired!", ToastType.error);
                        }
                    } else {
                        toast("Signature is not valid!", ToastType.error);
                    }
                } catch (Exception ex) {
                    showStacktraceInDialog(ex);
                    return;
                }
            } else if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        } else if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = getBitmapFromUri(selectedImageUri);
                    if (bitmap != null) {
                        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                        myWallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_SYSTEM);
                        toast("Home screen wallpaper is set successfully.", ToastType.info);
                    } else {
                        throw new Exception("Bitmap is null.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
//                    toast("Failed to set wallpaper!", ToastType.error);
                    showStacktraceInDialog(ex);
                }
            }
        }
    }

    private void showStacktraceInDialog(Exception ex) {
        ex.printStackTrace();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        showDialog("Stacktrace", sw.toString());
    }

    private boolean userRestrictionFragmentShown = false;
    private void showFragment(final Fragment fragment) {
        findViewById(R.id.menulayout).setVisibility(View.INVISIBLE);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(FirstActivity.class.getName()).replace(R.id.mycontainer, fragment).commit();
        userRestrictionFragmentShown = true;
    }

    @Override
    public void onBackPressed() {
        if (userRestrictionFragmentShown) {
            Intent intent = new Intent(this, FirstActivity.class);
            startActivity(intent);
            userRestrictionFragmentShown = false;
        } else {
            super.onBackPressed();
        }
    }

    private void processQRCode(QRCodeAndroid qrCodeAndroid) {
        if (isAppPasswordProtected()) {
            if (!isLoggedIn()) {
                // if app password protection is enable but not logged in yet
                if (qrCodeAndroid.isClearAppPassword()) {
                    clearAppPassword();
                    return;
                } else if (qrCodeAndroid.isDisableAppPasswordProtection()) {
                    disableAppPasswordProtection();
                    return;
                } else {
                    // we do not process any other qr code
                    toastLong("Please login first!", ToastType.error);
                    return;
                }
            } else {
                // this is ok and do not quit
            }
        }
        //show ui
        if (qrCodeAndroid.getUiCode() != DPCUICode.NONE) {
            if (qrCodeAndroid.getUiCode() == DPCUICode.WHOLE) {
                Intent intent = new Intent(FirstActivity.this, PolicyManagementActivity.class);
                startActivity(intent);
            } else if (qrCodeAndroid.getUiCode() == DPCUICode.HIDE_APPS) {
                showHideAppsPrompt();
            } else if (qrCodeAndroid.getUiCode() == DPCUICode.UNHIDE_APPS) {
                showUnhideAppsPrompt();
            } else if (qrCodeAndroid.getUiCode() == DPCUICode.UNINSTALL_APPS) {
                showUninstallAppsPrompt();
            } else if (qrCodeAndroid.getUiCode() == DPCUICode.SET_USER_RESTRICTIONS) {
                //TODO user restrictions
                showFragment(new UserRestrictionsDisplayFragment());
            }
        } if (qrCodeAndroid.isShowProfile()) {
            Intent myIntent = new Intent(this, PFSProfileActivity.class);
            this.startActivity(myIntent);
        } else if (qrCodeAndroid.isKnoxActivateLicense()) {
            //knox
            activateKnoxLicense = true;
            knoxLicense = LicenseHelper.unobfuscate(qrCodeAndroid.getKnoxLicenseKeyObfuscated());
        } else if (qrCodeAndroid.isKnoxDeactivateLicense()) {
            //knox
            deactivateKnoxLicense = true;
            knoxLicense = LicenseHelper.unobfuscate(qrCodeAndroid.getKnoxLicenseKeyObfuscated());
        } else {
            // set knox license as skipped
            if (qrCodeAndroid.isKnoxSkipLicense()) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
                if (pref.getBoolean(Profile.KNOX_LICENSE_ACTIVATED, false)) {
                    toastLong("System license is activated! It cannot be skipped anymore.", ToastType.error);
                } else {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Profile.KNOX_LICENSE_SKIPPED, true);
                    editor.commit();
                    toast("System license is skipped", ToastType.info);
                }
            }

            // remove all knox and user restrictions - put this in the beginning because some operations
            // below may need some permission
            if (qrCodeAndroid.isRemoveAllKnoxAndUserRestrictions()) {
                removeRestrictions();
                toast("Restrictions removed!", ToastType.info);
            }

            //remove user restrictions  - put this in the beginning because some operations
            // below may need some permission
            setUserRestrictions(null, qrCodeAndroid.getUserRestrictionsToRemove());
            if (qrCodeAndroid.getUserRestrictionsToRemove().length > 0) {
                toast("Removing user restriction is done!", ToastType.info);
            }

            // clear app password
            if (qrCodeAndroid.isClearAppPassword()) {
                clearAppPassword();
            }

            // enable app password protection
            if (qrCodeAndroid.isEnableAppPasswordProtection()) {
                if (isAppPasswordProtected()) {
                    toast("App password protection is already enabled.", ToastType.info);
                } else {
                    enableAppPasswordProtection();
                }
            }

            // disable app password protection
            if (qrCodeAndroid.isDisableAppPasswordProtection()) {
                if (!isAppPasswordProtected()) {
                    toast("App password protection is already disabled.", ToastType.info);
                } else {
                    disableAppPasswordProtection();
                }
            }

            //install apps, image as wallpaper and PFS config file
            String[] appsToInstall = qrCodeAndroid.getApksAndImage();
            for (String app : appsToInstall) {
                if (app.startsWith("https://")) {
                    //from website
                    installAppFromInternet(app);
                } else {
                    //from assets
                    installAppFromAssets(app);
                }
            }

            //uninstall packages
            String packagesToUninstall[] = qrCodeAndroid.getPackagesToUninstall();
            uninstallPackages(packagesToUninstall);

            //hide apps
            String appsToHide[] = qrCodeAndroid.getAppsToHide();
            hideApps(appsToHide);
            if (appsToHide.length > 0) {
                toast("Hidding apps is done!", ToastType.info);
            }

            //unhide apps
            String appsToUnhide[] = qrCodeAndroid.getAppsToUnhide();
            unhideApps(appsToUnhide);
            if (appsToUnhide.length > 0) {
                toast("Unhidding apps is done!", ToastType.info);
            }

            //remove device owner
            if (qrCodeAndroid.isRemoveThisDeviceOwner()) {
                clearDeviceOwner();
                toast("This app is no longer a device owen.", ToastType.info);
            }

            // PIN
            if (qrCodeAndroid.getResetPassword() != null && qrCodeAndroid.getResetPassword().trim().length() > 0) {
                if (processKnoxSetRequiredPasswordPattern(qrCodeAndroid.getResetPassword().trim())) {
                }
            }

            //disable apps
            String appsToDisable[] = qrCodeAndroid.getAppsToDisable();
            disableApps(appsToDisable);

            //enable apps
            String appsToEnable[] = qrCodeAndroid.getAppsToEnable();
            enableApps(appsToEnable);

            // Entity info
            EntityInfo entityInfo = qrCodeAndroid.getEntityInfo();
            if (entityInfo != null) {
                setEntityInfo(entityInfo);
            }

            // PFSTask
            if (qrCodeAndroid.getTasks() != null) {
                for (PFSTask task : qrCodeAndroid.getTasks()) {
                    if (task instanceof SetPrivateDNSTask) {
                        SetPrivateDNSTask setPrivateDNSTask = (SetPrivateDNSTask) task;
                        String dns = setPrivateDNSTask.getDnsServer();
                        if (SDK_INT >= 29) {
                            Thread t = new Thread() {
                                @Override
                                public void run() {
                                    // does not work - it always returns "Provided host does not support DNS-over-TLS" error.
                                    mDevicePolicyManager.setGlobalPrivateDnsModeSpecifiedHost(mAdminComponentName, dns);
                                }
                            };
                            t.start();
                        } else {
                            toastLong("OS version does not support private DNS setup!", ToastType.error);
                        }
                    }
                }
            }

            //initialize default knox and user restrictions - put this at the end because previous
            //operations may need some permissions
            if (qrCodeAndroid.isInitializeDefaultKnoxAndUserRestrictions()) {
                initializeRestrictions();
                toast("Restrictions initialized!", ToastType.info);
            }

            //add user restrictions - put this at the end because previous
            //operations may need some permissions
            setUserRestrictions(qrCodeAndroid.getUserRestrictionsToAdd(), null);
            if (qrCodeAndroid.getUserRestrictionsToAdd().length > 0) {
                toast("Adding user restriction is done!", ToastType.info);
            }

            // set knox restrictions
            Map<Integer, Boolean> knoxMap = qrCodeAndroid.getKnoxMap();
            if (knoxMap != null) {
                for (Map.Entry<Integer, Boolean> entry : knoxMap.entrySet()) {
                    processKnoxRestriction(entry.getKey(), entry.getValue().booleanValue());
                }
            }
        }
    }

    private void enableAppPasswordProtection() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Profile.APP_PASSWORD_PROTECTION, true);
        editor.remove(Profile.APP_PASSWORD_HASH);
        editor.commit();
        toastLong("App password protection has be enabled successfully.", ToastType.info);
        findViewById(R.id.appprotectionbutton).setVisibility(View.VISIBLE);
        findViewById(R.id.appprotectionbutton).setBackgroundResource(R.drawable.button2_red);
        ((Button) findViewById(R.id.appprotectionbutton)).setText("Set Password");
        removeAppPassword();
        findViewById(R.id.hideappsbtn).setVisibility(View.GONE);
        findViewById(R.id.unhideappbtns).setVisibility(View.GONE);
        findViewById(R.id.uninstallappsbtn).setVisibility(View.GONE);
    }

    private void disableAppPasswordProtection() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(Profile.APP_PASSWORD_HASH);
        editor.putBoolean(Profile.APP_PASSWORD_PROTECTION, false);
        editor.commit();
        toastLong("App password protection has be disabled successfully.", ToastType.info);
        removeAppPassword();
        findViewById(R.id.appprotectionbutton).setVisibility(View.GONE);
        findViewById(R.id.hideappsbtn).setVisibility(View.GONE);
        findViewById(R.id.unhideappbtns).setVisibility(View.GONE);
        findViewById(R.id.uninstallappsbtn).setVisibility(View.GONE);
    }

    private void clearAppPassword() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(Profile.APP_PASSWORD_HASH);
        editor.commit();
        removeAppPassword();
        toastLong("App password has be cleared successfully.", ToastType.info);
        if (isAppPasswordProtected()) {
            findViewById(R.id.appprotectionbutton).setVisibility(View.VISIBLE);
            findViewById(R.id.appprotectionbutton).setBackgroundResource(R.drawable.button2_red);
            ((Button) findViewById(R.id.appprotectionbutton)).setText("Set Password");
        } else {
            findViewById(R.id.appprotectionbutton).setVisibility(View.GONE);
        }
        findViewById(R.id.hideappsbtn).setVisibility(View.GONE);
        findViewById(R.id.unhideappbtns).setVisibility(View.GONE);
        findViewById(R.id.uninstallappsbtn).setVisibility(View.GONE);
    }

    private void setEntityInfo(EntityInfo entityInfo) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        Store.saveEntityInfo(pref, entityInfo);
        this.entityInfo = entityInfo;
        setActivityTitle();
    }

    private void setWallpaper(String filename, int which) {
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        boolean set = false;
        try {
            AssetManager assets = getApplicationContext().getResources().getAssets();
            String[] names = assets.list("");
            if (names != null && names.length > 0) {
                for (String name : names) {
                    if (name.equalsIgnoreCase(filename))  {
                        Bitmap bitmap = getBitmapFromAsset(getApplicationContext(), name);
                        if (bitmap != null) {
                            myWallpaperManager.setBitmap(bitmap, null, false, which);
                            set = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
//            toastLong("Images have not been installed. " + e.getMessage(), ToastType.error);
            showStacktraceInDialog(e);
            Log.e("ERROR", "Failed to install images", e);
        }
        if (set) {
            toast("Wallpaper has been set successfully.", ToastType.info);
        } else {
            toast("Wallpaper cannot be set properly.", ToastType.error);
        }
    }

    private boolean processKnoxSetRequiredPasswordPattern(String regex) {
        if (!isDeviceOwner()) {
            toast("Not a device owner!", ToastType.error);
            return false;
        } else {
            // Instantiate the EnterpriseDeviceManager class
            EnterpriseDeviceManager enterpriseDeviceManager = EnterpriseDeviceManager.getInstance(this.getApplicationContext());
            PasswordPolicy passwordPolicy = enterpriseDeviceManager.getPasswordPolicy();
            try {
                //disable biometric authentication
                passwordPolicy.setBiometricAuthenticationEnabled(PasswordPolicy.BIOMETRIC_AUTHENTICATION_FINGERPRINT |
                        PasswordPolicy.BIOMETRIC_AUTHENTICATION_IRIS | PasswordPolicy.BIOMETRIC_AUTHENTICATION_FACE, false);
                // enable visible pattern - this is to show the lines between dots or not when use pattern to unlock the screen
                passwordPolicy.setScreenLockPatternVisibilityEnabled(true);

                boolean result = passwordPolicy.setRequiredPasswordPattern(regex);
                if (true == result) {
                    // password pattern set
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Profile.CELLPHONE_PASSWORD_PIN_PATTERN, regex);
                    editor.commit();
                    toast("Password pattern " + regex + " is set.", ToastType.info);
                } else {
                    toast("Password pattern " + regex + " is not set.", ToastType.error);
                }
                return result;
            } catch (SecurityException e) {
                Log.w(TAG, "SecurityException: " + e);
                toast(e.getMessage(), ToastType.error);
                return false;
            }
        }
    }

    private boolean processKnoxRestriction(Integer key, boolean booleanValue) {
        if (!isDeviceOwner()) {
            toast("Not a device owner!", ToastType.error);
            return false;
        } else {
            SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
            if (pref.getBoolean(Profile.KNOX_LICENSE_SKIPPED, false)) {
                toastLong("System license is skipped!", ToastType.warning);
                return true;
            }
            if (!pref.getBoolean(Profile.KNOX_LICENSE_ACTIVATED, false)) {
                toastLong("System license is not activated!", ToastType.error);
                return false;
            }
            // Instantiate the EnterpriseDeviceManager class - this line has error if obfuscation is in use, even we use -keep class com.samsung.** { *; }
            EnterpriseDeviceManager enterpriseDeviceManager = EnterpriseDeviceManager.getInstance(this.getApplicationContext());
            // Get the RestrictionPolicy class where the setCameraState method lives
            if (enterpriseDeviceManager == null) {
                toastLong("System EnterpriseDeviceManager is null!", ToastType.error);
                return false;
            }
            RestrictionPolicy restrictionPolicy = enterpriseDeviceManager.getRestrictionPolicy();
            if (restrictionPolicy == null) {
                toastLong("System RestrictionPolicy is null!", ToastType.error);
                return false;
            }
            boolean b = false;
            if (key == KnoxPrivilege.allowFactoryResetIndex) {
                b = restrictionPolicy.allowFactoryReset(booleanValue);
            } else if (key == KnoxPrivilege.allowFirmwareRecoveryIndex) {
                b = restrictionPolicy.allowFirmwareRecovery(booleanValue);
            } else if (key == KnoxPrivilege.allowOTAUpgradeIndex) {
                b = restrictionPolicy.allowOTAUpgrade(booleanValue);
            } else if (key == KnoxPrivilege.allowSafeModeIndex) {
                b = restrictionPolicy.allowSafeMode(booleanValue);
            } else if (key == KnoxPrivilege.allowUsbHostStorageIndex) {
                b = restrictionPolicy.allowUsbHostStorage(booleanValue);
            } else if (key == KnoxPrivilege.allowWallpaperChangeIndex) {
                b = restrictionPolicy.allowWallpaperChange(booleanValue);
            } else if (key == KnoxPrivilege.allowWiFiIndex) {
                b = restrictionPolicy.allowWiFi(booleanValue);
            } else if (key == KnoxPrivilege.allowWiFiDirectIndex) {
                b = restrictionPolicy.allowWifiDirect(booleanValue);
            } else if (key == KnoxPrivilege.setWifiTetheringIndex) {
                b = restrictionPolicy.setWifiTethering(booleanValue);
            } else if (key == KnoxPrivilege.setSdCardStateIndex) {
                b = restrictionPolicy.setSdCardState(booleanValue);
            } else if (key == KnoxPrivilege.setCellularDataIndex) {
                b = restrictionPolicy.setCellularData(booleanValue);
            } else if (key == KnoxPrivilege.setLockScreenStateIndex) {
                b = restrictionPolicy.setLockScreenState(booleanValue);
            } else if (key == KnoxPrivilege.setUsbDebuggingEnabledIndex) {
                b = restrictionPolicy.setUsbDebuggingEnabled(booleanValue);
            } else if (key == KnoxPrivilege.allowBluetoothDataTransferIndex) {
                BluetoothPolicy bluetoothPolicy = enterpriseDeviceManager.getBluetoothPolicy();
                try {
                    b = bluetoothPolicy.setAllowBluetoothDataTransfer(booleanValue);
                } catch (SecurityException e) {
                    Log.w(TAG, "SecurityException: " + e);
                    showStacktraceInDialog(e);
                    return false;
                }
            }
            if (b) {
                toast(key + " is set as " + booleanValue, ToastType.info);
            } else {
                toast(key + " is not set as " + booleanValue, ToastType.error);
            }
            return b;
        }
    }

    private boolean isDeviceOwner() {
        return mDevicePolicyManager.isDeviceOwnerApp(getPackageName());
    }

    private void installAppFromAssets(String app) {
        try {
            AssetManager assets = getApplicationContext().getResources().getAssets();
            String[] names = assets.list("");
            if (names != null && names.length > 0) {
                for (String s : names) {
                    if (s.equals(app)) {
                        InputStream inputStream = assets.open(s);
                        PackageInstallationUtils.installPackage(this, inputStream, null);
                        return;
                    }
                }
            }
            toast(app + " has not been installed.", ToastType.error);
        } catch (Exception e) {
//            toastLong("APK file " + app + " has not been installed. " + e.getMessage(), ToastType.error);
            showStacktraceInDialog(e);
            Log.e("ERROR", "Failed to install APK file", e);
        }
    }

    private void installAppFromInternet(String app) {
        if (checkPermission()) {
            UpdateApp updateApp = new UpdateApp();
            updateApp.setContext(FirstActivity.this);
            updateApp.execute(app);
        } else {
            appToInstallFromInternetURL = app;
            requestPermission();
        }
    }

    public void onExit(View view) {
        finishAffinity();
    }

    public void onActivateKnoxLicense(View view) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode

        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            intent.putExtra("SAVE_HISTORY",false);
            startActivityForResult(intent, REQUEST_ZXING);
        } catch (Exception e) {
            toastLong("zxing is installed. Tap the button again to scan QR code.", ToastType.warning);
        }
    }

    public void deployPolicy(QRCodeAndroid qrCodeAndroid) {
        // before deploy the policy, make checkings and run some pre tasks
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        if (!pref.getBoolean(Profile.KNOX_LICENSE_ACTIVATED, false) && !pref.getBoolean(Profile.KNOX_LICENSE_SKIPPED, false)) {
            toastLong("Run Activate or Skip System License first!", ToastType.error);
            return;
        }

        long time = pref.getLong(Profile.INITIAL_PROVISION, 0);
        if (time == 0) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putLong(Profile.INITIAL_PROVISION, System.currentTimeMillis());
            editor.commit();
        }
        String uuid = pref.getString(Profile.UUID, null);
        if (uuid == null) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(Profile.UUID, UUID.randomUUID().toString());
            editor.commit();
        }
        // set home screen image
        setWallpaper(defaultWallpaper + ".jpg", WallpaperManager.FLAG_SYSTEM);
        // set lock screen image
        setWallpaper(LOGO_FILE_NAME, WallpaperManager.FLAG_LOCK);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Profile.LOCK_SCRREN_IMAGE, LOGO_FILE_NAME);
        editor.commit();

        hideAndUninstallPredefinedApps();

        // this contains the real policy
        processQRCode(qrCodeAndroid);

        editor = pref.edit();
        editor.putBoolean(Profile.POLICY_DEPLOYED, true);
        editor.commit();
    }

    private void removeShortcutFromHomeScreen(String packageName, String className) {
        try {
            //TODO
        } catch (Exception ex) {
            ex.printStackTrace();
            toastLong(ex.getMessage(), ToastType.error);
        }
    }

    public void onApps(View view) {
        Intent intent = new Intent(this, AppSelectionActivity.class);
        startActivity(intent);
    }

    public void onAppProtection(View view) {
        if (isLoggedIn()) {
            // logout
            removeAppPassword();
            findViewById(R.id.hideappsbtn).setVisibility(View.GONE);
            findViewById(R.id.unhideappbtns).setVisibility(View.GONE);
            findViewById(R.id.uninstallappsbtn).setVisibility(View.GONE);
            findViewById(R.id.appprotectionbutton).setBackgroundResource(R.drawable.button2);
            ((Button)findViewById(R.id.appprotectionbutton)).setText("Login");
        } else {
            String appPasswordHash = getAppPasswordHash();
            if (appPasswordHash == null) {
                // set password
                showPasswordDialog(DialogAction.setAppPassword);
            } else {
                // login
                currentSavedPasswordHash = getAppPasswordHash();
                showPasswordDialog(DialogAction.login);
            }
        }
    }
    public void onHideAppsButton(View view) {
        showHideAppsPrompt();
    }

    public void onUnhideAppsButton(View view) {
        showUnhideAppsPrompt();
    }

    public void onUninstallAppsButton(View view) {
        showUninstallAppsPrompt();
    }

    public void hideAndUninstallPredefinedApps() {
        hideApps(ApplicationHidden.hiddens);
        uninstallPackages(PackagesToUninstall.packages);

        removeShortcutFromHomeScreen("com.android.vending", "com.google.android.finsky.activities.MainActivity");

        toastLong("Hide and Uninstall Apps finished!", ToastType.info);
    }

    private void setUserRestrictions(String[] adds, String[] removes) {
        if (adds != null) {
            for (String restriction : adds) {
                try {
                    mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
                } catch (Exception ex) {
                    Log.e("setUserRestrictions (add)", ex.getMessage());
                }
            }
        }
        if (removes != null) {
            for (String restriction : removes) {
                try {
                    mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
                } catch (Exception ex) {
                    Log.e("setUserRestrictions (remove)", ex.getMessage());
                }
            }
        }
    }

    private void setUserRestrictionsForInstall(boolean addRestriction) {
        for (String restriction : UserRestrictions.installRestrictions) {
            try {
                if (addRestriction) {
                    mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
                } else {
                    mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
                }
            } catch (Exception ex) {
                Log.e("setUserRestrictions", ex.getMessage());
            }
        }
    }

    private void hideApps(String[] apps) {
        final int successResId = R.string.hide_apps_success;
        final int failureResId = R.string.hide_apps_failure;
        for (String app : apps) {
            if (app.startsWith(PACKAGE_NAME) || app.startsWith(ZXING)) {
                toast(app + " cannot be hidden!", ToastType.error);
            } else {
                try {
                    if (mDevicePolicyManager.setApplicationHidden(mAdminComponentName,
                            app, true)) {
                        //showToast(successResId, app);
                        Log.e("hideApps", "Hide app succeeded: " + app);
                    } else {
                        //showToast(getString(failureResId, app), Toast.LENGTH_LONG);
                        Log.e("hideApps", "Hide app failed: " + app);
                    }
                } catch (Exception ex) {
                    Log.e("hideApps", "Hide app failed: " + app, ex);
                }
            }
        }
    }

    private void unhideApps(String[] apps) {
        final int successResId = R.string.unhide_apps_success;
        final int failureResId = R.string.unhide_apps_failure;
        for (String app : apps) {
            try {
                if (mDevicePolicyManager.setApplicationHidden(mAdminComponentName,
                        app, false)) {
                    //showToast(successResId, app);
                    Log.e("unhideApps", "Unhide app succeeded: " + app);
                } else {
                    //showToast(getString(failureResId, app), Toast.LENGTH_LONG);
                    Log.e("unhideApps", "Unhide app failed: " + app);
                }
            } catch (Exception ex) {
                Log.e("unhideApps", "Unhide app failed: " + app, ex);
            }
        }
    }

    private boolean disableApps(String[] apps) {
        if (apps == null || apps.length == 0) {
            return true;
        }
        //Disabling Facial Recognition Device Unlock - https://kp-cdn.samsungknox.com/b60a7f0f59df8f466e8054f783fbbfe2.pdf
        // https://docs.samsungknox.com/knox-service-plugin/admin-guide/password-schema.html
//        EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
        EnterpriseDeviceManager edm = EnterpriseDeviceManager.getInstance(this.getApplicationContext());
        ApplicationPolicy appPolicy = edm.getApplicationPolicy();
        boolean result = true;
        for (String app : apps) {
            try {
                boolean r = appPolicy.setDisableApplication(app);
                result = result & r;
            } catch (Exception ex) {
                Log.e("disableApps", "Disabling app failed: " + app, ex);
            }
        }
        if (!result) {
            toast("Disabling apps failed!", ToastType.error);
        } else {
            toast("Disabling apps succeeded!", ToastType.info);
        }
        return result;
    }

    private boolean enableApps(String[] apps) {
        if (apps == null || apps.length == 0) {
            return true;
        }
        //Disabling Facial Recognition Device Unlock - https://kp-cdn.samsungknox.com/b60a7f0f59df8f466e8054f783fbbfe2.pdf
        // https://docs.samsungknox.com/knox-service-plugin/admin-guide/password-schema.html
//        EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
        EnterpriseDeviceManager edm = EnterpriseDeviceManager.getInstance(this.getApplicationContext());
        ApplicationPolicy appPolicy = edm.getApplicationPolicy();
        boolean result = true;
        for (String app : apps) {
            try {
                boolean r = appPolicy.setEnableApplication(app);
                result = result & r;
            } catch (Exception ex) {
                Log.e("enableApps", "Enabling app failed: " + app, ex);
            }
        }
        if (!result) {
            toast("Enabling apps failed!", ToastType.error);
        } else {
            toast("Enabling apps succeeded!", ToastType.info);
        }
        return result;
    }

    private void showDialog(String title, String text) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(FirstActivity.this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);
        dlg.show();
    }

    private void showPasswordDialog(final DialogAction dialogAction) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.passcode_prompt, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.passcode);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                String user_text = (userInput.getText()).toString().trim();

                                /** CHECK FOR USER'S INPUT **/
                                if (dialogAction.equals(DialogAction.setAppPassword)) {
                                    if (user_text.length() < 8) {
                                        toast("Password must be at least 8 characters!", ToastType.error);
                                    } else {
                                        try {
                                            SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
                                            SharedPreferences.Editor editor = pref.edit();
                                            String hash = CryptoHelper.getSHA256HashHex(user_text);
                                            editor.putString(Profile.APP_PASSWORD_HASH, hash);
                                            editor.commit();
                                            toastLong("Password has be saved successfully.", ToastType.info);
                                            findViewById(R.id.appprotectionbutton).setBackgroundResource(R.drawable.button2);
                                            ((Button)findViewById(R.id.appprotectionbutton)).setText("Login");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
//                                            toastLong("Password could not be saved successfully!", ToastType.error);
                                            showStacktraceInDialog(ex);
                                        }
                                    }
                                } else if (isPasswordValid(user_text)) {
                                    if (dialogAction.equals(DialogAction.login)) {
                                        saveAppPassword(user_text.trim());
                                        if (isLoggedIn()) {
                                            findViewById(R.id.hideappsbtn).setVisibility(View.VISIBLE);
                                            findViewById(R.id.unhideappbtns).setVisibility(View.VISIBLE);
                                            findViewById(R.id.uninstallappsbtn).setVisibility(View.VISIBLE);
                                            findViewById(R.id.appprotectionbutton).setBackgroundResource(R.drawable.button2_red);
                                            ((Button)findViewById(R.id.appprotectionbutton)).setText("Logout");
                                        } else {
                                            removeAppPassword();
                                            showDialog("Warning", "Nice try!");
                                        }
                                    } else if (dialogAction.equals(DialogAction.manualProvision)) {
                                        Intent intent = new Intent(FirstActivity.this, PolicyManagementActivity.class);
                                        startActivity(intent);
                                    } else if (dialogAction.equals(DialogAction.removeRestrictions)) {
                                        removeRestrictions();
                                    }
                                } else {
                                    showDialog("Warning", "Nice try!");
                                }
                            }
                        })
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void showToast(int msgId, Object... args) {
        showToast(getString(msgId, args));
    }

    private void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    private void showToast(String msg, int duration) {
        Activity activity = this;
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Toast.makeText(activity, msg, duration).show();
    }

    /**
     * Shows an alert dialog which displays a list non-hidden apps. Clicking an app in the
     * dialog hide the app.
     *
     */
    private void showHideAppsPrompt() {
        Set<String> apps = new HashSet<>();
        final List<String> showApps = new ArrayList<>();
        apps.add(PACKAGE_NAME); // PFS app
        apps.add(ZXING); // bar code scanner
        apps.add("com.samsung.android.dialer"); // phone

        // Find all non-hidden apps with a launcher icon
        for (ResolveInfo res : getAllLauncherIntentResolversSorted()) {
            if (!apps.contains(res.activityInfo.packageName)
                    && !mDevicePolicyManager.isApplicationHidden(mAdminComponentName,
                    res.activityInfo.packageName)) {
                apps.add(res.activityInfo.packageName);
                showApps.add(res.activityInfo.packageName);
            }
        }

        if (showApps.isEmpty()) {
            showToast(R.string.hide_apps_empty);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(this,
                    R.id.pkg_name, showApps, true);
            final int dialogTitleResId;
            // showing a dialog to hide an app
            dialogTitleResId = R.string.hide_apps_title;
            new AlertDialog.Builder(this)
                    .setTitle(getString(dialogTitleResId))
                    .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            String packageName = showApps.get(position);
                            if (mDevicePolicyManager.setApplicationHidden(mAdminComponentName,
                                    packageName, true)) {
                                showDialog("Hide App", packageName);
                            } else {
                                toastLong(packageName + " cannot be hidden.", ToastType.error);
                            }
                        }
                    })
                    .show();
        }
    }

    /**
     * Shows an alert dialog which displays a list hidden apps. Clicking an app in the
     * dialog to unhide the app.
     *
     */
    private void showUnhideAppsPrompt() {
        final List<String> showApps = new ArrayList<>();
        // Find all hidden packages using the GET_UNINSTALLED_PACKAGES flag
        for (ApplicationInfo applicationInfo : getAllInstalledApplicationsSorted()) {
            if (mDevicePolicyManager.isApplicationHidden(mAdminComponentName,
                    applicationInfo.packageName)) {
                showApps.add(applicationInfo.packageName);
            }
        }

        if (showApps.isEmpty()) {
            showToast(R.string.unhide_apps_empty);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(this,
                    R.id.pkg_name, showApps, true);
            final int dialogTitleResId;
            // showing a dialog to unhide an app
            dialogTitleResId = R.string.unhide_apps_title;
            new AlertDialog.Builder(this)
                    .setTitle(getString(dialogTitleResId))
                    .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            String packageName = showApps.get(position);
                            if (mDevicePolicyManager.setApplicationHidden(mAdminComponentName,
                                    packageName, false)) {
                                showDialog("Unhide App", packageName);
                            } else {
                                toastLong(packageName + " cannot be unhidden.", ToastType.error);
                            }
                        }
                    })
                    .show();
        }
    }

    /**
     * Shows an alert dialog which displays a list unhidden apps. Clicking an app in the
     * dialog to uninstall the app.
     *
     */
    private void showUninstallAppsPrompt() {
        Set<String> apps = new HashSet<>();
        final List<String> showApps = new ArrayList<>();
        apps.add(PACKAGE_NAME); // PFS app
        apps.add(ZXING); // bar code scanner
        apps.add("com.samsung.android.dialer"); // phone
        apps.add("com.sec.android.app.camera"); // camera
        apps.add("com.sec.android.app.clockpackage"); // clock
        apps.add("com.samsung.android.app.contacts"); // contacts
        apps.add("com.sec.android.gallery3d"); // gallery
        apps.add("com.android.vending"); // google play store
        apps.add("com.samsung.android.messaging"); // messages
        apps.add("com.sec.android.app.myfiles"); // my files
        apps.add("com.android.settings"); // settings

        // Find all non-hidden apps with a launcher icon
        for (ResolveInfo res : getAllLauncherIntentResolversSorted()) {
            if (!apps.contains(res.activityInfo.packageName)) {
                apps.add(res.activityInfo.packageName);
                showApps.add(res.activityInfo.packageName);
            }
        }

        if (showApps.isEmpty()) {
            showToast("No apps to uninstall!");
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(this,
                    R.id.pkg_name, showApps, true);
            final int dialogTitleResId;
            new AlertDialog.Builder(this)
                    .setTitle("Uninstall Apps")
                    .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            String packageName = showApps.get(position);
                            try {
                                // remove user restriction DISALLOW_UNINSTALL_APPS so that we can uninstall
                                mDevicePolicyManager.clearUserRestriction(mAdminComponentName, UserManager.DISALLOW_UNINSTALL_APPS);
                                PackageInstallationUtils.uninstallPackage(getApplicationContext(), packageName);
                                // set user restriction DISALLOW_UNINSTALL_APPS so that no other apps can be uninstalled
                                mDevicePolicyManager.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_UNINSTALL_APPS);
                                showDialog("Uninstall App", packageName);
                            } catch (Exception ex) {
                                Log.e(TAG, ex.getMessage());
//                                toastLong(packageName + " cannot be uninstalled.", ToastType.error);
                                showStacktraceInDialog(ex);
                            }
                        }
                    })
                    .show();
        }
    }

    private List<ResolveInfo> getAllLauncherIntentResolversSorted() {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> launcherIntentResolvers = mPackageManager
                .queryIntentActivities(launcherIntent, 0);
        Collections.sort(launcherIntentResolvers,
                new ResolveInfo.DisplayNameComparator(mPackageManager));
        return launcherIntentResolvers;
    }

    private List<ApplicationInfo> getAllInstalledApplicationsSorted() {
        List<ApplicationInfo> allApps = mPackageManager.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(mPackageManager));
        return allApps;
    }

    protected void toastLong(String msg, ToastType toastType) {
        _toast(msg, toastType, Toast.LENGTH_LONG);
    }

    protected void toast(String msg, ToastType toastType) {
        _toast(msg, toastType, Toast.LENGTH_SHORT);
    }

    private void _toast(String msg, ToastType toastType, int duration) {
        LayoutInflater inflater = getLayoutInflater();
        // Inflate the Layout
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_layout));
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (toastType.equals(ToastType.error)) {
                layout.setBackground(getDrawable(R.drawable.toast_error));
            } else if (toastType.equals(ToastType.info)) {
                layout.setBackground(getDrawable(R.drawable.toast_info));
            } else if (toastType.equals(ToastType.warning)) {
                layout.setBackground(getDrawable(R.drawable.toast_warning));
            } else {
                layout.setBackground(getDrawable(R.drawable.toast_info));
            }
        }
        TextView text = (TextView) layout.findViewById(R.id.textToShow);
        // Set the Text to show in TextView
        text.setText(msg);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    /**
     * Note that embedding your license key in code is unsafe and is done here for
     * demonstration purposes only.
     * Please visit https://seap.samsung.com/license-keys/about. for more details about license
     * keys.
     */
    private boolean activateKnoxLicense(String kpeLicenseKey) {
        final StringBuffer s = new StringBuffer();
        s.append("1 ");
        try {
            // Instantiate the EnterpriseLicenseManager class to use the activateLicense method
            KnoxEnterpriseLicenseManager klmManager = KnoxEnterpriseLicenseManager.getInstance(this.getApplicationContext());
            s.append("2 ");
            if (EnterpriseDeviceManager.getAPILevel() < 37) {
                s.append("3 ");
                klmManager.activateLicense(kpeLicenseKey, PACKAGE_NAME);
                s.append("4 ");
                knoxLicenseActivationSuccessCallback();
                s.append("5 ");
                toastLong(s.toString(), ToastType.error);
            } else {
                s.append("A ");
                klmManager.activateLicense(kpeLicenseKey, licenseResult -> {
                    s.append("B ");
                    if (licenseResult.isSuccess() && licenseResult.isActivation()) {
                        s.append("C ");
                        knoxLicenseActivationSuccessCallback();
                        s.append("D ");
                    } else {
                        s.append("E ");
                    }
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(getApplicationContext(),
                                s.toString(),
                                Toast.LENGTH_LONG).show();
                    });
                });
            }
            return true;
        } catch (Exception e) {
            showStacktraceInDialog(e);
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    private void knoxLicenseActivationSuccessCallback() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Profile.KNOX_LICENSE_ACTIVATED, true);
        // must set this to false because we may have skipped the license before
        editor.putBoolean(Profile.KNOX_LICENSE_SKIPPED, false);
        findViewById(R.id.knoxlicensebutton).setVisibility(View.GONE);
//        toast("System license has been activated successfully.", ToastType.info);
        editor.commit();
    }

    /**
     * Note that embedding your license key in code is unsafe and is done here for
     * demonstration purposes only.
     * Please visit https://seap.samsung.com/license-keys/about. for more details about license
     * keys.
     */
    private boolean deactivateKnoxLicense(String kpeLicenseKey) {
        try {
            // Instantiate the EnterpriseLicenseManager class to use the activateLicense method
            KnoxEnterpriseLicenseManager klmManager = KnoxEnterpriseLicenseManager.getInstance(this.getApplicationContext());
            // KPE License Activation TODO Add license key to Constants.java
            if (EnterpriseDeviceManager.getAPILevel() < 36) {
                klmManager.deActivateLicense(kpeLicenseKey, PACKAGE_NAME);
                knoxLicenseDeactivationSuccessCallback();
            } else {
                klmManager.deActivateLicense(kpeLicenseKey, PACKAGE_NAME, new LicenseResultCallback() {
                    @Override
                    public void onLicenseResult(LicenseResult licenseResult) {
                        if (licenseResult.isSuccess() && !licenseResult.isActivation()) {
                            knoxLicenseDeactivationSuccessCallback();
                        } else {
                            toastLong("System license couldn't be deactivated successfully.", ToastType.error);
                        }
                    }
                });
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    private void knoxLicenseDeactivationSuccessCallback() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(Profile.KNOX_LICENSE_ACTIVATED);
        editor.commit();
        findViewById(R.id.knoxlicensebutton).setVisibility(View.VISIBLE);
        toast("System license has been deactivated successfully.", ToastType.info);
        editor.commit();
    }

    private void clearDeviceOwner() {
        mDevicePolicyManager.clearDeviceOwnerApp(getPackageName());
    }

    private String currentSavedPasswordHash = ""; // we set this password in a few different places for different buttons.
    private final boolean isPasswordValid(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        try {
            String shashhex = CryptoHelper.getSHA256HashHex(s);
            if (currentSavedPasswordHash == null) {
                return false;
            } else {
                return currentSavedPasswordHash.equals(shashhex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void onLogo(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("PFS")
                .setMessage("\n\n" +
                        "PhoneForStudents.com\n\n" +
                        "Safe for Kids\n" +
                        "Easy for Parents\n\n")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true); // so that touching outside of dialog will dismiss the dialog.
        AlertDialog dialog = builder.show();

        // Must call show() prior to fetching text view
        TextView messageView = (TextView)(dialog.findViewById(android.R.id.message));
        messageView.setGravity(Gravity.CENTER);
    }

    private int invisibleButtonClickedCount = 0;
    private long invisibleButtonClickStartTime = 0;
    public void onInvisible(View view) {
        if (System.currentTimeMillis() - invisibleButtonClickStartTime < 10 * 1000 && invisibleButtonClickedCount >= 7) {
            invisibleButtonClickedCount = 0;
            invisibleButtonClickStartTime = 0;
            //show button for manual provision
            findViewById(R.id.provision).setVisibility(View.VISIBLE);
        } else if (invisibleButtonClickStartTime == 0) {
            invisibleButtonClickStartTime = System.currentTimeMillis();
            invisibleButtonClickedCount = 1;
        } else {
            if (System.currentTimeMillis() - invisibleButtonClickStartTime < 2 * 1000) {
                invisibleButtonClickedCount++;
                invisibleButtonClickStartTime = System.currentTimeMillis();
            } else {
                //reset
                invisibleButtonClickStartTime = System.currentTimeMillis();
                invisibleButtonClickedCount = 1;
            }
        }
    }

    private int invisibleButtonClickedCount2 = 0;
    private long invisibleButtonClickStartTime2 = 0;
    public void onInvisible2(View view) {
        if (System.currentTimeMillis() - invisibleButtonClickStartTime2 < 10 * 1000 && invisibleButtonClickedCount2 >= 7) {
            invisibleButtonClickedCount2 = 0;
            invisibleButtonClickStartTime2 = 0;
            //remove knox restrictions
            currentSavedPasswordHash = entityInfo.getPasswordHashForRemoveDefaultSystemAndUserRestrictions();
            findViewById(R.id.appsbutton).setVisibility(View.VISIBLE);
            showPasswordDialog(DialogAction.removeRestrictions);
        } else if (invisibleButtonClickStartTime2 == 0) {
            invisibleButtonClickStartTime2 = System.currentTimeMillis();
            invisibleButtonClickedCount2 = 1;
        } else {
            if (System.currentTimeMillis() - invisibleButtonClickStartTime2 < 2 * 1000) {
                invisibleButtonClickedCount2++;
                invisibleButtonClickStartTime2 = System.currentTimeMillis();
            } else {
                //reset
                invisibleButtonClickStartTime2 = System.currentTimeMillis();
                invisibleButtonClickedCount2 = 1;
            }
        }
    }

    private void initializeAndroidRestrictions() {
        setUserRestrictions(UserRestrictions.restrictons, UserRestrictions.noRestrictons);
    }

    private void removeAndroidRestrictions() {
        setUserRestrictions(null, UserRestrictions.noRestrictons);
        setUserRestrictions(null, UserRestrictions.restrictons);
    }

    private boolean initializeKnoxRestrictions() {
        boolean b = true;

        // these permissions will be disabled
        boolean value = false;
        // not sure about this restriction. Do we allow bluetooth data transfer?
        b = b & processKnoxRestriction(KnoxPrivilege.allowBluetoothDataTransferIndex, value);
        b = b & processKnoxRestriction(KnoxPrivilege.allowUsbHostStorageIndex, value);
        b = b & processKnoxRestriction(KnoxPrivilege.allowWallpaperChangeIndex, value);
        b = b & processKnoxRestriction(KnoxPrivilege.allowWiFiDirectIndex, value);
        b = b & processKnoxRestriction(KnoxPrivilege.setSdCardStateIndex, value);
        b = b & processKnoxRestriction(KnoxPrivilege.setUsbDebuggingEnabledIndex, value);
        b = b & processKnoxRestriction(KnoxPrivilege.setWifiTetheringIndex, value);

        // these permissions will be enabled
        value = true;

        // not sure about this. OTA updates are usually performed over Wi-Fi or a cellular network,
        // but can also be performed over other wireless protocols, or over the local area network.
        b = b & processKnoxRestriction(KnoxPrivilege.allowOTAUpgradeIndex, value);

        // cannot disable wifi here because we need to download other app packages during
        // the provisioning.
        b = b & processKnoxRestriction(KnoxPrivilege.allowWiFiIndex, value);
        // Cellular network cannot be restored once it is disabled
        // and Wallpapers cannot show images from Internet.
        b = b & processKnoxRestriction(KnoxPrivilege.setCellularDataIndex, value);

        // Remove these restrictions to prevent bricking the phone. We use separate QR codes to set
        // these restrictions.
        b = b & processKnoxRestriction(KnoxPrivilege.allowFactoryResetIndex, value);
        b = b & processKnoxRestriction(KnoxPrivilege.allowFirmwareRecoveryIndex, value);
        b = b & processKnoxRestriction(KnoxPrivilege.allowSafeModeIndex, value);

        // this one controls whether screen lock type is enabled or not. we want to enable this so that
        // PIN type can be selected manually, then lock this type using another QR code.
        b = b & processKnoxRestriction(KnoxPrivilege.setLockScreenStateIndex, value);

        return b;
    }

    private void removeKnoxRestrictions() {
        for (int i = 0; i < KnoxPrivilege.privileges.length; i++) {
            processKnoxRestriction(i, true);
        }
    }

    private void initializeRestrictions() {
        initializeAndroidRestrictions();
        initializeKnoxRestrictions();
    }

    private void removeRestrictions() {
        try {
            removeAndroidRestrictions();
            removeKnoxRestrictions();
        } finally {
        }
    }

    private void saveImagesToGallary() {
        try {
            AssetManager assets = getApplicationContext().getResources().getAssets();
            String[] names = assets.list("");
            if (names != null && names.length > 0) {
                for (String name : names) {
                    if (name.endsWith(".jpg") || name.endsWith(".png")) {
                        Bitmap bitmap = getBitmapFromAsset(getApplicationContext(), name);
                        if (bitmap != null) {
                            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "wallpaper-" + name, "wallpaper"); // this line (if not together with next line) sends images to Pictures folder. they are not shown up in Samsung Gallery
//                            addImageToGallery(name, bitmap, getApplicationContext()); // adding this line the images will show up in Samsung Gallery. ???
                        }
                    }
                }
                toast("Images have been installed.", ToastType.info);
            }
        } catch (Exception e) {
//            toastLong("Images have not been installed. " + e.getMessage(), ToastType.error);
            showStacktraceInDialog(e);
            Log.e("ERROR", "Failed to install images", e);
        }
    }

    private void addImageToGallery(final String name, Bitmap bitmap, final Context context) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        if (name.toLowerCase().endsWith(".jpg")) {
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        } else if (name.toLowerCase().endsWith(".png")) {
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        }
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, name);
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (BufferedOutputStream oStream = new BufferedOutputStream(getContentResolver().openOutputStream(uri))) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream);
        }
    }

    private static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();
        Bitmap bitmap = null;
        try (InputStream istr = assetManager.open(filePath)) {
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted && cameraAccepted) {
                        UpdateApp updateApp = new UpdateApp();
                        updateApp.setContext(FirstActivity.this);
                        updateApp.execute(appToInstallFromInternetURL);
                    }
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //https://www.wepstech.com/download-and-install-app-programmatically/
    public class UpdateApp extends AsyncTask<String, Integer, String> {
        String tempAPKFilename = "my_apk.apk";
        private QRCodeAndroid _qrCodeAndroid = null;

        private ProgressDialog mPDialog;
        private Context mContext;

        void setContext(Activity context) {
            mContext = context;
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPDialog = new ProgressDialog(mContext);
                    mPDialog.setMessage("Please wait ...");
                    mPDialog.setIndeterminate(true);
                    mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mPDialog.setCancelable(false);
                    mPDialog.show();
                }
            });
        }

        @Override
        protected String doInBackground(String... arg0) {
            String result = null;
            try {
                URL url = new URL(arg0[0]);
                int lastSlashIndex = url.getFile().lastIndexOf("/");
                if (lastSlashIndex >= 0) {
                    // if we download multiple apks simultaneously using the same name, we will get
                    // some error message in UI (something like "There was a problem parsing
                    // the package"), though it seems Android will re-download and install each app
                    // successfully at the end.
                    tempAPKFilename = url.getFile().substring(lastSlashIndex + 1);
                }
                if (url.getFile().endsWith("policy.pfs")
                        || url.getFile().endsWith("policy.pfs.2023")
                        || url.getFile().endsWith("policy.pfs.2025")) { // using pfs will give us the correct lenghtOfFile. xml and txt does not work - lenghtOfFile is -1 for them.
                    // the policy file
                    HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
                    httpUrlConnection.setConnectTimeout(10000);
                    httpUrlConnection.setReadTimeout(10000);
                    httpUrlConnection.setRequestMethod("GET");
                    //c.setDoOutput(true); // it won't work with c.setDoOutput(true)
                    httpUrlConnection.connect();
                    int responseCode = httpUrlConnection.getResponseCode();

                    if (responseCode != 200) {
                        return ("Response code: " + httpUrlConnection.getResponseCode());
                    }
                    int lenghtOfFile = httpUrlConnection.getContentLength();
                    InputStream is = httpUrlConnection.getInputStream();

                    byte[] buffer = new byte[lenghtOfFile];
                    int len1;
                    int total = 0;
                    while (total < lenghtOfFile && (len1 = is.read(buffer, total, lenghtOfFile - total)) != -1) {
                        total += len1;
                        publishProgress((int) ((total * 100) / lenghtOfFile));
                    }
                    is.close();
                    if (total == lenghtOfFile) {
                        QRCodeAndroid qrCodeAndroid = QRCodeAndroid.parse(new String(buffer, StandardCharsets.UTF_8));
                        if (qrCodeAndroid != null) {
                            _qrCodeAndroid = qrCodeAndroid;
                        } else {
                            result = "Policy file cannot be parsed properly!";
                        }
                    } else {
                        result = "File download is incomplete!";
                    }
                    if (mPDialog != null) {
                        mPDialog.dismiss();
                    }
                } else if (url.getFile().endsWith("public.key")) {
                    // the new public key
                    HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
                    httpUrlConnection.setConnectTimeout(10000);
                    httpUrlConnection.setReadTimeout(10000);
                    httpUrlConnection.setRequestMethod("GET");
                    //c.setDoOutput(true); // it won't work with c.setDoOutput(true)
                    httpUrlConnection.connect();
                    int responseCode = httpUrlConnection.getResponseCode();

                    if (responseCode != 200) {
                        return ("Response code: " + httpUrlConnection.getResponseCode());
                    }
                    int lenghtOfFile = httpUrlConnection.getContentLength();
                    InputStream is = httpUrlConnection.getInputStream();

                    byte[] buffer = new byte[lenghtOfFile];
                    int len1;
                    int total = 0;
                    while (total < lenghtOfFile && (len1 = is.read(buffer, total, lenghtOfFile - total)) != -1) {
                        total += len1;
                        publishProgress((int) ((total * 100) / lenghtOfFile));
                    }
                    is.close();
                    if (total == lenghtOfFile) {
                        PublicKey publicKey = CryptoHelper.getPublicKey(new String(buffer, StandardCharsets.UTF_8));
                        if (publicKey != null) {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString(Profile.PUBLIC_KEY, Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));
                            editor.commit();
                        } else {
                            result = "Public key cannot be generated!";
                        }
                    } else {
                        result = "File download is incomplete!";
                    }
                    if (mPDialog != null) {
                        mPDialog.dismiss();
                    }
                } else if (url.getFile().endsWith(".jpg") || url.getFile().endsWith(".png")) {
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    if (bitmap != null) {
                        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                        myWallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_SYSTEM);
                        myWallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK);
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(Profile.LOCK_SCRREN_IMAGE, url.toString());
                        editor.commit();
                    } else {
                        result = "Download failed: " + url.toString();
                    }
                } else if (url.getFile().endsWith(".apk")) {
                    HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
                    httpUrlConnection.setConnectTimeout(10000);
                    httpUrlConnection.setReadTimeout(10000);
                    httpUrlConnection.setRequestMethod("GET");
                    //c.setDoOutput(true); // it won't work with c.setDoOutput(true)
                    httpUrlConnection.connect();
                    int responseCode = httpUrlConnection.getResponseCode();

                    if (responseCode != 200) {
                        return ("Response code: " + httpUrlConnection.getResponseCode());
                    }
                    int lenghtOfFile = httpUrlConnection.getContentLength();

                    String PATH = Objects.requireNonNull(mContext.getExternalFilesDir(null)).getAbsolutePath();
                    File file = new File(PATH);
                    boolean isCreate = file.mkdirs();
                    File outputFile = new File(file, tempAPKFilename);
                    if (outputFile.exists()) {
                        boolean isDelete = outputFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(outputFile);

                    InputStream is = httpUrlConnection.getInputStream();

                    byte[] buffer = new byte[1024];
                    int len1;
                    long total = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        total += len1;
                        fos.write(buffer, 0, len1);
                        publishProgress((int) ((total * 100) / lenghtOfFile));
                    }
                    fos.close();
                    is.close();
                    if (mPDialog != null) {
                        mPDialog.dismiss();
                    }
                    result = installApkSilently();
                } else {
                    result = "Not supported file type: " + url.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
                result = e.getLocalizedMessage();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mPDialog != null)
                mPDialog.show();

        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mPDialog != null) {
                mPDialog.setIndeterminate(false);
                mPDialog.setMax(100);
                mPDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (mPDialog != null) {
                mPDialog.dismiss();
            }
            if (result == null) {
                toast("Download successful.", ToastType.info);
                if (this._qrCodeAndroid != null) {
                    deployPolicy(_qrCodeAndroid);
                }
            } else {
                toast(result, ToastType.error);
            }
        }

        private String installApkSilently() {
            String result = null;
            try {
                String PATH = Objects.requireNonNull(mContext.getExternalFilesDir(null)).getAbsolutePath();
                File file = new File(PATH + "/" + tempAPKFilename);
                InputStream inputStream = new FileInputStream(file);
                PackageInstallationUtils.installPackage(FirstActivity.this, inputStream, null);
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                result = ex.getMessage();
            }
            return result;
        }

        private String installApk() {
            String result = null;
            try {
                String PATH = Objects.requireNonNull(mContext.getExternalFilesDir(null)).getAbsolutePath();
                File file = new File(PATH + "/" + tempAPKFilename);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (SDK_INT >= 24) {
                    Uri downloaded_apk = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".fileprovider", file);
                    intent.setDataAndType(downloaded_apk, "application/vnd.android.package-archive");
                    List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        mContext.grantUriPermission(mContext.getApplicationContext().getPackageName() + ".fileprovider", downloaded_apk, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                } else {
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                result = e.getMessage();
            }
            return result;
        }
    }

    public void onWallpaper(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_PICTURE);
        } catch (Exception ex) {
            try {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            } catch (Exception ex2) {
//                toast("Failed to set wallpaper!\n\n" + ex.getMessage(), ToastType.error);
                showStacktraceInDialog(ex2);
            }
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_setHomeScreenWallpaper) {
            onWallpaper(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // serialize the app password so that it is still available after coming back from other activities
    private void saveAppPassword(String p) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(APP_PASSWORD, p);
        editor.commit();
    }

    private String getAppPassword() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        return pref.getString(APP_PASSWORD, null);
    }

    private void removeAppPassword() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.MY_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(APP_PASSWORD);
        editor.commit();
    }
}
