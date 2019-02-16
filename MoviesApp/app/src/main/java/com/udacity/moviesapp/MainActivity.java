package com.udacity.moviesapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.moviesapp.database.FavoriteEntry;
import com.udacity.moviesapp.model.FavoriteViewModel;
import com.udacity.moviesapp.model.Movie;
import com.udacity.moviesapp.model.MovieEndpointInterface;
import com.udacity.moviesapp.model.MovieList;
import com.udacity.moviesapp.utilities.NetworkUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.ListItemClickListener {

    @BindView(R.id.recyclerview_movies) RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message_display) TextView mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;
    private MoviesAdapter mMoviesAdapter;
    private List mMoviesData;
    private GridLayoutManager mGridLayoutManager;
    private int mPosition = RecyclerView.NO_POSITION;
    private final String USER_OPTION_KEY = "user_option";
    private final String RECYCLER_POSITION_KEY = "recycler_position";
    private final static int PAGE_LIMIT = 5;
    private final static String ORDER_MOST_POPULAR = "popular";
    private final static String ORDER_TOP_RATED = "top_rated";
    private final static String FAVORITES = "favorites";

    private String userOption;

    ArrayList<Movie> allJsonMovieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.main_activity);

        ButterKnife.bind(this);

        mGridLayoutManager = new GridLayoutManager(getApplicationContext(), getResources().getInteger(R.integer.grid_columns));

        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMoviesData = new ArrayList<Movie>();

        mMoviesAdapter = new MoviesAdapter(MainActivity.this, mMoviesData, this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        if(savedInstanceState != null) {
            userOption = savedInstanceState.getString(USER_OPTION_KEY);
            mPosition = savedInstanceState.getInt(RECYCLER_POSITION_KEY);
        }else{
            userOption = ORDER_MOST_POPULAR;
        }

        loadMoviesData(userOption);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(RECYCLER_POSITION_KEY,  mGridLayoutManager.findFirstVisibleItemPosition());

        outState.putString(USER_OPTION_KEY, userOption);

        Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        super.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(RECYCLER_POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(RECYCLER_POSITION_KEY);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        }

        if(savedInstanceState.containsKey(USER_OPTION_KEY)){
            userOption = savedInstanceState.getString(USER_OPTION_KEY);
        }
    }

    public void loadMoviesData(String orderType) {

        final FavoriteViewModel favoriteViewModel =  ViewModelProviders.of(this).get(FavoriteViewModel.class);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NetworkUtils.MOVIE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MovieEndpointInterface apiService =
                retrofit.create(MovieEndpointInterface.class);

        mLoadingIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        allJsonMovieData = new ArrayList<Movie>();

        if (orderType == ORDER_TOP_RATED || orderType == ORDER_MOST_POPULAR) {

            fetchMovies(orderType, apiService, 1);

        } else {

            favoriteViewModel.getFavorites().observe(this, new Observer<List<FavoriteEntry>>() {
                @Override
                public void onChanged(@Nullable List<FavoriteEntry> taskEntries) {
                    final List<Movie> movieList = new ArrayList<Movie>();

                    for(FavoriteEntry favorite : favoriteViewModel.getFavorites().getValue()){
                        Movie movie = new Movie();
                        movie.setId(favorite.getMovieId());
                        movie.setOriginalTitle(favorite.getOriginalTitle());
                        movie.setOverview(favorite.getPlotSynopsis());
                        movie.setVoteAverage(Double.parseDouble(favorite.getUserRating()));
                        movie.setReleaseDate(favorite.getReleaseDate());
                        movie.setPosterPath(favorite.getImageUrl());
                        movieList.add(movie);
                    }

                    populateUI(movieList);
                    showMoviesDataView();

                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);

                }
            });
        }
    }

    public void fetchMovies(final String orderType, final MovieEndpointInterface apiService, int page){

        Call<MovieList> call;
        if (orderType == ORDER_TOP_RATED) {
            call = apiService.getTopRated(BuildConfig.MOVIE_API_KEY, page);
        } else {
            call = apiService.getMostPopular(BuildConfig.MOVIE_API_KEY, page);
        }

        call.clone().enqueue(new Callback<MovieList>() {
            @Override
            public void onResponse(Call<MovieList> call, Response<MovieList> response) {
                int statusCode = response.code();

                if (statusCode != HttpURLConnection.HTTP_OK) {
                    showErrorMessage();
                }
                MovieList movieList = response.body();

                if (movieList == null) {
                    showErrorMessage();
                    return;
                }

                allJsonMovieData.addAll(movieList.getResults());

                if (movieList.getPage() == PAGE_LIMIT) {
                    populateUI(allJsonMovieData);

                    showMoviesDataView();

                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    fetchMovies(orderType, apiService, movieList.getPage() + 1);
                }
            }

            @Override
            public void onFailure(Call<MovieList> call, Throwable t) {
                // Log error here since request failed
                showErrorMessage();
            }
        });
    }

    public void showMoviesDataView() {

        mErrorMessageDisplay.setVisibility(View.INVISIBLE);

        mRecyclerView.setVisibility(View.VISIBLE);
    }



    public void populateUI(List<Movie> moviesData){

        mMoviesData.clear();
        mMoviesData = moviesData;

        mMoviesAdapter.setMoviesData(mMoviesData);

        mRecyclerView.scrollToPosition(mPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        mPosition = 0;
        mRecyclerView.scrollToPosition(mPosition);

        if (id == R.id.action_most_popular) {
            userOption = ORDER_MOST_POPULAR;
            loadMoviesData(userOption);
            return true;
        }
        if(id == R.id.action_top_rated){
            userOption = ORDER_TOP_RATED;
            loadMoviesData(userOption);
            return true;
        }

        if(id == R.id.action_favorites){
            userOption = FAVORITES;
            loadMoviesData(userOption);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showErrorMessage() {

        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {

        Movie movie = (Movie) mMoviesData.get(clickedItemIndex);

        String originalTitle = movie.getOriginalTitle();
        String plotSynopsis = movie.getOverview();
        String userRating = String.valueOf(movie.getVoteAverage());
        String releaseDate = movie.getReleaseDate();
        String imageUrl = movie.getPosterPath();
        int movieId = movie.getId();

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("OriginalTitle", originalTitle);
        intent.putExtra("PlotSynopsis", plotSynopsis);
        intent.putExtra("UserRating", userRating);
        intent.putExtra("ReleaseDate", releaseDate);
        intent.putExtra("ImageUrl", imageUrl);
        intent.putExtra("MovieID", movieId);

        this.startActivity(intent);
    }
}