package com.udacity.moviesapp.model;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;


import com.udacity.moviesapp.database.AppDatabase;
import com.udacity.moviesapp.database.FavoriteEntry;

import java.util.List;

// COMPLETED (1) make this class extend AndroidViewModel and implement its default constructor
public class FavoriteViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = FavoriteViewModel.class.getSimpleName();

    // COMPLETED (2) Add a tasks member variable for a list of TaskEntry objects wrapped in a LiveData
    private LiveData<List<FavoriteEntry>> tasks;

    public FavoriteViewModel(Application application) {
        super(application);
        // COMPLETED (4) In the constructor use the loadAllTasks of the taskDao to initialize the tasks variable
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        tasks = database.favoriteDao().loadAllFavorite();
    }

    // COMPLETED (3) Create a getter for the tasks variable
    public LiveData<List<FavoriteEntry>> getFavorites() {
        return tasks;
    }
}
