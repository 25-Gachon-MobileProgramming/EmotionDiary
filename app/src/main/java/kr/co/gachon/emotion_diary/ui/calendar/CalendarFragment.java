package kr.co.gachon.emotion_diary.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.Emotions;
import kr.co.gachon.emotion_diary.databinding.FragmentCalendarBinding;
import kr.co.gachon.emotion_diary.ui.Remind.WriteRate.RateActivity;

public class CalendarFragment extends Fragment {
    private FragmentCalendarBinding binding;
    private TableLayout calendarTable;
    private TextView monthYearText;
    private Button prevMonthButton;
    private Button nextMonthButton;
    private Button showMonthlyStat;

    private List<Diary> monthlyDiaries;

    private CalendarViewModel calendarViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        calendarTable = binding.calendar;
        monthYearText = binding.monthYearText;
        prevMonthButton = binding.prevMonthButton;
        nextMonthButton = binding.nextMonthButton;
        showMonthlyStat = binding.showMonthlyStat;

        monthYearText.setOnClickListener(v -> calendarViewModel.goToCurrentMonth());
        showMonthlyStat.setOnClickListener(v -> {
            // TODO: isMonthly 대신 어떤 달의 통계를 보여줄지 바꿔야 함
            Intent intent = new Intent(getActivity(), RateActivity.class);
            intent.putExtra("isMonthly", true);
            startActivity(intent);
        });

        // Set cell size dynamically
        calendarTable.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calendarTable.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                updateCellSizes();

                recreateCalendar(calendarViewModel.currentYear.getValue(), calendarViewModel.currentMonth.getValue(), monthlyDiaries);
            }
        });

        observeViewModel();
        setEventListeners();

        return root;
    }

    // Execute when data changed
    private void observeViewModel() {
        calendarViewModel.currentYear.observe(getViewLifecycleOwner(), year -> {
            updateMonthYearText(year, calendarViewModel.currentMonth.getValue());
            recreateCalendar(year, calendarViewModel.currentMonth.getValue(), monthlyDiaries);
        });

        calendarViewModel.currentMonth.observe(getViewLifecycleOwner(), month -> {
            updateMonthYearText(calendarViewModel.currentYear.getValue(), month);
            recreateCalendar(calendarViewModel.currentYear.getValue(), month, monthlyDiaries);
        });

        calendarViewModel.getMonthlyDiaries().observe(getViewLifecycleOwner(), diaries -> {
            monthlyDiaries = diaries;
            recreateCalendar(calendarViewModel.currentYear.getValue(), calendarViewModel.currentMonth.getValue(), monthlyDiaries);
        });
    }

    private void setEventListeners() {
        prevMonthButton.setOnClickListener(v -> calendarViewModel.goToPreviousMonth());
        nextMonthButton.setOnClickListener(v -> calendarViewModel.goToNextMonth());
    }

    private void updateMonthYearText(Integer year, Integer month) {
        if (year != null && month != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, 1); // -1 cause zero-based month
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월", Locale.getDefault());
            monthYearText.setText(sdf.format(cal.getTime()));
        }
    }

    private void recreateCalendar(Integer year, Integer month, List<Diary> diaries) {
        if (year != null && month != null) {
            int childCount = calendarTable.getChildCount();

            // start: 1 => "day of the week" doesn't need to be removed
            if (childCount > 1) calendarTable.removeViews(1, childCount - 1);

            createCalendar(year, month, diaries);
            updateCellSizes();
        }
    }

    private void createCalendar(int year, int month, List<Diary> diaries) {
        // Get the first day of the month
        Calendar firstDayCalendar = Calendar.getInstance();
        firstDayCalendar.set(year, month - 1, 1); // -1 cause zero-based month
        int dayOfWeekNumber = firstDayCalendar.get(Calendar.DAY_OF_WEEK); // sun(1) ~ sat(7)
        int daysInMonth = firstDayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // First time running
        if (calendarTable.getChildCount() == 0) {
            TableRow dayNamesRow = new TableRow(getContext());
            String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

            for (String dayName : dayNames) {
                TextView dayNameTextView = createEmotionTextView(dayName);
                dayNameTextView.setTextSize(Dimension.SP, 25);
                dayNameTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.calendar_text));
                dayNamesRow.addView(dayNameTextView);
            }

            calendarTable.addView(dayNamesRow);

            TableRow.LayoutParams dayOfTheWeekParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            dayOfTheWeekParams.setMargins(0, 0, 0, dpToPx(8));

            for (int j = 0; j < dayNamesRow.getChildCount(); j++) {
                View cell = dayNamesRow.getChildAt(j);
                cell.setLayoutParams(dayOfTheWeekParams);
            }
        }

        TableRow currentRow = new TableRow(getContext());

        // TODO: Instead of empty, fill prev month? -> but with emoji, calendar may change in the future
        // Calendar의 형태를 유지하기 위해서 감정 이모지를 넣고 나서 이전 달 이후 달을 잘 보여주는 방식을 고민.

        // Before the first day of the month, add empty cells
        for (int i = 1; i < dayOfWeekNumber; i++) {
            TextView emptyTextView = createEmotionTextView("");
            currentRow.addView(emptyTextView);
        }

        // Add the days of the month
        int currentDayOfMonth = 1;

        while (currentDayOfMonth <= daysInMonth) {
            Diary diary = findDiaryForDay(year, month, currentDayOfMonth, diaries);
            Boolean isDiaryExist = (diary != null);
            String calendarText = isDiaryExist
                    ? Emotions.getEmotionDataById(diary.getEmotionId()).getEmoji()
                    : String.valueOf(currentDayOfMonth);

            TextView dayTextView = createEmotionTextView(calendarText);
            dayTextView.setTextSize(Dimension.SP, 20);

            if (isDiaryExist) {
                dayTextView.setTextSize(Dimension.SP, 40);
            }

            // int finalCurrentDayOfMonth = currentDayOfMonth; // For the lambda wtf
            dayTextView.setOnClickListener(v -> {
                // Toast.makeText(getContext(), year + "년 " + month + "월 " + finalCurrentDayOfMonth + "일 클릭", Toast.LENGTH_SHORT).show();
                // 일기 작성했는지 판단하는 로직 => DB에서 불러올때 처리하는게 좋을 듯?

                // Intent intent = new Intent(getActivity(), WriteDiaryActivity.class);
                // intent.putExtra("year", year);
                // intent.putExtra("month", month);
                // intent.putExtra("day", finalCurrentDayOfMonth);
                // startActivity(intent);
            });

            LinearLayout diaryLayout = new LinearLayout(getContext());
            diaryLayout.setOrientation(LinearLayout.VERTICAL);
            diaryLayout.setGravity(Gravity.CENTER);
            diaryLayout.addView(dayTextView);

            currentRow.addView(diaryLayout);

            int currentTableNumber = dayOfWeekNumber + currentDayOfMonth - 1;
            boolean isLastDayOfWeek = currentTableNumber % 7 == 0;

            // Last day of the month
            if (!isLastDayOfWeek && currentDayOfMonth == daysInMonth) {
                // Fill the rest of the row with empty cells
                while (currentRow.getChildCount() < 7) {
                    TextView emptyTextView = createEmotionTextView("");
                    currentRow.addView(emptyTextView);
                }

                calendarTable.addView(currentRow);
            }

            // Last day of the week
            if (isLastDayOfWeek) {
                calendarTable.addView(currentRow);
                currentRow = new TableRow(getContext());
            }

            currentDayOfMonth++;
        }
    }

    private void updateCellSizes() {
        int cellSize = (calendarTable.getWidth() - (calendarTable.getPaddingStart() + calendarTable.getPaddingEnd())) / 7;

        TableRow.LayoutParams dayOfTheWeekParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dayOfTheWeekParams.setMargins(0, 0, 0, dpToPx(8));

        if (calendarTable.getChildCount() > 0) {
            TableRow dayOfTheWeekRow = (TableRow) calendarTable.getChildAt(0);

            for (int j = 0; j < dayOfTheWeekRow.getChildCount(); j++) {
                View cell = dayOfTheWeekRow.getChildAt(j);
                cell.setLayoutParams(dayOfTheWeekParams);
            }
        }

        TableRow.LayoutParams params = new TableRow.LayoutParams(cellSize, cellSize);

        for (int i = 1; i < calendarTable.getChildCount(); i++) {
            TableRow row = (TableRow) calendarTable.getChildAt(i);

            for (int j = 0; j < row.getChildCount(); j++) {
                View cell = row.getChildAt(j);
                cell.setLayoutParams(params);
            }
        }
    }

    private TextView createEmotionTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.calendar_text));
        textView.setGravity(android.view.Gravity.CENTER);

        return textView;
    }

    private Diary findDiaryForDay(int year, int month, int day, List<Diary> diaries) {
        if (diaries == null) {
            return null;
        }
        Calendar targetCal = Calendar.getInstance();
        targetCal.set(year, month - 1, day, 0, 0, 0); // zero-based month
        targetCal.set(Calendar.MILLISECOND, 0);

        for (Diary diary : diaries) {
            Calendar diaryCal = Calendar.getInstance();
            diaryCal.setTime(diary.getDate());
            diaryCal.set(Calendar.HOUR_OF_DAY, 0);
            diaryCal.set(Calendar.MINUTE, 0);
            diaryCal.set(Calendar.SECOND, 0);
            diaryCal.set(Calendar.MILLISECOND, 0);

            if (targetCal.equals(diaryCal)) return diary;
        }
        return null;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;

        return Math.round(dp * density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}