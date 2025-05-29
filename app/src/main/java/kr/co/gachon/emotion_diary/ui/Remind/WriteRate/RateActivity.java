package kr.co.gachon.emotion_diary.ui.Remind.WriteRate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import kr.co.gachon.emotion_diary.R;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import kr.co.gachon.emotion_diary.data.AppDatabase;
import kr.co.gachon.emotion_diary.data.DiaryDao;
import kr.co.gachon.emotion_diary.databinding.RemindBinding;
import kr.co.gachon.emotion_diary.ui.Remind.emotionStatistics.EmotionStatisticsActivity;
import kr.co.gachon.emotion_diary.ui.Remind.timeGraph.TimeZoneFragment;


public class RateActivity extends AppCompatActivity implements RateFragment.RateTextListener {


    private TextView rateText;
    Date startDate;
    Date endDate;
    String term = "2024-5";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remind);

        rateText = findViewById(R.id.rateText);


        boolean isMonthly = getIntent().getBooleanExtra("isMonthly", true);
        term = getIntent().getStringExtra("term");

            Pair<Date, Date> range = null;
            try {
                range = getDateRangeFromTerm(term, isMonthly);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            startDate = range.first;
            endDate = range.second;



        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(view -> {
            Intent intent = new Intent(RateActivity.this, EmotionStatisticsActivity.class);
            intent.putExtra("isMonthly", isMonthly);
            intent.putExtra("startDate", startDate);
            intent.putExtra("endDate", endDate);
            startActivity(intent);
        });


        RateFragment fragment = new RateFragment();
        Bundle args = new Bundle();
        args.putBoolean("isMonthly", isMonthly);
        args.putSerializable("startDate", startDate);
        args.putSerializable("endDate", endDate);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.circleFragmentContainer, fragment)
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
                titleTextView.setText("Rate");
            }
        }
    }

    @Override
    public void onRateTextUpdated(String text) {
        rateText.setText(text);
    }
    public static Pair<Date, Date> getDateRangeFromTerm(String term, Boolean isMonthly) throws ParseException {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        if (isMonthly) {
            // term이 "2024-5" 또는 "2024-05" 형태
            String[] parts = term.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Calendar는 0-based

            // 시작일: YYYY-MM-01 00:00:00
            calStart.set(year, month, 1, 0, 0, 0);
            calStart.set(Calendar.MILLISECOND, 0);

            // 종료일: 해당 월의 마지막 날 23:59:59
            calEnd.set(year, month, 1);
            int lastDay = calEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
            calEnd.set(Calendar.DAY_OF_MONTH, lastDay);
            calEnd.set(Calendar.HOUR_OF_DAY, 23);
            calEnd.set(Calendar.MINUTE, 59);
            calEnd.set(Calendar.SECOND, 59);
            calEnd.set(Calendar.MILLISECOND, 999);
        } else if (isMonthly == false) {
            // term이 "2024" 형태
            calStart = Calendar.getInstance();
            calEnd = Calendar.getInstance();

            // 연도만 추출: "2024-5" → 2024, "2024" → 2024
            String yearStr = term.split("-")[0];
            int year = Integer.parseInt(yearStr);

            // 시작일: YYYY-01-01 00:00:00
            calStart.set(year, Calendar.JANUARY, 1, 0, 0, 0);
            calStart.set(Calendar.MILLISECOND, 0);

            // 종료일: YYYY-12-31 23:59:59
            calEnd.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
            calEnd.set(Calendar.MILLISECOND, 999);
        } else {
            throw new IllegalArgumentException("Invalid term format: " + term);
        }

        return new Pair<>(calStart.getTime(), calEnd.getTime());
    }
}