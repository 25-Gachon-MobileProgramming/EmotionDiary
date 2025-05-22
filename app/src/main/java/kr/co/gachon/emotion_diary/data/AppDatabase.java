package kr.co.gachon.emotion_diary.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters; // Date 타입 변환을 위해 추가

@Database(entities = {Diary.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract DiaryDao diaryDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),

                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration() // schema changed -> recreate db
                            .build();
                }
            }
        }

        return INSTANCE;
    }
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "emotion-diary-db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }

}