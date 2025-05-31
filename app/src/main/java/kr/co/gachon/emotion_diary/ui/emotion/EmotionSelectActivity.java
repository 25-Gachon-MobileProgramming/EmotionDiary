package kr.co.gachon.emotion_diary.ui.emotion;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Date;
import java.util.List;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.AppDatabase;
import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.DiaryDao;
import kr.co.gachon.emotion_diary.data.DiaryRepository;
import kr.co.gachon.emotion_diary.data.Emotions;
import kr.co.gachon.emotion_diary.helper.Helper;
import kr.co.gachon.emotion_diary.ui.taro.TaroActivity;

public class EmotionSelectActivity extends AppCompatActivity {

    String selectedEmotion = null;

    private Button previousButton = null;
    private DiaryDao diaryDao;
    private DiaryRepository diaryRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_emotion);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.custom_back_bar);

            Toolbar parent = (Toolbar) actionBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0, 0);

            ImageButton backButton = actionBar.getCustomView().findViewById(R.id.backButtonActionBar);
            backButton.setOnClickListener(v -> finish());

            TextView titleTextView = actionBar.getCustomView().findViewById(R.id.titleTextViewActionBar);
            if (titleTextView != null) titleTextView.setText("Emotion");
        }

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        diaryDao = db.diaryDao();
        diaryRepository = new DiaryRepository(getApplication());

        Intent intent = getIntent();
        long dateMillis = intent.getLongExtra("date", -1);
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        if (dateMillis == -1) {
            Toast.makeText(this, "Invalid date selected. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Date selectedDate = new Date(dateMillis);

        makeEmotionButtons();

        new Thread(() -> {
            Date startOfToday = Helper.getStartOfDay(selectedDate);
            Date startOfTomorrow = Helper.getStartOfNextDay(selectedDate);

            List<Diary> diariesOnce = diaryDao.getDiariesForSpecificDayOnce(startOfToday, startOfTomorrow);

            if (diariesOnce != null && !diariesOnce.isEmpty()) {
                Diary diary = diariesOnce.get(0);
                String emotionFromDiary = diary.getEmotionText();

                runOnUiThread(() -> {
                    GridLayout emotionGrid = findViewById(R.id.emotionGrid);

                    for (int i = 0; i < emotionGrid.getChildCount(); i++) {
                        MaterialButton button = (MaterialButton) emotionGrid.getChildAt(i);
                        String buttonEmotionText = button.getContentDescription().toString();

                        if (buttonEmotionText.equals(emotionFromDiary)) {
                            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorSecondary));
                            button.setTextColor(ContextCompat.getColor(this, R.color.colorOnSecondary));

                            selectedEmotion = emotionFromDiary;
                            previousButton = button;
                            break;
                        }
                    }
                });
            }
        }).start();

        Button nextPage = findViewById(R.id.nextPageButton);
        nextPage.setOnClickListener(v -> {
            v.setClickable(false);

            if (selectedEmotion == null) {
                Toast.makeText(EmotionSelectActivity.this, "감정을 선택하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            Date currentDate = new Date(dateMillis);
            diaryRepository.insert(new Diary(title, content, currentDate, Emotions.getEmotionIdByText(selectedEmotion), null, null));

            Intent intent1 = new Intent(EmotionSelectActivity.this, TaroActivity.class);
            intent1.putExtra("date", currentDate.getTime());
            intent1.putExtra("title", title);
            intent1.putExtra("content", content);
            intent1.putExtra("emotion", selectedEmotion);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
            v.setClickable(true);
        });
    }

    private void makeEmotionButtons() {
        GridLayout emotionGrid = findViewById(R.id.emotionGrid);
        List<Emotions.EmotionData> emotionList = Emotions.getAllEmotionDataList();

        for (Emotions.EmotionData emotion : emotionList) {
            String text = emotion.getText();
            String emoji = emotion.getEmoji();

            MaterialButton emojiButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);

            emojiButton.setText(String.format("%s\n%s", emoji, text));
            emojiButton.setContentDescription(text);
            emojiButton.setTextSize(18);
            emojiButton.setAllCaps(false);
            emojiButton.setGravity(Gravity.CENTER);
            emojiButton.setPadding(16, 16, 16, 16);

            // 텍스트 색상 및 배경 설정
            emojiButton.setTextColor(ContextCompat.getColor(this, R.color.colorOnSurface));
            emojiButton.setStrokeColorResource(R.color.colorPrimary);
            emojiButton.setStrokeWidth(2);
            emojiButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorSurface));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            emojiButton.setLayoutParams(params);

            emojiButton.setOnClickListener(v -> {
                if (previousButton != null && previousButton != emojiButton) {
                    previousButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorSurface));
                    previousButton.setTextColor(ContextCompat.getColor(this, R.color.colorOnSurface));
                }

                emojiButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorSecondary));
                emojiButton.setTextColor(ContextCompat.getColor(this, R.color.colorOnSecondary));

                selectedEmotion = text;
                previousButton = emojiButton;
            });

            emotionGrid.addView(emojiButton);
        }
    }

}


