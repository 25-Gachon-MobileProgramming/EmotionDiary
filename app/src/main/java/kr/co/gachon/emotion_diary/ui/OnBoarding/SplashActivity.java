package kr.co.gachon.emotion_diary.ui.OnBoarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.gachon.emotion_diary.MainActivity;
import kr.co.gachon.emotion_diary.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView logoVideoView = findViewById(R.id.logoVideoView);
        ImageView videoPlaceholder = findViewById(R.id.videoPlaceholder);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.logovideo);
        logoVideoView.setVideoURI(videoUri);

        // 영상 준비되면 썸네일 제거 + 영상 재생
        logoVideoView.setOnPreparedListener(mp -> {
            videoPlaceholder.setVisibility(ImageView.GONE);
            logoVideoView.start();
        });

        // 영상 끝나면 다음 화면으로
        logoVideoView.setOnCompletionListener(mp -> {
            SharedPreferences prefs = getSharedPreferences("avatar_pref", MODE_PRIVATE);
            boolean isAvatarCompleted = prefs.getBoolean("isAvatarCompleted", false);

            Intent intent = isAvatarCompleted ?
                    new Intent(SplashActivity.this, MainActivity.class) :
                    new Intent(SplashActivity.this, OnBoardingActivity.class);

            startActivity(intent);
            finish();
        });
    }
}
