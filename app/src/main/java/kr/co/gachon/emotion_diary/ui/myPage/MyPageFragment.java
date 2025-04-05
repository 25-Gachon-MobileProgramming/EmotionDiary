package kr.co.gachon.emotion_diary.ui.myPage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import kr.co.gachon.emotion_diary.databinding.FragmentMypageBinding;
import kr.co.gachon.emotion_diary.ui.Remind.RemindActivity;

public class MyPageFragment extends Fragment {

    private FragmentMypageBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MyPageViewModel myPageViewModel =
                new ViewModelProvider(this).get(MyPageViewModel.class);

        binding = FragmentMypageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        View yearRemindButton = binding.remindYearTouchView;

        yearRemindButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RemindActivity.class);

            startActivity(intent);
        });

        View monthRemindButton = binding.remindMonthTouchView;

        monthRemindButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RemindActivity.class);

            startActivity(intent);
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}