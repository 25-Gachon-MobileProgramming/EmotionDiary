package kr.co.gachon.emotion_diary.ui.Remind.emotionStatistics;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Date;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.ui.Remind.timeGraph.TimeZoneActivity;

public class EmotionStatisticsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emotion_statistics);
        boolean isMonthly = getIntent().getBooleanExtra("isMonthly", true);
        Date startDate = (Date) getIntent().getSerializableExtra("startDate");
        Date endDate = (Date) getIntent().getSerializableExtra("endDate");



        EmotionStatisticsFragment fragment = new EmotionStatisticsFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMonthly", isMonthly);
        args.putSerializable("startDate", startDate);
        args.putSerializable("endDate", endDate);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.emotion_statistics_fragment_container, fragment)
                .commit();

        // 버튼 클릭 시 이동
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, TimeZoneActivity.class);
            intent.putExtra("isMonthly", isMonthly);
            intent.putExtra("startDate", startDate);
            intent.putExtra("endDate", endDate);
            startActivity(intent);
        });
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
                titleTextView.setText("Emotion Statistics");
            }
        }
    }
}