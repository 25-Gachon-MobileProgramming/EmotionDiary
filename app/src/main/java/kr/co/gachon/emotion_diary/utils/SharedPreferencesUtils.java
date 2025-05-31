package kr.co.gachon.emotion_diary.utils;

import android.content.Context;

public class SharedPreferencesUtils {
    private static final String PREF_NAME = "diary_prefs";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";

    public static void saveTime(Context context, int hour, int minute) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .apply();
    }

    public static int getHour(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_HOUR, 23); // 기본값 23시
    }

    public static int getMinute(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_MINUTE, 0);
    }
}