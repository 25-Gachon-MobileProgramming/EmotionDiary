package kr.co.gachon.emotion_diary.ui.emotion;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.Emotions;
import kr.co.gachon.emotion_diary.ui.taro.TaroActivity;

public class EmotionSelectActivity extends AppCompatActivity {

    String selectedEmotion = null;

    private Button previousButton = null;
    private Integer originalTint = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_emotion);

        Intent intent = getIntent();

        String CurrentDate = intent.getStringExtra("date");
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        Log.wtf("getTest", CurrentDate);
        Log.wtf("getTest", title);
        Log.wtf("getTest", content);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.custom_back_bar);

            ImageButton backButton = actionBar.getCustomView().findViewById(R.id.backButtonActionBar);
            backButton.setOnClickListener(v -> finish());

            // 액션 바 제목 바꾸기
            TextView titleTextView = actionBar.getCustomView().findViewById(R.id.titleTextViewActionBar);
            if (titleTextView != null) {
                titleTextView.setText("Emotion");
            }

        GridLayout emotionGrid = findViewById(R.id.emotionGrid);

            // emotions 에 있는 감정 목록을 가져 와서 버튼 생성
        List<Emotions.EmotionData> emotionList = Emotions.getAllEmotionDataList();
        for (Emotions.EmotionData emotion : emotionList) {
            String text = emotion.getText();
            String emoji = emotion.getEmoji();

            Button emojiButton = new Button(this);

            // 😀\n행복 이런 식으로 정보를 가져 오기
            emojiButton.setText(emoji + "\n" + text);
            emojiButton.setTextSize(20);
            emojiButton.setPadding(16, 16, 16, 16);
            emojiButton.setAllCaps(false);
            emojiButton.setBackgroundColor(Color.TRANSPARENT);
            emojiButton.setTextColor(Color.WHITE);
            emojiButton.setGravity(Gravity.CENTER);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            emojiButton.setLayoutParams(params);

            emojiButton.setOnClickListener(v -> {
                // 이전 선택된 버튼 초기화, 배경은 투명하게 놨둠
                if (previousButton != null && previousButton != emojiButton) {
                    previousButton.setBackgroundColor(Color.TRANSPARENT);
                    ((Button) previousButton).setTextColor(Color.WHITE);

                }

                // 눌린 버튼 색깔
                emojiButton.setBackgroundColor(ContextCompat.getColor(EmotionSelectActivity.this, R.color.green));

                selectedEmotion = text;
                previousButton = emojiButton;
            });

            emotionGrid.addView(emojiButton);
        }

            originalTint = getColor(R.color.white);


            Button nextPage = findViewById(R.id.nextPageButton);
            nextPage.setOnClickListener(view -> {
                if (previousButton == null) {
                    Toast.makeText(EmotionSelectActivity.this, "감정 선택 하세요", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent1 = new Intent(EmotionSelectActivity.this, TaroActivity.class);
                    intent1.putExtra("date", CurrentDate);
                    intent1.putExtra("title", title);
                    intent1.putExtra("content", content);
                    intent1.putExtra("emotion", selectedEmotion);

                    startActivity(intent1);
                }
            });
        }
    }
}


