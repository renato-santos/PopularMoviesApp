package com.udacity.moviesapp.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.udacity.moviesapp.database.AppDatabase;
import com.udacity.moviesapp.database.FavoriteEntry;

public class GetFavoriteViewModel extends ViewModel {

    private LiveData<FavoriteEntry> favorite;

    public GetFavoriteViewModel(AppDatabase database, int movieId) {
        favorite = database.favoriteDao().loadFavoriteById(movieId);
    }

    public LiveData<FavoriteEntry> getFavorite() {
        return favorite;
    }
}
