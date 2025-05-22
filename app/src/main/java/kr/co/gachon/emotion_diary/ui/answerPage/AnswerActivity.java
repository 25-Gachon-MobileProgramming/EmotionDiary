package kr.co.gachon.emotion_diary.ui.answerPage;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import kr.co.gachon.emotion_diary.MainActivity;
import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.DiaryRepository;
import kr.co.gachon.emotion_diary.data.Emotions;

public class AnswerActivity extends AppCompatActivity {

    private DiaryRepository diaryRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_answer);

        // diaryRepository에 있는 insert를 참조하기 위해 사용
        diaryRepository = new DiaryRepository(getApplication());

        Intent intent = getIntent();

        String CurrentDate = intent.getStringExtra("date");
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String emotion = intent.getStringExtra("emotion");
        String taroCard = intent.getStringExtra("taroCard");

        Log.wtf("get2Test", CurrentDate);
        Log.wtf("get2Test", title);
        Log.wtf("get2Test", content);
        Log.wtf("get2Test", emotion);
        Log.wtf("get2Test", taroCard);

        // 바 왼쪽에 imageButton 사용해서 뒤로가기
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.custom_back_bar);

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
                titleTextView.setText("Answer");
            }

        }

        ImageView taroImage = findViewById(R.id.taro);
        String CardImage = getIntent().getStringExtra("taroCard");

        int imageResId = getResources().getIdentifier(CardImage, "drawable", getPackageName());

        if (imageResId != 0) {
            taroImage.setImageResource(imageResId);
        } else {
            taroImage.setImageResource(R.drawable.card_back);
        }



        // GPT 응답 받기
        String gptReply = intent.getStringExtra("gptReply");

        Log.wtf("Testanwer", gptReply);

        TextView textView = findViewById(R.id.answer);
        textView.setText(gptReply);

        // date형태로 넣어줘야 하기 때문에 string을 date로 변환
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date parsedDate = null;
        try {
            parsedDate = formatter.parse(CurrentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date finalParsedDate = parsedDate;

        findViewById(R.id.room_button).setOnClickListener(v -> {
            if (finalParsedDate != null) {
                Diary diary = new Diary(title, content, finalParsedDate, Emotions.getEmotionIdByText(emotion));
                diaryRepository.insert(diary);
                Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show();

                // 페이지를 MainActivity로 바로 가기, 이전 페이지들을 모두 종료 후 이동
                Intent firstPage = new Intent(AnswerActivity.this, MainActivity.class);
                firstPage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(firstPage);
                finish();
            } else {
                Toast.makeText(this, "날짜 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // Toast로 wifi 연결 상태 보여줌 - 작동은 맨 아래 코드
        if (wifiConnected(AnswerActivity.this)) {
            Toast.makeText(AnswerActivity.this, "Wi-Fi가 연결되었습니다", Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(AnswerActivity.this, "Wi-Fi가 연결 않되었습니다", Toast.LENGTH_SHORT).show();
        }
    }


    // wifi 유무 판단 코드
    public static boolean wifiConnected(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return false;
            }
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return false;
    }
}
