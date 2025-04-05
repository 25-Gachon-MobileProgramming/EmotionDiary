package kr.co.gachon.emotion_diary.ui.Remind;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.core.content.res.ResourcesCompat;

import kr.co.gachon.emotion_diary.R;

public class CircleGraphView extends View {

    private Paint paint;
    private float[] sweepAngles = new float[2];

    public CircleGraphView(Context context) {
        this(context, null);
    }

    public CircleGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(150f); //두께조절
        paint.setTextSize(56f);

        for (int i = 0; i < 2; i++) {
            animateSection(i, 0f, 0f);
        }
    }

    public void animateSection(final int section, float fromProgress, float toProgress) {
        if (section >= 0 && section <= 1) {
            ValueAnimator animator = ValueAnimator.ofFloat(fromProgress, toProgress);
            animator.setDuration(1000);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                sweepAngles[section] = 360 * (value / 100);
                invalidate();
            });
            animator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float radius = Math.min(getWidth(), getHeight()) / 2f;

        RectF rect = new RectF(
                getWidth() / 2f - radius + paint.getStrokeWidth() / 2f,
                getHeight() / 2f - radius + paint.getStrokeWidth() / 2f,
                getWidth() / 2f + radius - paint.getStrokeWidth() / 2f,
                getHeight() / 2f + radius - paint.getStrokeWidth() / 2f
        );

        float startAngle = -90f;

        int[] sectionColors = new int[] {
                R.color.purple_200,
                R.color.purple_500
        };

        int[] textColors = new int[] {
                R.color.white,
                R.color.white
        };

        for (int i = 0; i < 2; i++) {
            paint.setColor(getContext().getColor(sectionColors[i]));
            canvas.drawArc(rect, startAngle, sweepAngles[i], false, paint);

            int percentage = (int) (sweepAngles[i] / 360 * 100);
            if (percentage > 0) {
                float sectionCenterAngle = startAngle + sweepAngles[i] / 2f;

                float sectionCenterX = (float) (getWidth() / 2f +
                        (radius - paint.getStrokeWidth() / 2f) * Math.cos(Math.toRadians(sectionCenterAngle)));
                float sectionCenterY = (float) (getHeight() / 2f +
                        (radius - paint.getStrokeWidth() / 2f) * Math.sin(Math.toRadians(sectionCenterAngle)));

                Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setColor(getContext().getColor(textColors[i]));
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(40f);
                textPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.temp));

                String text = percentage + "%";
                float textOffset = (textPaint.descent() + textPaint.ascent()) / 2f;
                canvas.drawText(text, sectionCenterX, sectionCenterY - textOffset, textPaint);
            }

            startAngle += sweepAngles[i];
        }
    }
}
