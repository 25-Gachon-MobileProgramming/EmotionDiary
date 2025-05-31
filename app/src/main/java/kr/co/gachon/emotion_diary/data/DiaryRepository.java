package kr.co.gachon.emotion_diary.data;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiaryRepository {

    private final DiaryDao diaryDao;
    private final LiveData<List<Diary>> allDiaries;
    private final ExecutorService executorService;

    public DiaryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        diaryDao = db.diaryDao();
        allDiaries = diaryDao.getAllDiaries();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Diary>> getAllDiaries() {
        return allDiaries;
    }

    public LiveData<Integer> getConsecutiveWritingDays() {
        return Transformations.map(allDiaries, diaries -> {
            if (diaries == null || diaries.isEmpty()) return 0;

            Set<String> diaryEntryDates = new HashSet<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (Diary diary : diaries) {
                diaryEntryDates.add(sdf.format(diary.getDate()));
            }


            Calendar calendar = Calendar.getInstance();
            int consecutiveDays = 1;


            // check today
            String todayStr = sdf.format(calendar.getTime());
            if (!diaryEntryDates.contains(todayStr)) return 0;

            calendar.add(Calendar.DAY_OF_YEAR, -1); // move yesterday

            while (true) {
                // if no diary entry, break
                String previousDayStr = sdf.format(calendar.getTime());
                if (!diaryEntryDates.contains(previousDayStr)) break;

                consecutiveDays++;
                calendar.add(Calendar.DAY_OF_YEAR, -1); // move yesterday 1 more
            }

            return consecutiveDays;
        });
    }

    public void insert(Diary diary) {
        executorService.execute(() -> diaryDao.insertDiary(diary));
    }

    public void update(Diary diary) {
        executorService.execute(() -> diaryDao.updateDiary(diary));
    }

    public void delete(Diary diary) {
        executorService.execute(() -> diaryDao.deleteDiary(diary));
    }


    public interface DiaryMapCallback {
        void onResult(Map<LocalDate, Diary> diaryMap);
    }

    public void getDiaryMapForRangeAsync(LocalDate start, LocalDate end, DiaryMapCallback callback) {
        executorService.execute(() -> {
            Map<LocalDate, Diary> diaryMap = new HashMap<>();
            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            startCal.set(start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth(), 0, 0, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            endCal.set(end.getYear(), end.getMonthValue() - 1, end.getDayOfMonth(), 23, 59, 59);
            endCal.set(Calendar.MILLISECOND, 999);

            List<Diary> diaryList = diaryDao.getDiariesForSpecificDayOnce(startCal.getTime(), endCal.getTime());

            for (Diary diary : diaryList) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(diary.getDate());
                LocalDate key = LocalDate.of(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)
                );
                diaryMap.put(key, diary);
            }

            // 메인스레드로 콜백 전달
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(diaryMap));
        });
    }


    public void insertDummyData() {
        executorService.execute(() -> {
            if (diaryDao.getDiaryCount() == 0) {
                Calendar calendar = Calendar.getInstance();

                // dummy data from gpt
                calendar.set(2025, Calendar.FEBRUARY, 4, 10, 30);
                insert(new Diary("과제를함", "하루종일과제를함", calendar.getTime(), Emotions.getEmotionIdByText("뿌듯"), null, null));

                calendar.set(2025, Calendar.APRIL, 2, 10, 39);
                insert(new Diary("즐거운 하루", "오늘 날씨가 정말 좋았어요!", calendar.getTime(), Emotions.getEmotionIdByText("행복"), null, null));
                calendar.set(2025, Calendar.MARCH, 3, 15, 0);
                insert(new Diary("힘든 날", "회사에서 어려운 일이 있었어요.", calendar.getTime(), Emotions.getEmotionIdByText("슬픔"), null, null));

                calendar.set(2025, Calendar.APRIL, 4, 18, 45);
                insert(new Diary("평범한 저녁", "맛있는 저녁을 먹고 휴식했어요.", calendar.getTime(), Emotions.getEmotionIdByText("편안"), null, null));

                calendar.set(2025, Calendar.APRIL, 5, 9, 0);
                // "신남" is not defined in Emotions, just for error test
                insert(new Diary("신나는 아침", "새로운 아이디어가 떠올랐어요!", calendar.getTime(), Emotions.getEmotionIdByText("신남"), null, null));
                calendar.set(2025, Calendar.APRIL, 4, 9, 29);
                // "신남" is not defined in Emotions, just for error test
                insert(new Diary("신나는 밤", "새로운 아이디어가 떠올랐어요!", calendar.getTime(), Emotions.getEmotionIdByText("신남"), null, null));

                calendar.set(2025, Calendar.MAY, 7, 18, 45);
                insert(new Diary("평범한 저녁", "졸려요 잘래요", calendar.getTime(), Emotions.getEmotionIdByText("편안"), null, null));

                calendar = Calendar.getInstance();

                for (int i = 0; i < 5; i++) {
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                    insert(new Diary("test1", "test1", calendar.getTime(), (int) (Math.random() * 16) + 1, null, null));
                }


                Log.d("RoomExample", "더미 데이터 삽입 완료");
            } else {
                Log.d("RoomExample", "이미 데이터가 존재하여 더미 데이터를 삽입하지 않음");
            }
        });
    }
}
