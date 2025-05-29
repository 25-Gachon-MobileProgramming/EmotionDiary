package kr.co.gachon.emotion_diary.ui.Remind.remindSummary;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Date;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.ui.Remind.WriteRate.RateFragment;
import kr.co.gachon.emotion_diary.ui.Remind.emotionStatistics.EmotionStatisticsFragment;
import kr.co.gachon.emotion_diary.ui.Remind.timeGraph.TimeZoneFragment;

public class RemindSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remind_summary);

        boolean isMonthly = getIntent().getBooleanExtra("isMonthly", true);
        Date startDate = (Date) getIntent().getSerializableExtra("startDate");
        Date endDate = (Date) getIntent().getSerializableExtra("endDate");

        Bundle args = new Bundle();
        args.putBoolean("isMonthly", isMonthly);
        args.putSerializable("startDate", startDate);
        args.putSerializable("endDate", endDate);

        // RateFragment
        RateFragment rateFragment = new RateFragment();
        rateFragment.setArguments(args);

        // EmotionStatisticsFragment
        EmotionStatisticsFragment emotionFragment = new EmotionStatisticsFragment();
        emotionFragment.setArguments(args);

        // TimeZoneFragment
        TimeZoneFragment timeFragment = new TimeZoneFragment();
        timeFragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.summaryFragmentCircle, rateFragment)
                .replace(R.id.summaryFragmentEmotion,  emotionFragment)
                .replace(R.id.summaryFragmentTime, timeFragment)
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.custom_back_bar);

            Toolbar parent = (Toolbar) actionBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0, 0);

            ImageButton backButton = actionBar.getCustomView().findViewById(R.id.backButtonActionBar);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            // 액션 바 제목 바꾸기
            TextView titleTextView = actionBar.getCustomView().findViewById(R.id.titleTextViewActionBar);
            if (titleTextView != null) {
                titleTextView.setText("Remind Summary");
            }
        }
    }
}
