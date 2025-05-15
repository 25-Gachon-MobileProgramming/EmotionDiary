package kr.co.gachon.emotion_diary.ui.OnBoarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

import kr.co.gachon.emotion_diary.MainActivity;
import kr.co.gachon.emotion_diary.R;

public class AvatarActivity extends AppCompatActivity {

    Button button;
    EditText editText;
    private Context context;
    RadioGroup radGender;
    RadioButton genderM;
    RadioButton genderF;
    NumberPicker yearPicker;
    NumberPicker monthPicker;
    NumberPicker dayPicker;
    int selectedYear;
    int selectedMonth;
    int selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_avatar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        context = getApplicationContext();
        button = findViewById(R.id.button);
        editText = findViewById(R.id.nickname);
        radGender = findViewById(R.id.radGender);
        genderM = findViewById(R.id.genderM);
        genderF = findViewById(R.id.genderF);
        yearPicker = findViewById(R.id.yearPicker);
        monthPicker = findViewById(R.id.monthPicker);
        dayPicker = findViewById(R.id.dayPicker);

        yearPicker.setMinValue(1950);
        yearPicker.setMaxValue(2020);
        yearPicker.setValue(2000);
        yearPicker.setWrapSelectorWheel(true);
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> selectedYear = newVal);
        selectedYear = yearPicker.getValue();

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(1);
        monthPicker.setWrapSelectorWheel(true);
        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> selectedMonth = newVal);
        selectedMonth = monthPicker.getValue();

        updateDaysInMonth(selectedYear, selectedMonth);
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setValue(1);
        dayPicker.setWrapSelectorWheel(true);
        dayPicker.setOnValueChangedListener((picker, oldVal, newVal) -> selectedDay = newVal);
        selectedDay = dayPicker.getValue();

        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedMonth = newVal;
            updateDaysInMonth(selectedYear, selectedMonth);
            int maxDay = getDaysInMonth(selectedYear, selectedMonth);
            if (dayPicker.getValue() > maxDay) {
                dayPicker.setValue(maxDay);
                selectedDay = maxDay;
            }
        });

        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedYear = newVal;
            updateDaysInMonth(selectedYear, selectedMonth);
            int maxDay = getDaysInMonth(selectedYear, selectedMonth);
            if (dayPicker.getValue() > maxDay) {
                dayPicker.setValue(maxDay);
                selectedDay = maxDay;
            }
        });

        button.setOnClickListener(view -> {
            String input = editText.getText().toString().trim();
            int genderId = radGender.getCheckedRadioButtonId();
            String gender;

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(context, "Please enter your nickname", Toast.LENGTH_SHORT).show();
                return;
            }

            if (genderM.getId() == genderId) {
                gender = "Male";
            } else if (genderF.getId() == genderId) {
                gender = "Female";
            } else {
                Toast.makeText(context, "Please select your gender", Toast.LENGTH_SHORT).show();
                return;
            }

            String birthDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay);

            final String finalInput = input;
            final String finalGender = gender;
            final String finalBirthDate = birthDate;

            new android.app.AlertDialog.Builder(AvatarActivity.this)
                    .setTitle("입력 확인")
                    .setMessage("닉네임: " + finalInput + "\n성별: " + finalGender + "\n생년월일: " + finalBirthDate + "\n\n입력하신 내용이 맞습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("avatar_pref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isAvatarCompleted", true);
                        editor.putString("nickname", finalInput);
                        editor.putString("gender", finalGender);
                        editor.putString("birthDate", finalBirthDate);
                        editor.apply();

                        Intent intent = new Intent(AvatarActivity.this, MainActivity.class);
                        intent.putExtra("nickname", finalInput);
                        intent.putExtra("gender", finalGender);
                        intent.putExtra("birthDate", finalBirthDate);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });
    }

    private int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void updateDaysInMonth(int year, int month) {
        int daysInMonth = getDaysInMonth(year, month);
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(daysInMonth);
    }
}
