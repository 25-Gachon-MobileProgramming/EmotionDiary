package kr.co.gachon.emotion_diary.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import kr.co.gachon.emotion_diary.MainActivity;
import kr.co.gachon.emotion_diary.R;


public class ConsecutiveWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, 0);
        }
    }

    public static String generateSquareFireGrid(int dayCount) {
        int N = (int) Math.floor(Math.sqrt(dayCount));

        StringBuilder builder = new StringBuilder();

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) builder.append("🔥");
            if(r != N-1) builder.append("\n");
        }

        return builder.toString();
    }

    public static float getFireTextSize(int dayCount) {
        int N = Math.max(1, (int) Math.floor(Math.sqrt(dayCount)));
        float baseSize = 96f;
        return baseSize / N;
    }


    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int consecutiveDaysCount) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_consecutive_days);

        views.setTextViewText(R.id.text_view_consecutive_days, String.valueOf(consecutiveDaysCount) + "일");
        views.setTextViewText(R.id.text_view_fire_background, generateSquareFireGrid(consecutiveDaysCount));
        views.setTextViewTextSize(R.id.text_view_fire_background, TypedValue.COMPLEX_UNIT_SP, getFireTextSize(consecutiveDaysCount));
        views.setViewVisibility(R.id.text_view_consecutive_days_message, View.VISIBLE);

        if (consecutiveDaysCount == 0) {
            views.setTextViewText(R.id.text_view_fire_background, "😒");
            views.setTextViewText(R.id.text_view_consecutive_days, "일기를 써볼까요?");
            views.setTextViewTextSize(R.id.text_view_consecutive_days, TypedValue.COMPLEX_UNIT_SP, 24);
            views.setViewVisibility(R.id.text_view_consecutive_days_message, View.GONE);
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
        views.setOnClickPendingIntent(R.id.text_view_consecutive_days, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllWidgets(Context context, int consecutiveDaysCount) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, ConsecutiveWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, consecutiveDaysCount);
        }
    }
}