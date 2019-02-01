package com.udacity.moviesapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
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

import com.udacity.moviesapp.model.Movie;
import com.udacity.moviesapp.utilities.NetworkUtils;
import com.udacity.moviesapp.utilities.OpenMoviesJsonUtils;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.ListItemClickListener {

    private RecyclerView mRecyclerView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    private MoviesAdapter mMoviesAdapter;
    private ArrayList mMoviesData;

    private final static int PAGE_LIMIT = 3;
    private final static String ORDER_MOST_POPULAR = "popular";
    private final static String ORDER_TOP_RATED = "top_rated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(R.string.main_activity);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mMoviesData = new ArrayList<Movie>();
        loadMoviesData(ORDER_MOST_POPULAR);

        mMoviesAdapter = new MoviesAdapter(MainActivity.this, mMoviesData, this );
        mRecyclerView.setAdapter(mMoviesAdapter);

    }

    public void loadMoviesData(String orderType) {
        showMoviesDataView();
        new FetchMoviesTask().execute(orderType);
    }

    public void showMoviesDataView() {

        mErrorMessageDisplay.setVisibility(View.INVISIBLE);

        mRecyclerView.setVisibility(View.VISIBLE);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String orderType = params[0];

            try {

                ArrayList<Movie> allJsonMovieData = new ArrayList<>();
                URL moviesRequestUrl;
                ArrayList<Movie> simpleJsonMovieData;
                String jsonWeatherResponse;

                for(int page = 1; page <=  PAGE_LIMIT; page++){
                    moviesRequestUrl =  NetworkUtils.buildUrl(orderType, String.valueOf(page));

                    jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(moviesRequestUrl);

                    simpleJsonMovieData = OpenMoviesJsonUtils
                            .getSimpleMovieListFromJson(jsonWeatherResponse);

                    allJsonMovieData.addAll(simpleJsonMovieData);
                }

                return allJsonMovieData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> moviesData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if (moviesData == null) {
                showErrorMessage();
                return;
            }

            populateUI(moviesData);
            showMoviesDataView();
        }
    }

    public void populateUI(ArrayList<Movie> moviesData){

        mMoviesData.clear();
        mMoviesData = moviesData;

        mMoviesAdapter.setMoviesData(mMoviesData);
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

        if (id == R.id.action_most_popular) {
            loadMoviesData(ORDER_MOST_POPULAR);
            return true;
        }
        if(id == R.id.action_top_rated){
            loadMoviesData(ORDER_TOP_RATED);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {

        Movie movie = (Movie) mMoviesData.get(clickedItemIndex);

        String originalTitle = movie.getOriginalTitle();
        String plotSynopsis = movie.getPlot();
        String userRating = movie.getUserRating();
        String releaseDate = movie.getReleaseDate();
        String imageUrl = movie.getPosterPath();

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("OriginalTitle", originalTitle);
        intent.putExtra("PlotSynopsis", plotSynopsis);
        intent.putExtra("UserRating", userRating);
        intent.putExtra("ReleaseDate", releaseDate);
        intent.putExtra("ImageUrl", imageUrl);

        this.startActivity(intent);
    }
}