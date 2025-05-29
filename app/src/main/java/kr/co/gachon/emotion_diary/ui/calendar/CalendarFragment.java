package kr.co.gachon.emotion_diary.ui.calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.Diary;
import kr.co.gachon.emotion_diary.data.Emotions;
import kr.co.gachon.emotion_diary.databinding.FragmentCalendarBinding;
import kr.co.gachon.emotion_diary.helper.Helper;
import kr.co.gachon.emotion_diary.ui.Remind.WriteRate.RateActivity;
import kr.co.gachon.emotion_diary.ui.taro.TaroActivity;
import kr.co.gachon.emotion_diary.ui.writePage.DiaryWriteActivity;

public class CalendarFragment extends Fragment {
    private FragmentCalendarBinding binding;
    private TableLayout calendarTable;
    private TextView monthYearText;
    private ImageView prevMonthButton;
    private ImageView nextMonthButton;

    private List<Diary> monthlyDiaries;

    private CalendarViewModel calendarViewModel;

    private int calendarYear;
    private int calendarMonth;

    private Helper helper;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        helper = new Helper(getContext());

        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        calendarTable = binding.calendar;
        monthYearText = binding.monthYearText;
        prevMonthButton = binding.prevMonthButton;
        nextMonthButton = binding.nextMonthButton;

        TextView showMonthlyStat = binding.showMonthlyStat;
        TextView showYearlyStat = binding.showYearlyStat;

        showMonthlyStat.setOnClickListener(v -> moveToStatActivity(true, calendarYear, calendarMonth));
        showYearlyStat.setOnClickListener(v -> moveToStatActivity(false, calendarYear, calendarMonth));

        monthYearText.setOnClickListener(v -> calendarViewModel.goToCurrentMonth());

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

    private void moveToStatActivity(boolean isMonthly, int year, int month) {
        String term = year + "-" + month;

        Intent intent = new Intent(getActivity(), RateActivity.class);
        intent.putExtra("isMonthly", isMonthly);
        intent.putExtra("term", term);

        startActivity(intent);
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

            calendarMonth = month;
            calendarYear = year;
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
                dayNameTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
                dayNamesRow.addView(dayNameTextView);
            }

            calendarTable.addView(dayNamesRow);

            TableRow.LayoutParams dayOfTheWeekParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            dayOfTheWeekParams.setMargins(0, 0, 0, helper.dpToPx(8));

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
            boolean isDiaryExist = (diary != null);
            String calendarText = isDiaryExist
                    ? Emotions.getEmotionDataById(diary.getEmotionId()).getEmoji()
                    : String.valueOf(currentDayOfMonth);

            boolean isDiaryIncomplete = isDiaryExist && diary.getGptAnswer() == null;
            boolean isDiaryComplete = isDiaryExist && diary.getGptAnswer() != null;

            if (isDiaryIncomplete) calendarText = "⏳";

            TextView dayTextView = createEmotionTextView(calendarText);
            dayTextView.setTextSize(Dimension.SP, 20);

            if (isDiaryExist) {
                dayTextView.setTextSize(Dimension.SP, 40);
            }

            int finalCurrentDayOfMonth = currentDayOfMonth; // For the lambda wtf
            dayTextView.setOnClickListener(v -> {
                String dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, finalCurrentDayOfMonth);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                try {
                    Date date = formatter.parse(dateString);
                    assert date != null;

                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    Calendar targetDate = Calendar.getInstance();
                    targetDate.setTime(date);
                    targetDate.set(Calendar.HOUR_OF_DAY, 0);
                    targetDate.set(Calendar.MINUTE, 0);
                    targetDate.set(Calendar.SECOND, 0);
                    targetDate.set(Calendar.MILLISECOND, 0);

                    if (targetDate.after(today)) return;

                    if (!isDiaryExist) {
                        Intent intent = new Intent(getActivity(), DiaryWriteActivity.class);
                        intent.putExtra("selectedDate", date.getTime());
                        startActivity(intent);
                    }

                    if (isDiaryIncomplete) {
                        Intent intent = new Intent(getActivity(), TaroActivity.class);
                        intent.putExtra("date", date.getTime());
                        intent.putExtra("title", diary.getTitle());
                        intent.putExtra("content", diary.getContent());
                        intent.putExtra("emotion", Emotions.getEmotionDataById(diary.getEmotionId()).getText());
                        startActivity(intent);
                    }

                    if (isDiaryComplete) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setTitle("선택해주세요");
                        builder.setMessage("원하는 작업을 선택하세요.");

                        builder.setPositiveButton("결과", (dialog, which) -> {
                            Intent intent = new Intent(getActivity(), TaroActivity.class);
                            intent.putExtra("date", date.getTime());
                            intent.putExtra("title", diary.getTitle());
                            intent.putExtra("content", diary.getContent());
                            intent.putExtra("emotion", Emotions.getEmotionDataById(diary.getEmotionId()).getText());
                            startActivity(intent);

                            dialog.dismiss();
                        });

                        builder.setNegativeButton("수정", (dialog, which) -> {
                            Intent intent = new Intent(getActivity(), DiaryWriteActivity.class);
                            intent.putExtra("selectedDate", date.getTime());
                            startActivity(intent);

                            dialog.dismiss();
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            });

            boolean isToday = Calendar.getInstance().get(Calendar.YEAR) == calendarYear &&
                    Calendar.getInstance().get(Calendar.MONTH) + 1 == calendarMonth &&
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == currentDayOfMonth;


            LinearLayout diaryLayout = new LinearLayout(getContext());
            diaryLayout.setOrientation(LinearLayout.VERTICAL);
            diaryLayout.setGravity(Gravity.CENTER);
            diaryLayout.addView(dayTextView);

            if (isToday)
                diaryLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_today_background));


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
        dayOfTheWeekParams.setMargins(0, 0, 0, helper.dpToPx(8));

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
        textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorOnSecondary));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}