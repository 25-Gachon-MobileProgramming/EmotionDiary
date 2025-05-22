package kr.co.gachon.emotion_diary.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy; // OnConflictStrategy import 추가

import java.util.Date;
import java.util.List;

@Dao
public interface DiaryDao {
    @Query("SELECT COUNT(*) FROM diaries")
    int getDiaryCount();

    @Query("SELECT * FROM diaries ORDER BY date DESC")
    LiveData<List<Diary>> getAllDiaries();

    // 중복방지
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDiary(Diary diary);

    @Update
    void updateDiary(Diary diary);

    @Delete
    void deleteDiary(Diary diary);

    @Query("SELECT COUNT(DISTINCT strftime('%Y-%m-%d', date / 1000, 'unixepoch')) FROM diaries WHERE date BETWEEN :startDate AND :endDate")
    int getDiaryCountPerDay(Date startDate, Date endDate);

    @Query("SELECT * FROM diaries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    LiveData<List<Diary>> getDiariesForDateRange(Date startDate, Date endDate);

    @Query("SELECT emotion_id, COUNT(*) as count FROM diaries WHERE date BETWEEN :startDate AND :endDate GROUP BY emotion_id")
    List<EmotionCount> getEmotionCounts(Date startDate, Date endDate);

    @Query("SELECT date FROM diaries WHERE date BETWEEN :startDate AND :endDate")
    List<Date> getAllDiaryDates(Date startDate, Date endDate);

    // 특정 날짜의 일기를 조회하는 쿼리 추가
    @Query("SELECT * FROM diaries WHERE date = :date LIMIT 1")
    LiveData<Diary> getDiaryByDate(Date date);

    @Query("SELECT * FROM diaries WHERE date BETWEEN :startDate AND :endDate LIMIT 1")
    LiveData<Diary> getDiaryForSingleDay(Date startDate, Date endDate);
}