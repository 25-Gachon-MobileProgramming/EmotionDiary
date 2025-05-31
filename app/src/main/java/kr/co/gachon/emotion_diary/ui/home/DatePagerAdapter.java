package kr.co.gachon.emotion_diary.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.Emotions;
import kr.co.gachon.emotion_diary.ui.taro.TaroActivity;
import kr.co.gachon.emotion_diary.ui.writePage.DiaryWriteActivity;

public class DatePagerAdapter extends RecyclerView.Adapter<DatePagerAdapter.DateViewHolder> {

    private final List<LocalDate> dateList;
    private final Map<LocalDate, Diary> diaryMap; // 날짜별 일기 정보
    private final Context context;

    public DatePagerAdapter(Context context, List<LocalDate> dateList, Map<LocalDate, Diary> diaryMap) {
        this.dateList = dateList;
        this.diaryMap = diaryMap;  // 외부에서 주입
        this.context = context;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date_card, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        LocalDate date = dateList.get(position);
        holder.bind(date);
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, dayTextView, emotionHintText;
        ImageView tarotImage;
        MaterialCardView cardView;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            emotionHintText = itemView.findViewById(R.id.emotionHintText);
            tarotImage = itemView.findViewById(R.id.tarotImage);
            cardView = itemView.findViewById(R.id.taro_daily);
        }

        void bind(LocalDate date) {
            // 날짜 텍스트 출력
            dateTextView.setText(String.format(Locale.getDefault(), "%d.%d %s", date.getYear(), date.getMonthValue(), date.getDayOfWeek()));
            dayTextView.setText(String.format(Locale.getDefault(), "%02d", date.getDayOfMonth()));

            if (diaryMap.containsKey(date)) {
                Diary diary = diaryMap.get(date);
                if (Objects.requireNonNull(diary).getTaroName() != null) {
                    int imageResId = context.getResources().getIdentifier(
                            "taro_" + Objects.requireNonNull(diaryMap.get(date)).getTaroName(),
                            "drawable",
                            context.getPackageName()
                    );

                    if (imageResId != 0) {
                        tarotImage.setImageResource(imageResId);
                        tarotImage.setVisibility(View.VISIBLE);

                        dateTextView.setTextColor(Color.WHITE);
                        emotionHintText.setTextColor(Color.WHITE);
                    }

                    emotionHintText.setText("일기가 기록되어 있어요 😄");
                } else {
                    emotionHintText.setText("결과를 확인해볼까요? 🧐");
                }

                cardView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, TaroActivity.class);
                    intent.putExtra("date", diary.getDate().getTime());
                    intent.putExtra("title", diary.getTitle());
                    intent.putExtra("content", diary.getContent());
                    intent.putExtra("emotion", Emotions.getEmotionDataById(diary.getEmotionId()).getText());
                    ContextCompat.startActivity(context, intent, null);
                });
            } else {
                tarotImage.setVisibility(View.GONE);
                emotionHintText.setText("오늘의 일기를 써보세요 ✍️");

                cardView.setOnClickListener(v -> {
                    Date today = new Date();
                    Intent intent = new Intent(context, DiaryWriteActivity.class);
                    intent.putExtra("selectedDate", today.getTime());
                    ContextCompat.startActivity(context, intent, null);
                });
            }
        }
    }
}

