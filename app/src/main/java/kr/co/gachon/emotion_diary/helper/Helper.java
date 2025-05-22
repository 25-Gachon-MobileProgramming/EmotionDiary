package kr.co.gachon.emotion_diary.helper;

import android.content.Context;
import android.util.DisplayMetrics;

public class Helper {
    private final Context context;

    public Helper(Context context) {
        this.context = context;
    }

    public int dpToPx(int dp) {
        if (context == null) throw new IllegalStateException("Context isn't initialized.");

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;

        return Math.round(dp * density);
    }
}