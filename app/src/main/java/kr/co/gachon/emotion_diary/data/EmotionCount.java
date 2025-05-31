package kr.co.gachon.emotion_diary.data;

import androidx.room.Ignore;

public class EmotionCount {
    public int emotion_id; // 감정 ID
    public int count; // 해당 감정의 출현 횟수
    @Ignore
    public String emotion; // 감정명

    public EmotionCount(int emotion_id, int count) {
        this.emotion_id = emotion_id;
        this.count = count;
        this.emotion = Emotions.getEmotionDataById(emotion_id).getText(); // 감정명 설정
    }
}

