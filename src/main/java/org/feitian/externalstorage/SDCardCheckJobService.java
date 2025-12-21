package org.feitian.externalstorage;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class SDCardCheckJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        // onStartJob() is called by the system when it is time for your job to execute. If your task is short and simple, feel free to implement the logic directly
        // in onStartJob() and return false when you are finished, to let the system know that all work has been completed. But if you need to do a more complicated
        // task, like connecting to the network, you’ll want to kick off a background thread and return true, letting the system know that you have a thread still
        // running and it should hold on to your wakelock for a while longer.
        //
        // Note: Your JobService will run on the main thread. That means that you need to manage any asynchronous tasks yourself (like using a Thread or AsyncTask
        // to open a network connection, and then returning true) within your onStartJob() method.
        //
        // jobFinished() is not a method you override, and the system won’t call it. That’s because you need to be the one to call this method once your service or
        // thread has finished working on the job. If your onStartJob() method kicked off another thread and then returned true, you’ll need to call this method
        // from that thread when the work is complete. This is how to system knows that it can safely release your wakelock. If you forget to call jobFinished(),
        // your app is going to look pretty guilty in the battery stats lineup.
        //
        // jobFinished() requires two parameters: the current job, so that it knows which wakelock can be released, and a boolean indicating whether you’d like to
        // reschedule the job. If you pass in true, this will kick off the JobScheduler’s exponential backoff logic for you.
        SDCardCheckTask sdCardCheckTask = new SDCardCheckTask() {
            @Override
            protected void onPostExecute(Boolean success) {
                jobFinished(params, !success);
            }
        };
        sdCardCheckTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // onStopJob() is called by the system if the job is cancelled before being finished. This generally
        // happens when your job conditions are no longer being met, such as when the device has been
        // unplugged or if WiFi is no longer available. So use this method for any safety checks and clean
        // up you may need to do in response to a half-finished job. Then, return true if you’d like the
        // system to reschedule the job, or false if it doesn’t matter and the system will drop this job.
        // (For the most part, you shouldn’t encounter this situation too often, but if you’re having a lot
        // of trouble, consider trying to shorten your job. For example, if your download never finishes,
        // have your server split the download into smaller packets that can be retrieved quicker.)
        return true;
    }
}
