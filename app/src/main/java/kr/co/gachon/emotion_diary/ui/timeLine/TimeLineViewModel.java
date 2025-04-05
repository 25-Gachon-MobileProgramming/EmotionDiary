package kr.co.gachon.emotion_diary.ui.timeLine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TimeLineViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TimeLineViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}