package kr.co.gachon.emotion_diary.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kr.co.gachon.emotion_diary.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<LocalDate> dateList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupDateViewPager();
        return root;
    }

    private void setupDateViewPager() {
        dateList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = -5; i <= 5; i++) {
            dateList.add(today.plusDays(i));
        }

        DatePagerAdapter adapter = new DatePagerAdapter(dateList);
        binding.dateViewPager.setAdapter(adapter);
        binding.dateViewPager.setCurrentItem(5, false);  // 오늘 날짜 중앙에

        // 캐러셀 효과
        binding.dateViewPager.setClipToPadding(false);
        binding.dateViewPager.setClipChildren(false);
        binding.dateViewPager.setOffscreenPageLimit(3);
        binding.dateViewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        binding.dateViewPager.setPadding(60, 0, 60, 0);

        binding.dateViewPager.setPageTransformer((page, position) -> {
            float scale = 1 - Math.abs(position) * 0.2f;
            page.setScaleY(scale);
            page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


