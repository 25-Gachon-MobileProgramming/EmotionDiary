package kr.co.gachon.emotion_diary;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Calendar;

import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.DiaryRepository;
import kr.co.gachon.emotion_diary.databinding.ActivityMainBinding;
import kr.co.gachon.emotion_diary.notification.AlarmScheduler;
import kr.co.gachon.emotion_diary.ui.writePage.DiaryWriteActivity;
import kr.co.gachon.emotion_diary.utils.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {

    /**
     * @noinspection FieldCanBeLocal
     */
    private ActivityMainBinding binding;

    // --------- Assign FOR DB TEST START---------
    /**
     * @noinspection FieldCanBeLocal
     */
    private DiaryRepository diaryRepository;
    // --------- Assign FOR DB TEST END-----------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Notification
        SharedPreferences prefs = getSharedPreferences("diary_prefs", MODE_PRIVATE);
        boolean alarmSet = prefs.getBoolean("alarm_set", false);

        if (!alarmSet) {
            // 최초 실행 시 기본값 23:00 저장 및 알림 예약
            SharedPreferencesUtils.saveTime(this, 23, 0);
            AlarmScheduler.scheduleDiaryReminder(this, 23, 0);

            prefs.edit().putBoolean("alarm_set", true).apply();
        } else {
            // 기존 저장된 시간으로 알림 예약
            int hour = SharedPreferencesUtils.getHour(this);
            int minute = SharedPreferencesUtils.getMinute(this);
            AlarmScheduler.scheduleDiaryReminder(this, hour, minute);
        }

        // BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_calendar, R.id.navigation_timeLine, R.id.navigation_myPage)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        FloatingActionButton diaryWriteButton = findViewById(R.id.diary_write_button);

        diaryWriteButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), DiaryWriteActivity.class);
            intent.putExtra("selectedDate", System.currentTimeMillis());
            startActivity(intent);
        });

        // --------- DB TEST START ---------
        diaryRepository = new DiaryRepository(getApplication());

        diaryRepository.insertDummyData();

        diaryRepository.getAllDiaries().observe(this, diaries -> {
            Log.d("RoomExample", "모든 일기 (Repository - ExecutorService):");

            for (Diary diary : diaries) {
                Log.d("RoomExample", "ID: " + diary.getId() + ", 제목: " + diary.getTitle() + ", 내용: " + diary.getContent() + ", 날짜: " + diary.getDate()  + ", 감정: " + diary.getEmotionText());
            }
        });

//         Diary newDiary = new Diary("title", "content",  Calendar.getInstance().getTime(), 1);
//         diaryRepository.insert(newDiary);


        // --------- DB TEST END ----------

    }
}
