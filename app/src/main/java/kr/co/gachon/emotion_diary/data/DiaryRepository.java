package kr.co.gachon.emotion_diary.data;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    public void insert(Diary diary) {
        executorService.execute(() -> diaryDao.insertDiary(diary));
    }

    public void update(Diary diary) {
        executorService.execute(() -> diaryDao.updateDiary(diary));
    }

    public void delete(Diary diary) {
        executorService.execute(() -> diaryDao.deleteDiary(diary));
    }

    public void insertDummyData() {
        executorService.execute(() -> {
            if (diaryDao.getDiaryCount() == 0) {
                Calendar calendar = Calendar.getInstance();

                // dummy data from gpt
                calendar.set(2025, Calendar.FEBRUARY, 4, 10, 30);
                insert(new Diary("과제를함", "하루종일과제를함", calendar.getTime(), Emotions.getEmotionIdByText("뿌듯")));

                calendar.set(2025, Calendar.APRIL, 2, 10, 39);
                insert(new Diary("즐거운 하루", "오늘 날씨가 정말 좋았어요!", calendar.getTime(), Emotions.getEmotionIdByText("행복")));
                calendar.set(2025, Calendar.MARCH, 3, 15, 0);
                insert(new Diary("힘든 날", "회사에서 어려운 일이 있었어요.", calendar.getTime(), Emotions.getEmotionIdByText("슬픔")));

                calendar.set(2025, Calendar.APRIL, 4, 18, 45);
                insert(new Diary("평범한 저녁", "맛있는 저녁을 먹고 휴식했어요.", calendar.getTime(), Emotions.getEmotionIdByText("편안")));

                calendar.set(2025, Calendar.APRIL, 5, 9, 0);
                // "신남" is not defined in Emotions, just for error test
                insert(new Diary("신나는 아침", "새로운 아이디어가 떠올랐어요!", calendar.getTime(), Emotions.getEmotionIdByText("신남")));

                calendar.set(2025, Calendar.MAY, 7, 18, 45);
                insert(new Diary("평범한 저녁", "졸려요 잘래요", calendar.getTime(), Emotions.getEmotionIdByText("편안")));

                Log.d("RoomExample", "더미 데이터 삽입 완료");
            } else {
                Log.d("RoomExample", "이미 데이터가 존재하여 더미 데이터를 삽입하지 않음");
            }
        });
    }
}
