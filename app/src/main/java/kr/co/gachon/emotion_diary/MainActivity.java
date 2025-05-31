package kr.co.gachon.emotion_diary;


import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1010;
    // --------- Assign FOR DB TEST START---------
    /**
     * @noinspection FieldCanBeLocal
     */
    private DiaryRepository diaryRepository;
    // --------- Assign FOR DB TEST END-----------

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 알림 권한 요청
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (!alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                1010);

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
                Log.d("RoomExample", "ID: " + diary.getId() + ", 제목: " + diary.getTitle() + ", 내용: " + diary.getContent() + ", 날짜: " + diary.getDate() + ", 감정: " + diary.getEmotionText());
            }
        });

//         Diary newDiary = new Diary("title", "content",  Calendar.getInstance().getTime(), 1);
//         diaryRepository.insert(newDiary);


        // --------- DB TEST END ----------

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification
                SharedPreferences prefs = getSharedPreferences("diary_prefs", MODE_PRIVATE);
                boolean alarmSet = prefs.getBoolean("alarm_set", false);

                if(!alarmSet) {
                    SharedPreferencesUtils.saveTime(this, 19, 9);
                    AlarmScheduler.scheduleDiaryReminder(this, 19, 9);

                    prefs.edit().putBoolean("alarm_set", true).apply();
                } else {
                    int hour = SharedPreferencesUtils.getHour(this);
                    int minute = SharedPreferencesUtils.getMinute(this);
                    AlarmScheduler.scheduleDiaryReminder(this, hour, minute);
                }
            }
        }
    }
}