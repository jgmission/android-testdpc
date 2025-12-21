package org.feitian.dpc.util;

import android.content.SharedPreferences;

import com.pfs.dpc.entity.EntityInfo;
import com.pfs.dpc.qrcode.Profile;

public class Store {
    private final static String DELIMITER = "\t";

    public static EntityInfo loadEntityInfo(SharedPreferences sharedPref) {
        String s = sharedPref.getString(Profile.ENTITY_INFO, null);
        if (s != null) {
            EntityInfo entityInfo = EntityInfo.parse(s);
            return entityInfo;
        } else {
            return null;
        }
    }

    public static void saveEntityInfo(SharedPreferences sharedPref, EntityInfo entityInfo) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Profile.ENTITY_INFO, entityInfo.toString());
        editor.commit();
    }
}
