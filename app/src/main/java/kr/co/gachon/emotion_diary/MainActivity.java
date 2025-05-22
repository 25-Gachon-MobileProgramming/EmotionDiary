package kr.co.gachon.emotion_diary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.time.LocalDate;
import java.time.LocalDateTime; // LocalDateTime 추가
import java.time.LocalTime;    // LocalTime 추가
import java.time.ZoneId;
import java.util.Locale;
import java.util.Date;

import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.DiaryRepository;
import kr.co.gachon.emotion_diary.data.Emotions;
import kr.co.gachon.emotion_diary.databinding.ActivityMainBinding;
import kr.co.gachon.emotion_diary.ui.home.HomeFragment;
import kr.co.gachon.emotion_diary.ui.taro.TaroActivity;
import kr.co.gachon.emotion_diary.ui.writePage.DiaryWriteActivity;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnDateSelectedListener {

    /**
     * @noinspection FieldCanBeLocal
     */
    private ActivityMainBinding binding;

    /**
     * @noinspection FieldCanBeLocal
     */
    private DiaryRepository diaryRepository;

    private LocalDate currentSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_calendar, R.id.navigation_timeLine, R.id.navigation_myPage)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        diaryRepository = new DiaryRepository(getApplication());

        currentSelectedDate = LocalDate.now();

        FloatingActionButton diaryWriteButton = findViewById(R.id.diary_write_button);

        diaryWriteButton.setOnClickListener(v -> {
            long startOfDayMillis = currentSelectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endOfDayMillis = currentSelectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;

            diaryRepository.getDiaryForDateRange(new Date(startOfDayMillis), new Date(endOfDayMillis))
                    .observe(this, diary -> {
                        Intent intent;
                        if (diary != null) {
                            Log.d("MainActivity", "일기 데이터 조회 성공. 제목: " + diary.getTitle() + ", 타로 내용: " + diary.getTaroName());
                            if (diary.getTaroName() == null) {

                                intent = new Intent(getApplicationContext(), TaroActivity.class);


                                LocalDateTime dateTimeWithCurrentTime = currentSelectedDate.atTime(LocalTime.now());

                                Date dateToSend = Date.from(dateTimeWithCurrentTime.atZone(ZoneId.systemDefault()).toInstant());

                                intent.putExtra("date", dateToSend.toString());

                                intent.putExtra("title", diary.getTitle());
                                intent.putExtra("content", diary.getContent());
                                intent.putExtra("emotion", Emotions.getEmotionDataById(diary.getEmotionId()).getText());
                            } else {

                                intent = new Intent(getApplicationContext(), DiaryWriteActivity.class);
                                intent.putExtra("selectedDate", startOfDayMillis);
                            }
                        } else {

                            intent = new Intent(getApplicationContext(), DiaryWriteActivity.class);
                            intent.putExtra("selectedDate", startOfDayMillis);
                        }
                        startActivity(intent);
                    });
        });

        diaryRepository.insertDummyData();

        diaryRepository.getAllDiaries().observe(this, diaries -> {
            Log.d("RoomExample", "모든 일기 (Repository - ExecutorService):");

            for (Diary diary : diaries) {
                Log.d("RoomExample", "ID: " + diary.getId() + ", 제목: " + diary.getTitle() + ", 내용: " + diary.getContent() + ", 날짜: " + diary.getDate()  + ", 감정: " + diary.getEmotionText() + ", 타로제목: " + diary.getTaroName());
            }
        });
        // --------- DB TEST END ----------
    }

    @Override
    public void onDateSelected(LocalDate selectedDate) {
        this.currentSelectedDate = selectedDate;
    }
}