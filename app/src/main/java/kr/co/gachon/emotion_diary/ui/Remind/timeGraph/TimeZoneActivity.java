package kr.co.gachon.emotion_diary.ui.Remind.timeGraph;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import java.util.Date;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.ui.Remind.remindSummary.RemindSummaryActivity;

public class TimeZoneActivity extends AppCompatActivity {
    private TextView timeTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_zone);

        timeTextView = findViewById(R.id.time);

        boolean isMonthly = getIntent().getBooleanExtra("isMonthly", true);
        Date startDate = (Date) getIntent().getSerializableExtra("startDate");
        Date endDate = (Date) getIntent().getSerializableExtra("endDate");

        TimeZoneFragment fragment = new TimeZoneFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMonthly", isMonthly);
        args.putSerializable("startDate", startDate);
        args.putSerializable("endDate", endDate);
        fragment.setArguments(args);

        // Fragment를 container에 삽입
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.timegraph_fragment_container, fragment);
        transaction.commit();

        Button button = findViewById(R.id.nextButton);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(TimeZoneActivity.this, RemindSummaryActivity.class);
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
                titleTextView.setText("Time Zone");
            }
        }
    }

    public void setTimeText(String time) {
        timeTextView.setText(time);
    }
}
