package kr.co.gachon.emotion_diary.ui.taro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import kr.co.gachon.emotion_diary.BuildConfig;
import kr.co.gachon.emotion_diary.MainActivity;
import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.Diary; // Diary import 추가
import kr.co.gachon.emotion_diary.data.DiaryRepository; // DiaryRepository import 추가
import kr.co.gachon.emotion_diary.data.Emotions;
import kr.co.gachon.emotion_diary.data.Gpt.GptApiService;
import kr.co.gachon.emotion_diary.data.Gpt.GptRequest;
import kr.co.gachon.emotion_diary.data.Gpt.GptResponse;
import kr.co.gachon.emotion_diary.ui.answerPage.AnswerActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaroActivity extends AppCompatActivity {

    private String content;
    private String selectedEmotion;
    private String currentDate;
    private String title;
    private ImageButton cardTopLeft;
    private ImageButton cardTopRight;
    private ImageButton cardBottomLeft;
    private ImageButton cardBottomRight;
    private Button nextButton;
    private int selectedCardIndex = -1;
    private String taroName = "";
    private String gender;
    private String birth;


    private DiaryRepository diaryRepository;


    private View.OnClickListener cardClickListener;


    private List<String> cardTitles;

    @Override
    protected void onResume() {
        super.onResume();


        selectedCardIndex = -1;
        taroName = "";
        nextButton.setEnabled(false);


        setCardSelectionEnabled(true);
        cardTopLeft.setAlpha(1f);
        cardTopRight.setAlpha(1f);
        cardBottomLeft.setAlpha(1f);
        cardBottomRight.setAlpha(1f);

        cardTopLeft.setImageResource(R.drawable.card_back);
        cardTopRight.setImageResource(R.drawable.card_back);
        cardBottomLeft.setImageResource(R.drawable.card_back);
        cardBottomRight.setImageResource(R.drawable.card_back);
    }

    private void flipCard(final ImageButton card) {
        card.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // 카드 이름에 해당하는 이미지 drawable에서 가져오기
                        int imageResId = getResources().getIdentifier(
                                taroName, // taroName 사용
                                "drawable",
                                getPackageName()
                        );

                        if (imageResId != 0) {
                            card.setImageResource(imageResId);
                        } else {
                            card.setImageResource(R.drawable.card_back);
                            Toast.makeText(TaroActivity.this, "이미지 리소스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }

                        card.animate()
                                .rotationY(0f)
                                .setDuration(700)
                                .start();
                    }
                })
                .start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_taro);

        diaryRepository = new DiaryRepository(getApplication());

        Intent intent = getIntent();
        content = intent.getStringExtra("content");
        selectedEmotion = intent.getStringExtra("emotion");
        currentDate = intent.getStringExtra("date");
        title = intent.getStringExtra("title");


        checkWifiStatus();

        SharedPreferences prefs = getSharedPreferences("avatar_pref", MODE_PRIVATE);
        gender = prefs.getString("gender", "알 수 없음");
        birth = prefs.getString("birthDate", "알 수 없음");
        Log.wtf("genderTest", gender);
        Log.wtf("birthTest", birth);


        cardTopLeft = findViewById(R.id.card_top_left);
        cardTopRight = findViewById(R.id.card_top_right);
        cardBottomLeft = findViewById(R.id.card_bottom_left);
        cardBottomRight = findViewById(R.id.card_bottom_right);
        nextButton = findViewById(R.id.next_button);

        // cardTitles 리스트 초기화는 여기서 합니다.
        cardTitles = new ArrayList<>(Arrays.asList(
                "fool", "magician", "highpriestess", "empress", "hierophant", "lovers", "chariot", "strength", "hermit",
                "wheeloffortune", "justice", "hangedman", "death", "temperance", "devil", "tower", "star", "moon",
                "sun", "judgment", "world", "top"
        ));


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
                titleTextView.setText("Taro Card");
            }
        }

        cardClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedCardIndex == -1) { // 이미 카드를 선택하지 않았을 때만 작동
                    // 선택되지 않은 카드들의 투명도 조절
                    if (view != cardTopLeft) cardTopLeft.setAlpha(0.3f);
                    if (view != cardTopRight) cardTopRight.setAlpha(0.3f);
                    if (view != cardBottomLeft) cardBottomLeft.setAlpha(0.3f);
                    if (view != cardBottomRight) cardBottomRight.setAlpha(0.3f);

                    view.setAlpha(1f); // 선택된 카드는 불투명하게

                    Random random = new Random();
                    // cardTitles 리스트가 제대로 초기화되었는지 확인
                    if (cardTitles != null && !cardTitles.isEmpty()) {
                        selectedCardIndex = random.nextInt(cardTitles.size());
                        taroName = cardTitles.get(selectedCardIndex); // taroName에 카드 이름 저장
                        // 다음 버튼 활성화
                        nextButton.setEnabled(true);
                    } else {
                        Toast.makeText(TaroActivity.this, "타로 카드 목록이 비어있습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        cardTopLeft.setOnClickListener(cardClickListener);
        cardTopRight.setOnClickListener(cardClickListener);
        cardBottomLeft.setOnClickListener(cardClickListener);
        cardBottomRight.setOnClickListener(cardClickListener);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCardIndex != -1) {
                    setCardSelectionEnabled(false);

                    ImageButton selectedCard = null;

                    if (cardTopLeft.getAlpha() == 1f) selectedCard = cardTopLeft;
                    else if (cardTopRight.getAlpha() == 1f) selectedCard = cardTopRight;
                    else if (cardBottomLeft.getAlpha() == 1f) selectedCard = cardBottomLeft;
                    else if (cardBottomRight.getAlpha() == 1f) selectedCard = cardBottomRight;

                    if (selectedCard != null) {
                        // 선택된 카드 회전 애니메이션 실행
                        flipCard(selectedCard);
                    }

                    // GPT 프롬프트 생성
                    String prompt = "타로 카드 제목에 해당하는 내용과 내가 일기장에 쓴 내용, 나이,성별을 종합하여 사람이 해주는 느낌으로 세 문장 정도 위로 글을 적어줘. " +
                            "나는 " + gender + "이고 생년월일은 " + birth + "이야." +
                            "\n내용: " + content + "\n타로 카드 제목: " + taroName; // taroName 사용

                    List<GptRequest.Message> messages = new ArrayList<>();
                    messages.add(new GptRequest.Message("user", prompt));

                    GptRequest request = new GptRequest("gpt-3.5-turbo", messages, 0.7);

                    // Retrofit 설정
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://api.openai.com/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    GptApiService apiService = retrofit.create(GptApiService.class);

                    String apiKey = "Bearer " + BuildConfig.API_KEY;

                    // GPT API 호출
                    Call<GptResponse> call = apiService.getGptMessage(apiKey, request);

                    call.enqueue(new Callback<GptResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<GptResponse> call, @NonNull Response<GptResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String gptResult = response.body().choices.get(0).message.content;

                                // 다음 액티비티(AnswerActivity)로 데이터 전달 및 이동
                                Intent sendintent = new Intent(TaroActivity.this, AnswerActivity.class);
                                sendintent.putExtra("gptReply", gptResult);
                                sendintent.putExtra("date", currentDate);
                                sendintent.putExtra("title", title);
                                sendintent.putExtra("content", content);
                                sendintent.putExtra("emotion", selectedEmotion);
                                sendintent.putExtra("taroCardName", taroName); // taroName 전달
                                startActivity(sendintent);
                            } else {
                                Log.e("TaroActivity", "GPT 응답 실패: " + response.code() + " " + response.message());
                                Toast.makeText(TaroActivity.this, "GPT 응답 실패. 일기가 저장됩니다.", Toast.LENGTH_SHORT).show();
                                saveDiaryToRoom(null);
                                setCardSelectionEnabled(true); // 카드 선택 다시 활성화
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<GptResponse> call, @NonNull Throwable t) {

                            Log.e("TaroActivity", "API 호출 실패: " + t.getMessage(), t);
                            Toast.makeText(TaroActivity.this, "API 호출 실패. 일기가 저장됩니다.", Toast.LENGTH_SHORT).show();
                            saveDiaryToRoom(null); // 타로 카드 정보 없이 저장
                            setCardSelectionEnabled(true); // 카드 선택 다시 활성화
                        }
                    });
                } else {
                    Toast.makeText(TaroActivity.this, "카드를 선택하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 다음 버튼 비활성화
        nextButton.setEnabled(false);
    }

    // 카드 활성화/비활성화 메서드
    private void setCardSelectionEnabled(boolean enabled) {
        cardTopLeft.setClickable(enabled);
        cardTopRight.setClickable(enabled);
        cardBottomLeft.setClickable(enabled);
        cardBottomRight.setClickable(enabled);
    }

    // Wi-Fi 상태 확인 메서드
    private void checkWifiStatus() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiInfo != null && wifiInfo.isConnected()) {
            Toast.makeText(this, "Wi-Fi에 연결되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wi-Fi 연결이 필요합니다. 일기가 저장됩니다.", Toast.LENGTH_LONG).show();
            // Wi-Fi 연결이 없을 경우, Room에 데이터 저장 후 MainActivity로 돌아감
            saveDiaryToRoom(null); // 타로 카드 정보 없이 저장 (null 전달)
            Intent mainIntent = new Intent(TaroActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        }
    }

    private void saveDiaryToRoom(String taroNameForDb) {

        Date diaryDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("\"EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
            diaryDate = sdf.parse(currentDate);
        } catch (java.text.ParseException e) {
            Log.e("TaroActivity", "날짜 파싱 오류: " + e.getMessage());
            // 파싱 오류 발생 시 현재 시간을 사용
            diaryDate = Calendar.getInstance().getTime();
        }

        int emotionId = 0;
        if (selectedEmotion != null && !selectedEmotion.isEmpty()) {

            emotionId = Emotions.getEmotionIdByText(selectedEmotion);
        }

        Diary diary = new Diary(
                title != null ? title : "",
                content != null ? content : "",
                diaryDate,
                emotionId,
                taroNameForDb
        );

        // Room DB에 삽입
        diaryRepository.insert(diary);
        Log.d("TaroActivity", "일기가 Room에 저장되었습니다. (타로 카드 정보: " + taroNameForDb + ")");
    }
}