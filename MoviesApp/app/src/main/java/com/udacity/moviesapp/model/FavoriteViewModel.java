package com.udacity.moviesapp.model;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;


import com.udacity.moviesapp.database.AppDatabase;
import com.udacity.moviesapp.database.FavoriteEntry;

import java.util.List;

public class FavoriteViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = FavoriteViewModel.class.getSimpleName();

    private LiveData<List<FavoriteEntry>> tasks;

    public FavoriteViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        tasks = database.favoriteDao().loadAllFavorite();
    }

    public LiveData<List<FavoriteEntry>> getFavorites() {
        return tasks;
    }
}
