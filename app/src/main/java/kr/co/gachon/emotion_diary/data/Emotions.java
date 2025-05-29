package kr.co.gachon.emotion_diary.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Emotions {
    private Emotions() {
    }

    public static class EmotionData {
        public final int id;
        public final String text;
        public final String emoji;

        EmotionData(int id, String text, String emoji) {
            this.id = id;
            this.text = text;
            this.emoji = emoji;
        }

        public int getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    private static final Map<Integer, EmotionData> EMOTION_DATA_MAP_BY_ID = new LinkedHashMap<>();
    private static final Map<String, EmotionData> EMOTION_DATA_MAP_BY_TEXT = new LinkedHashMap<>();


    static {
        String[][] emotionDefinitions = {
                {"행복", "\uD83D\uDE00"}, // 😀
                {"뿌듯", "\uD83E\uDD79"}, // 🥹
                {"편안", "\uD83D\uDE0C"}, // 😌
                {"설렘", "\uD83D\uDC93"}, // 💓
                {"감사", "\uD83D\uDE4F"}, // 🙏
                {"상쾌", "\uD83E\uDDFC"}, // 🧼
                {"무난", "\uD83D\uDE10"}, // 😐
                {"슬픔", "\uD83D\uDE22"}, // 😢
                {"후회", "\uD83D\uDE2D"}, // 😭
                {"분노", "\uD83D\uDE21"}, // 😡
                {"답답", "\uD83D\uDE23"}, // 😣
                {"짜증", "\uD83D\uDE24"}, // 😤
                {"우울", "\uD83D\uDE1E"}, // 😞
                {"좌절", "\uD83D\uDE29"}, // 😩
                {"수치", "\uD83D\uDE33"}, // 😳
                {"피곤", "\uD83E\uDD71"} // 🥱
        };

        for (int id = 0; id < emotionDefinitions.length; id++) {
            String[] definition = emotionDefinitions[id]; // text, emoji

            String text = definition[0];
            String emoji = definition[1];

            EmotionData data = new EmotionData(id, text, emoji);

            // Mapping by ID and text, both for fast lookup
            EMOTION_DATA_MAP_BY_ID.put(id, data);
            EMOTION_DATA_MAP_BY_TEXT.put(text, data);
        }
    }

    public static EmotionData getEmotionDataById(int emotionId) {
        EmotionData data = EMOTION_DATA_MAP_BY_ID.get(emotionId);

        return (data != null) ? data : getDefaultEmotionData();
    }

    public static EmotionData getEmotionDataByText(String emotionText) {
        EmotionData data = EMOTION_DATA_MAP_BY_TEXT.get(emotionText);

        return (data != null) ? data : getDefaultEmotionData();
    }

    public static int getEmotionIdByText(String emotionText) {
        return getEmotionDataByText(emotionText).getId();
    }

    public static List<String> getAllEmotionTexts() {
        return List.copyOf(EMOTION_DATA_MAP_BY_TEXT.keySet());
    }

    public static List<EmotionData> getAllEmotionDataList() {
        return List.copyOf(EMOTION_DATA_MAP_BY_TEXT.values());
    }
    public static List<Integer> getAllEmotionIds() {
        return List.copyOf(EMOTION_DATA_MAP_BY_ID.keySet());
    }


    private static EmotionData getDefaultEmotionData() {
        return new EmotionData(-1, "몰루", "❓");
    }
}