package kr.co.gachon.emotion_diary.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.DisplayMetrics;

import java.util.Calendar;
import java.util.Date;

public class Helper {
    private final Context context;

    public Helper(Context context) {
        if (context == null) throw new IllegalStateException("Context isn't initialized.");

        this.context = context;
    }

    public static Date getDateFromString(String currentDate) {
        if (currentDate == null || !currentDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Invalid date format. Expected format: YYYY-MM-DD");
        }

        try {
            String[] parts = currentDate.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, day);

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTime();
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid date format. Unable to parse date.", e);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;

        return Math.round(dp * density);
    }

    public boolean checkNetworkState() {
        ConnectivityManager connectivityManager = context.getSystemService(ConnectivityManager.class);
        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities actNetwork = connectivityManager.getNetworkCapabilities(network);
        if (actNetwork == null) return false;

        boolean isWifi = actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        boolean isCellular = actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);

        return isWifi || isCellular;
    }

    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getStartOfNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 다음 날로 이동
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}