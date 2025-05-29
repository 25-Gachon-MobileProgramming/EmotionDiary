package kr.co.gachon.emotion_diary.ui.OnBoarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.gachon.emotion_diary.MainActivity;
import kr.co.gachon.emotion_diary.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView titleTextView;
    private Animation alphaAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoImageView = findViewById(R.id.logoImageView);
        titleTextView = findViewById(R.id.titleTextView);

        // 애니메이션 로드
        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha);

        // 3초 후 애니메이션 시작 및 화면 전환
        new Handler().postDelayed(() -> {
            // 애니메이션 시작
            logoImageView.startAnimation(alphaAnimation);
            titleTextView.startAnimation(alphaAnimation);

            // 애니메이션 리스너 설정
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // 애니메이션 시작 시 동작
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // 애니메이션 종료 시 View를 숨김 처리 (이걸 안하면 마지막에 한번 더 등장)
                    logoImageView.setVisibility(ImageView.INVISIBLE);
                    titleTextView.setVisibility(TextView.INVISIBLE);

                    // SharedPreferences로 값 확인 (Avatar가 중복 설정되지 않게)
                    SharedPreferences prefs = getSharedPreferences("avatar_pref", MODE_PRIVATE);
                    boolean isAvatarCompleted = prefs.getBoolean("isAvatarCompleted", false);

                    Intent intent;
                    if (isAvatarCompleted) {
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, OnBoardingActivity.class);
                    }
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // 반복 애니메이션 처리
                }
            });
        }, 2000); // 2000 milliseconds = 2 seconds
    }
}

