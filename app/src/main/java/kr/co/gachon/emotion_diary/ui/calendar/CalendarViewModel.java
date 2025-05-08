package kr.co.gachon.emotion_diary.ui.calendar;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations; // Transformations import 추가

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Collections; // Collections import 추가

import kr.co.gachon.emotion_diary.data.AppDatabase;
import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.DiaryDao;

public class CalendarViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> _currentYear = new MutableLiveData<>();
    private final MutableLiveData<Integer> _currentMonth = new MutableLiveData<>();
    private final LiveData<List<Diary>> _monthlyDiaries;

    public final LiveData<Integer> currentYear = _currentYear;
    public final LiveData<Integer> currentMonth = _currentMonth;
    public LiveData<List<Diary>> getMonthlyDiaries() {
        return _monthlyDiaries;
    }

    private final DiaryDao diaryDao;
    private final ExecutorService executorService;

    public CalendarViewModel(Application application) {
        super(application);

        AppDatabase db = AppDatabase.getDatabase(application);
        diaryDao = db.diaryDao();
        executorService = Executors.newFixedThreadPool(2);

        _currentYearMonth.addSource(_currentYear, year -> {
            Integer month = _currentMonth.getValue();
            if (year != null && month != null) {
                _currentYearMonth.setValue(new Pair<>(year, month));
            }
        });
        _currentYearMonth.addSource(_currentMonth, month -> {
            Integer year = _currentYear.getValue();
            if (year != null && month != null) {
                _currentYearMonth.setValue(new Pair<>(year, month));
            }
        });

        _monthlyDiaries = Transformations.switchMap(_currentYearMonth, yearMonth -> {
            if (yearMonth == null)
                return new MutableLiveData<>(Collections.emptyList());

            Date start = getStartDateOfMonth(yearMonth.first, yearMonth.second);
            Date end = getEndDateOfMonth(yearMonth.first, yearMonth.second);

            return diaryDao.getDiariesForDateRange(start, end);
        });

        Calendar calendar = Calendar.getInstance();
        int initialYear = calendar.get(Calendar.YEAR);
        int initialMonth = calendar.get(Calendar.MONTH) + 1; // 0-based month

        _currentYear.setValue(initialYear);
        _currentMonth.setValue(initialMonth); // It's 0-based
    }

    private Date getStartDateOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private Date getEndDateOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    public void goToNextMonth() {
        Integer year = _currentYear.getValue();
        Integer month = _currentMonth.getValue();

        if (year != null && month != null) {
            if (month == 12) {
                // In the last month of the year
                _currentYear.setValue(year + 1);
                _currentMonth.setValue(1);
            } else {
                _currentMonth.setValue(month + 1);
            }
        }
    }

    public void goToPreviousMonth() {
        Integer year = _currentYear.getValue();
        Integer month = _currentMonth.getValue();

        if (year != null && month != null) {
            if (month == 1) {
                // In the first month of the year
                _currentYear.setValue(year - 1);
                _currentMonth.setValue(12);
            } else {
                _currentMonth.setValue(month - 1);
            }
        }
    }

    public void goToMonth(int year, int month) {
        _currentYear.setValue(year);
        _currentMonth.setValue(month);
    }

    public void goToCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;

        goToMonth(currentYear, currentMonth);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}