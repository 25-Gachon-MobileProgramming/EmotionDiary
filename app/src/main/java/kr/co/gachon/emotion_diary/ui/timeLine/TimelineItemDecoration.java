package kr.co.gachon.emotion_diary.ui.timeLine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import kr.co.gachon.emotion_diary.R;

public class TimelineItemDecoration extends RecyclerView.ItemDecoration {

    private final int lineWidth = 4;
    private final int dotRadius = 12;
    private final int offsetLeft = 80;

    private final Paint linePaint;
    private final Paint dotPaint;

    public TimelineItemDecoration(Context context) {
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, R.color.colorSecondary));
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setAntiAlias(true);

        dotPaint = new Paint();
        dotPaint.setColor(ContextCompat.getColor(context, R.color.colorSecondary));
        dotPaint.setAntiAlias(true);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = offsetLeft;
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent,
                           @NonNull RecyclerView.State state) {

        int childCount = parent.getChildCount();
        int centerX = offsetLeft / 2;

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            int top = child.getTop();
            int bottom = child.getBottom();
            int centerY = top + (bottom - top) / 2;
            int gap = 24;

            if (i == 0) {
                canvas.drawLine(centerX, centerY, centerX, bottom + gap, linePaint);
            } else if (i != childCount - 1) {
                canvas.drawLine(centerX, top - gap, centerX, bottom + gap, linePaint);
            } else {
                canvas.drawLine(centerX, top - gap, centerX, centerY, linePaint);
            }

            canvas.drawCircle(centerX, centerY, dotRadius, dotPaint);
        }
    }
}
