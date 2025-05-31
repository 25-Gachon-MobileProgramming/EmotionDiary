package kr.co.gachon.emotion_diary.ui.myPage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import kr.co.gachon.emotion_diary.R;
import kr.co.gachon.emotion_diary.data.DiaryDao;
import kr.co.gachon.emotion_diary.databinding.FragmentMypageBinding;
import kr.co.gachon.emotion_diary.notification.AlarmScheduler;
import kr.co.gachon.emotion_diary.utils.SharedPreferencesUtils;
import kr.co.gachon.emotion_diary.widget.ConsecutiveWidgetProvider;

public class MyPageFragment extends Fragment {

    private FragmentMypageBinding binding;

    private SharedPreferences sharedPreferences;
    private DiaryDao diaryDao;

    // 이미지 선택 런처
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        copyUriToInternalStorage(selectedImageUri);
                    }
                }
            }
    );

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MyPageViewModel myPageViewModel =
                new ViewModelProvider(this).get(MyPageViewModel.class);

        binding = FragmentMypageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedPreferences = requireContext().getSharedPreferences("avatar_pref", Context.MODE_PRIVATE);


        // 프로필 이미지 불러오기
        String savedPath = sharedPreferences.getString("profileImage", null);
        if (savedPath != null) {
            File file = new File(savedPath);
            if (file.exists()) {
                binding.profileImage.setImageURI(Uri.fromFile(file));
            }
        }

        // 이미지 클릭 시 갤러리 열기
        binding.profileImageChangeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        String savedNickname = sharedPreferences.getString("nickname", "사용자");
        binding.nickname.setText(savedNickname);

        myPageViewModel.getConsecutiveWritingDays().observe(getViewLifecycleOwner(), days -> {
            if (days != null) {
                String message = "오늘은 아직 일기를 작성하지 않았어요.";
                if (days > 0) {
                    message = "🔥" + days + "일 연속으로 일기 작성중🔥";
                    ConsecutiveWidgetProvider.updateAllWidgets(requireContext(), days);
                }

                binding.days.setText(message);
            }
        });



        binding.nicknameChangeLayout.setOnClickListener(view -> {
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

            AlertDialog dialog = builder.show();


            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorSecondary));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorSecondary));
        });
        View notification = binding.notificationTouchView;


        notification.setOnClickListener(view -> {
            binding.timePickerCard.setVisibility(View.VISIBLE);
        });

        Button cancel = binding.btnCancel;
        Button confirm = binding.btnConfirm;

        cancel.setOnClickListener(v -> {
            binding.timePickerCard.setVisibility(View.GONE);
        });


        confirm.setOnClickListener(v -> {
            int hour = binding.timePicker.getHour();
            int minute = binding.timePicker.getMinute();


            SharedPreferencesUtils.saveTime(requireContext(), hour, minute);
            AlarmScheduler.scheduleDiaryReminder(requireContext(), hour, minute);

            Log.d("TimePicker", "선택된 시간: " + hour + ":" + minute);
            Toast.makeText(requireActivity(), "알림 시간이 " + hour + ":" + minute + " 으로 변경되었습니다.", Toast.LENGTH_SHORT).show();

            binding.timePickerCard.setVisibility(View.GONE);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void copyUriToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
            File file = new File(requireContext().getFilesDir(), "profile.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            binding.profileImage.setImageURI(Uri.fromFile(file));
            // 로컬 URI 저장
            sharedPreferences.edit().putString("profileImage", file.getAbsolutePath()).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}