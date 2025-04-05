package kr.co.gachon.emotion_diary.ui.Remind;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import kr.co.gachon.emotion_diary.R;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import kr.co.gachon.emotion_diary.ui.Remind.CircleGraphView;
import kr.co.gachon.emotion_diary.databinding.RemindBinding;


public class RemindActivity extends AppCompatActivity {

    private RemindBinding binding;
    private CircleGraphView circleGraphView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remind);

        binding = RemindBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupCircleGraphView();
    }
    private int rateOfWrite() { //일기 작성률 리턴
        return 0;
    }
    private void setupCircleGraphView() {
        circleGraphView = binding.circle;
        circleGraphView.animateSection(0, 0f, 95f); //95f가 작성한 일기 비율이 들어올 부분
        circleGraphView.animateSection(1, 0f, 5f); // 5f가 작성안한 일기 비율

    }
}
