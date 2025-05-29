package kr.co.gachon.emotion_diary.ui.taro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import kr.co.gachon.emotion_diary.MainActivity;
import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.AppDatabase;
import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.DiaryDao;
import kr.co.gachon.emotion_diary.data.DiaryRepository;
import kr.co.gachon.emotion_diary.data.Gpt.GptGetDiaryResponse;
import kr.co.gachon.emotion_diary.helper.Helper;
import kr.co.gachon.emotion_diary.ui.answerPage.AnswerActivity;

public class TaroActivity extends AppCompatActivity {
    private String selectedCardTitle = null;
    private AlertDialog loadingDialog;
    private DiaryDao diaryDao;
    private DiaryRepository diaryRepository;

    List<String> cardTitles = new ArrayList<>(Arrays.asList(
            "chariot", "death", "devil", "emperor", "empress",
            "fool", "hangedman", "hermit", "hierophant", "highpriestess",
            "judgement", "justice", "lovers", "magician", "moon", "star",
            "strength", "sun", "temperance", "tower", "wheeloffortune", "world"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_taro);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.custom_back_bar);

            Toolbar parent = (Toolbar) actionBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0, 0);

            ImageButton backButton = actionBar.getCustomView().findViewById(R.id.backButtonActionBar);
            backButton.setOnClickListener(v -> showExitConfirmationDialog());

            TextView titleTextView = actionBar.getCustomView().findViewById(R.id.titleTextViewActionBar);
            if (titleTextView != null) titleTextView.setText("Taro Card");
        }

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        diaryDao = db.diaryDao();
        diaryRepository = new DiaryRepository(getApplication());

        Helper helper = new Helper(this);

        this.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });

        if (!helper.checkNetworkState()) {
            Toast.makeText(this, "네트워크가 없습니다.", Toast.LENGTH_SHORT).show();
            finishToMainActivity();
        }

        Intent intent = getIntent();
        long dateMillis = intent.getLongExtra("date", -1);
        String content = intent.getStringExtra("content");
        String selectedEmotion = intent.getStringExtra("emotion");
        String title = intent.getStringExtra("title");

        if (dateMillis == -1) {
            Toast.makeText(this, "Invalid date selected. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Date currentDate = new Date(dateMillis);

        ImageButton cardTopLeft = findViewById(R.id.card_top_left);
        ImageButton cardTopRight = findViewById(R.id.card_top_right);
        ImageButton cardBottomLeft = findViewById(R.id.card_bottom_left);
        ImageButton cardBottomRight = findViewById(R.id.card_bottom_right);
        Button nextButton = findViewById(R.id.next_button);

        View.OnClickListener cardClickListener = view -> {
            if (view != cardTopLeft) cardTopLeft.setAlpha(0.3f);
            if (view != cardTopRight) cardTopRight.setAlpha(0.3f);
            if (view != cardBottomLeft) cardBottomLeft.setAlpha(0.3f);
            if (view != cardBottomRight) cardBottomRight.setAlpha(0.3f);

            view.setAlpha(1f);

            Random random = new Random();
            int selectedCardIndex = random.nextInt(cardTitles.size());
            selectedCardTitle = cardTitles.get(selectedCardIndex);

            nextButton.setEnabled(true);
        };

        cardTopLeft.setOnClickListener(cardClickListener);
        cardTopRight.setOnClickListener(cardClickListener);
        cardBottomLeft.setOnClickListener(cardClickListener);
        cardBottomRight.setOnClickListener(cardClickListener);

        initLoadingDialog();

        new Thread(() -> {
            Date startOfToday = Helper.getStartOfDay(currentDate);
            Date startOfTomorrow = Helper.getStartOfNextDay(currentDate);

            List<Diary> diariesOnce = diaryDao.getDiariesForSpecificDayOnce(startOfToday, startOfTomorrow);

            if (diariesOnce != null && !diariesOnce.isEmpty()) {
                Diary diary = diariesOnce.get(0);

                if(diary.getTaroName() != null && diary.getGptAnswer() != null) {
                    Intent sendintent = new Intent(TaroActivity.this, AnswerActivity.class);
                    sendintent.putExtra("gptReply", diary.getGptAnswer());
                    sendintent.putExtra("taroCard", diary.getTaroName());
                    startActivity(sendintent);
                    finish();
                }
            }
        }).start();

        nextButton.setOnClickListener(v -> {
            if (selectedCardTitle == null) {
                Toast.makeText(TaroActivity.this, "카드를 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            v.setEnabled(false);

            ImageButton selectedCard = null;

            if (cardTopLeft.getAlpha() == 1f) selectedCard = cardTopLeft;
            else if (cardTopRight.getAlpha() == 1f) selectedCard = cardTopRight;
            else if (cardBottomLeft.getAlpha() == 1f) selectedCard = cardBottomLeft;
            else if (cardBottomRight.getAlpha() == 1f) selectedCard = cardBottomRight;

            if (selectedCard != null) flipCard(selectedCard);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (loadingDialog != null && loadingDialog.getWindow() != null && !loadingDialog.isShowing()) {
                    loadingDialog.show();
                }

                // TODO: 유저 정보 추가
                GptGetDiaryResponse.getGptReply(title, content, selectedEmotion, selectedCardTitle, new GptGetDiaryResponse.GptResponseListener() {
                    @Override
                    public void onGptResponseSuccess(String gptResult) {
                        if (loadingDialog != null && loadingDialog.isShowing())
                            loadingDialog.dismiss();

                        new Thread(() -> {
                            Date startOfToday = Helper.getStartOfDay(currentDate);
                            Date startOfTomorrow = Helper.getStartOfNextDay(currentDate);
                            List<Diary> diariesOnce = diaryDao.getDiariesForSpecificDayOnce(startOfToday, startOfTomorrow);

                            if (diariesOnce != null && !diariesOnce.isEmpty()) {
                                Diary diary = diariesOnce.get(0);
                                diary.setTaroName(selectedCardTitle);
                                diary.setGptAnswer(gptResult);
                                diaryRepository.update(diary);
                            }
                        }).start();

                        Intent sendintent = new Intent(TaroActivity.this, AnswerActivity.class);
                        sendintent.putExtra("gptReply", gptResult);
                        sendintent.putExtra("taroCard", selectedCardTitle);
                        startActivity(sendintent);
                    }

                    @Override
                    public void onGptResponseFailure(String errorMessage) {
                        if (loadingDialog != null && loadingDialog.isShowing())
                            loadingDialog.dismiss();

                        Toast.makeText(TaroActivity.this, "에러가 발생했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        finishToMainActivity();
                    }
                });
            }, 1000);
        });

        nextButton.setEnabled(true);
    }

    private void finishToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void flipCard(final ImageButton card) {
        card.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(() -> {
                    Log.d("TaroActivity", "selectedCardTitle: " + selectedCardTitle);

                    int imageResId = getResources().getIdentifier(
                            "taro_" + selectedCardTitle,
                            "drawable",
                            getPackageName()
                    );

                    if (imageResId != 0) {
                        card.setImageResource(imageResId);
                    } else {
                        card.setImageResource(R.drawable.card_back);
                        Toast.makeText(TaroActivity.this, "이미지 리소스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    card.animate().rotationY(0f).setDuration(700).start();
                })
                .start();
    }

    private void initLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        pbParams.rightMargin = 40;
        progressBar.setLayoutParams(pbParams);
        layout.addView(progressBar);

        TextView messageTextView = new TextView(this);
        messageTextView.setText("답변을 생성 중입니다...");
        messageTextView.setTextSize(16);

        messageTextView.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
        layout.addView(messageTextView);

        builder.setView(layout);
        builder.setCancelable(false);
        loadingDialog = builder.create();
    }

    private void showExitConfirmationDialog() {
        AlertDialog dialogBuilder = new AlertDialog.Builder(this)
                .setTitle("종료 확인")
                .setMessage("정말 타로카드 선택을 종료하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> finishToMainActivity())
                .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                .show();

        dialogBuilder.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.colorSecondary));
        dialogBuilder.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.colorSecondary));
    }
}