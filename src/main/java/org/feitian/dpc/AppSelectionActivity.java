package org.feitian.dpc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afwsamples.testdpc.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends Activity {
    private static class AppInfo {
        String packageName;
        String appName;
        String displayName;
        boolean checked;
        boolean systemApp;
    }

    private ListView appListView;
    private AppAdapter appAdapter = null;
    // static keeps all the elements in the list even device orientation changes and this instance is reinitialized
    private static final List<AppInfo> appList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        appListView = (ListView) findViewById(R.id.listviewapplist);
        appAdapter = new AppAdapter();
        setupAppList();
        if (appListView != null) {
            // Assign adapter to ListView
            appListView.setAdapter(appAdapter);
            this.registerForContextMenu(appListView);
        }

        setupAppList();
    }

    class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return appList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View view, ViewGroup arg2) {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.listview_item_app_selection, arg2, false);
            }
            AppInfo appInfo = appList.get(arg0);
            try {
                final PackageManager pm = getPackageManager();
                //app name
                if (appInfo.systemApp) {
//                ((TextView) view.findViewById(R.id.appname)).setText(appInfo.packageName);
                    ((TextView) view.findViewById(R.id.appname)).setText( appInfo.packageName + " (system)");
                } else {
//                ((TextView) view.findViewById(R.id.appname)).setText(appInfo.packageName);
                    ((TextView) view.findViewById(R.id.appname)).setText(appInfo.packageName + " (" + appInfo.displayName + ")");
                }

                //app icon
                Drawable ico = pm.getApplicationInfo(appInfo.packageName, PackageManager.GET_META_DATA).loadIcon(getPackageManager());
                ImageView imageView = (ImageView)view.findViewById(R.id.appimage);
                imageView.setImageDrawable(ico);

                //check option
                ((CheckBox)view.findViewById(R.id.checkboxappselect)).setChecked(appInfo.checked);
                ((CheckBox)view.findViewById(R.id.checkboxappselect)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appInfo.checked = ((CheckBox)v).isChecked();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return view;
        }
    }

    private void setupAppList() {
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        appList.clear();
        List<ApplicationInfo> systemApps = new LinkedList<>();
        Set<String> systemAppNames = new HashSet<>();
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.enabled) {
                if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                    // if there is launch intent, it is not a system app
                    String packageName = packageInfo.packageName;
                    if (!packageName.contains("tipas")) {
                        try {
                            final String applicationName = (String)pm.getApplicationLabel(packageInfo);
                            AppInfo appInfo = new AppInfo();
                            appInfo.appName = applicationName;
                            appInfo.packageName = packageName;
                            appInfo.displayName = applicationName;
                            appInfo.systemApp = false;
                            appList.add(appInfo);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    Log.i("system app ---> ", packageInfo.packageName);
                    String shortName = getSystemAppShortName(packageInfo.packageName);
                    if (!systemAppNames.contains(shortName)) {
                        systemAppNames.add(shortName);
                        systemApps.add(packageInfo);
                    }
                }
            }
        }

        //system apps
        if (systemApps.size() > 0) {
            for (ApplicationInfo packageInfo : systemApps) {
                try {
                    String packageName = packageInfo.packageName;
                    final String applicationName = (String) pm.getApplicationLabel(packageInfo);
                    AppInfo appInfo = new AppInfo();
                    String shortName = getSystemAppShortName(packageName);
                    appInfo.appName = applicationName;
                    appInfo.packageName = packageName;
                    appInfo.displayName = shortName;
                    appInfo.systemApp = true;
                    appList.add(appInfo);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        Collections.sort(appList, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                if (o1.systemApp && !o2.systemApp) {
                    return 1;
                } else if (!o1.systemApp && o2.systemApp) {
                    return -1;
                } else {
                    return o1.displayName.toLowerCase().compareTo(o2.displayName.toLowerCase());
                }
            }
        });
        appAdapter.notifyDataSetChanged();
    }

    public static String getSystemAppShortName(String packageName) {
        String[] tokens = packageName.split("\\.");
        String shortName = tokens[0];
        if (tokens.length > 1) {
            shortName += "." + tokens[1];
        }
        return shortName;
    }
}