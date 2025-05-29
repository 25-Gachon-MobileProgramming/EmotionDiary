package kr.co.gachon.emotion_diary.ui.Remind.emotionStatistics;



import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.AppDatabase;
import kr.co.gachon.emotion_diary.data.DiaryDao;
import kr.co.gachon.emotion_diary.data.EmotionCount;
import kr.co.gachon.emotion_diary.data.Emotions;

public class EmotionStatisticsFragment extends Fragment {

    private HorizontalBarChart barChart;
    private static final String SET_LABEL = "감정별 통계";
    private List<EmotionCount> emotions = new ArrayList<>();
    private List<Integer> displayIds = new ArrayList<>();

    boolean isMonthly;

    Date startDate;
    Date endDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emotion_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChart = view.findViewById(R.id.chart);
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        DiaryDao diaryDao = db.diaryDao();

        if (getArguments() != null) {
            isMonthly = getArguments().getBoolean("isMonthly", true);
            startDate = (Date) getArguments().getSerializable("startDate");
            endDate = (Date) getArguments().getSerializable("endDate");
            Log.d("EmotionStats", "💡 전달받은 isMonthly 값: " + isMonthly);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());



        try {

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                    emotions = diaryDao.getEmotionCounts(startDate, endDate);


                requireActivity().runOnUiThread(() -> {
                    TextView placeHolder = view.findViewById(R.id.emotion_statistics_hint);
                    if (emotions.isEmpty()) {
                        placeHolder.setVisibility(View.VISIBLE);
                    }else{
                        placeHolder.setVisibility(View.GONE);
                    }
                    configureChartAppearance();
                    BarData data = createChartData();
                    prepareChartData(data);
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void configureChartAppearance() {
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraOffsets(10f, 20f, 10f, 10f);
        barChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(15f);


        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                Log.d("ChartEntry", "id=" + index);
                return Emotions.getEmotionDataById(displayIds.get(index)).getEmoji();
            }
        });

        YAxis axisLeft = barChart.getAxisLeft();
        axisLeft.setDrawLabels(true);
        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisMinimum(0f);
        axisLeft.setGranularity(1f);

        YAxis axisRight = barChart.getAxisRight();
        axisRight.setEnabled(false);
    }

    private BarData createChartData() {
        ArrayList<BarEntry> values = new ArrayList<>();
        displayIds.clear();

        for (int i = 0; i < Emotions.getAllEmotionIds().size(); i++) {
            int id = Emotions.getAllEmotionIds().get(i);
            EmotionCount ec = findEmotionById(emotions, id); // 아래 함수 참고
            int count = ec != null ? ec.count : 0;

            Log.d("ChartXValue", "BarEntry x=" + i);

            values.add(new BarEntry(i, count));
            displayIds.add(id);
//            Log.d("ChartEntry", "id=" + id + ", count=" + count + ", emoji=" + Emotions.getEmotionDataById(id).getEmoji() + values.size());
        }

        BarDataSet set = new BarDataSet(values, SET_LABEL);
        set.setDrawIcons(false);
        set.setDrawValues(true);
        set.setColor(Color.parseColor("#FF0000"));

        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "회";
            }
        });

        BarData data = new BarData(set);
        data.setBarWidth(0.2f);
        data.setValueTextSize(15);
        return data;
    }

    private void prepareChartData(BarData data) {
        barChart.setData(data);
        barChart.invalidate();
    }
    private EmotionCount findEmotionById(List<EmotionCount> list, int id) {
        for (EmotionCount ec : list) {
            if (ec.emotion_id == id) return ec;
        }
        return null;
    }

}