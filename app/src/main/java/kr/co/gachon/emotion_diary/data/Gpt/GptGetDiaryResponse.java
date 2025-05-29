package kr.co.gachon.emotion_diary.data.Gpt;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.co.gachon.emotion_diary.BuildConfig;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GptGetDiaryResponse {
    public interface GptResponseListener {
        void onGptResponseSuccess(String gptResult);

        void onGptResponseFailure(String errorMessage);
    }

    public static void getGptReply(String title, String content, String selectedEmotion, String selectedCardTitle, GptResponseListener listener) {
        String promptTemplate = "내가 쓴 일기 제목은 '%s'이고, 내용은 다음과 같아: \"%s\"\n\n" +
                "저는 지금 '%s' 감정을 느끼고 있어. " +
                "그리고 제가 뽑은 타로 카드는 **'%s'**야. \n\n" +
                "이 타로 카드('%s')의 상징적인 의미와 당신의 현재 감정, 그리고 일기 내용을 깊이 연관 지어줘. " +
                "마치 타로 리더가 당신의 상황을 읽어주듯이, 카드의 통찰을 바탕으로 지금 저에게 필요한 조언과 따뜻한 공감의 메시지를 구체적으로 건네줘. " +
                "말투는 딱딱하지 않게 마치 진짜 친구가 말해주는 것 처럼 '합니다', '해보세요' 이런 어투보다 '할거야', '해보는게 어때?' 처럼 친근한 어체를 사용해줘" +
                "타로 카드가 던지는 시사점과 함께 위로를 제공하는 데 집중해줘." +
                "분량은 5문장씩 3문단 정도로 각 문단 끝나면 엔터 넣어줘 휴대폰 화면에 TextView로 띄울거거든.";


        String prompt = String.format(promptTemplate, title, content, selectedEmotion, selectedCardTitle, selectedCardTitle);

        List<GptRequest.Message> messages = new ArrayList<>();
        messages.add(new GptRequest.Message("user", prompt));

        GptRequest request = new GptRequest("gpt-3.5-turbo", messages, 0.7);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GptApiService apiService = retrofit.create(GptApiService.class);
        String apiKey = "Bearer " + BuildConfig.API_KEY;
        Call<GptResponse> call = apiService.getGptMessage(apiKey, request);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GptResponse> call, @NonNull Response<GptResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String gptResult = response.body().choices.get(0).message.content;
                    listener.onGptResponseSuccess(gptResult);
                } else {
                    ResponseBody errorBody = response.errorBody();
                    if (errorBody != null) try (errorBody) {
                        listener.onGptResponseFailure(errorBody.string());
                    } catch (IOException e) {
                        listener.onGptResponseFailure(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GptResponse> call, @NonNull Throwable t) {
                listener.onGptResponseFailure(t.getMessage());
            }
        });
    }
}
