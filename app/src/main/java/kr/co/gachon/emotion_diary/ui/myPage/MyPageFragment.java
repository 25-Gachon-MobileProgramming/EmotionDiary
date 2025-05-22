package kr.co.gachon.emotion_diary.ui.myPage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import kr.co.gachon.emotion_diary.data.DiaryDao;
import kr.co.gachon.emotion_diary.databinding.FragmentMypageBinding;
import kr.co.gachon.emotion_diary.ui.Remind.WriteRate.RateActivity;

public class MyPageFragment extends Fragment {

    private FragmentMypageBinding binding;

    private SharedPreferences sharedPreferences;
    private DiaryDao diaryDao;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MyPageViewModel myPageViewModel =
                new ViewModelProvider(this).get(MyPageViewModel.class);

        binding = FragmentMypageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedPreferences = requireContext().getSharedPreferences("avatar_pref", Context.MODE_PRIVATE);
        String savedNickname = sharedPreferences.getString("nickname", "사용자");
        binding.nickname.setText(savedNickname);

        myPageViewModel.getConsecutiveWritingDays().observe(getViewLifecycleOwner(), days -> {
            if (days != null) {
                String message = "오늘은 아직 일기를 작성하지 않았어요.";
                if (days > 0) message = "🔥" + days + "일 연속으로 일기 작성중🔥";

                binding.days.setText(message);
            }
        });

        View yearRemindButton = binding.remindYearTouchView;

        yearRemindButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RateActivity.class);
            intent.putExtra("isMonthly", false);
            startActivity(intent);
        });

        View monthRemindButton = binding.remindMonthTouchView;

        monthRemindButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RateActivity.class);
            intent.putExtra("isMonthly", true);
            startActivity(intent);
        });

        ImageButton nicknameChangeButton = binding.profileChange;

        nicknameChangeButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("닉네임 변경");

            final EditText input = new EditText(requireContext());
            input.setHint("새 닉네임을 입력하세요");
            builder.setView(input);

            builder.setPositiveButton("확인", (dialog, which) -> {
                String newNickname = input.getText().toString().trim();
                if (!newNickname.isEmpty()) {

                    sharedPreferences.edit().putString("nickname", newNickname).apply();
                    binding.nickname.setText(newNickname);
                }
            });


            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}