package kr.co.gachon.emotion_diary.ui.myPage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyPageViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MyPageViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is MyPage fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}