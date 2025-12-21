package org.feitian.externalstorage;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.List;

public class SDCardCheckTask extends AsyncTask<Void, Integer, Boolean> {
    public final static String TAG = "SDCARD";

    @Override
    protected Boolean doInBackground(Void... voids) {
        checkSDCard();
        return Boolean.TRUE;
    }

    public void checkSDCard() {
        Log.e(TAG, "Checking sd card ...");
        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
        for (StorageUtils.StorageInfo storageInfo : storageInfoList) {
            if (storageInfo.removable) {
                String file = "/storage" + storageInfo.path.substring(storageInfo.path.lastIndexOf("/"));
                checkDir(new File(file));
            }
        }
    }
    private void checkDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    Log.i("DIR", file.getAbsolutePath());
                    checkDir(file);
                } else {
                    Log.i("FILE", file.getAbsolutePath());
                }
            }
        } else {
            Log.i("DIR", dir.getAbsolutePath() + " is empty!");
        }
    }
}
