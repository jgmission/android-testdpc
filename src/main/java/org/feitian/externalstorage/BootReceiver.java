package org.feitian.externalstorage;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(context, SDCardCheckJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(101, componentName);
        builder.setOverrideDeadline(1000L);
//        builder.setPeriodic(10000L, 1000L);
        JobInfo jobInfo = builder.build();
        int resultCode = jobScheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.i(SDCardCheckTask.TAG, "Job scheduled after boot completed!");
        } else {
            Log.e(SDCardCheckTask.TAG, "Job not scheduled after boot completed");
        }

    }
}