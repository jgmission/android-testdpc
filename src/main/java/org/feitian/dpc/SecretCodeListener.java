package org.feitian.dpc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SecretCodeListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String pwd = intent.getData().getHost();
        if ("android.provider.Telephony.SECRET_CODE".equals(intent.getAction())) {
            Intent i = new Intent(context, FirstActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}