package kr.co.gachon.emotion_diary.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "diaries")
public class Diary {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "emotion_id")
    private int emotionId;

    public Diary(String title, String content, Date date, int emotionId) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.emotionId = emotionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getEmotionId() {
        return emotionId;
    }

    public void setEmotionId(int emotionId) {
        if (emotionId < 0 || emotionId >= Emotions.getAllEmotionDataList().size())
            throw new IllegalArgumentException("유효하지 않은 감정 ID입니다: " + emotionId);

        this.emotionId = emotionId;
    }

    @Ignore
    public String getEmotionEmoji() {
        Emotions.EmotionData emotionData = Emotions.getEmotionDataById(emotionId);

        return (emotionData != null) ? emotionData.emoji : "❓";
    }

    @Ignore
    public String getEmotionText() {
        Emotions.EmotionData data = Emotions.getEmotionDataById(emotionId);

        return (data != null) ? data.getText() : "알 수 없는 감정";
    }
}