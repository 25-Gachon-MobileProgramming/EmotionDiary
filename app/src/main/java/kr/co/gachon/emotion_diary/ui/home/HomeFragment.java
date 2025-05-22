package kr.co.gachon.emotion_diary.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kr.co.gachon.emotion_diary.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private List<LocalDate> dateList;
    private OnDateSelectedListener dateSelectedListener; // 리스너 선언

    // 리스너 인터페이스 정의
    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate selectedDate);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // MainActivity가 OnDateSelectedListener를 구현하는지 확인
        if (context instanceof OnDateSelectedListener) {
            dateSelectedListener = (OnDateSelectedListener) context;
        } else {
            // Activity가 인터페이스를 구현하지 않은 경우 (런타임 오류 방지)
            // throw new RuntimeException(context.toString() + " must implement OnDateSelectedListener");
        }
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        setupDateViewPager();

        // 뷰페이저 페이지 변경 리스너 추가
        binding.dateViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (dateSelectedListener != null) {
                    // 현재 선택된 날짜를 Activity로 전달
                    dateSelectedListener.onDateSelected(dateList.get(position));
                }
            }
        });

        setupDatePager();


        return root;
    }

    private void setupDatePager() {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = -5; i <= 1; i++) {
            dateList.add(today.plusDays(i));
        }

        DatePagerAdapter adapter = new DatePagerAdapter(dateList);
        binding.dateViewPager.setAdapter(adapter);


        binding.dateViewPager.setCurrentItem(5, false);


        binding.dateViewPager.setCurrentItem(5, false); // 초기 설정 시 애니메이션 없이
        if (dateSelectedListener != null) {
            dateSelectedListener.onDateSelected(dateList.get(5)); // 초기 날짜 전달
        }

        binding.dateViewPager.setClipToPadding(false);
        binding.dateViewPager.setClipChildren(false);
        binding.dateViewPager.setOffscreenPageLimit(3);
        binding.dateViewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        binding.dateViewPager.setPadding(120, 0, 120, 0);

        binding.dateViewPager.setPageTransformer((page, position) -> {
            float scale = 1 - Math.abs(position) * 0.2f;
            page.setScaleY(scale);
            page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dateSelectedListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}

}

