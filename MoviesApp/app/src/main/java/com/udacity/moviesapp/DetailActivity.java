package com.udacity.moviesapp;

import android.support.v7.app.ActionBar;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import com.udacity.moviesapp.database.AppDatabase;
import com.udacity.moviesapp.database.FavoriteEntry;
import com.udacity.moviesapp.model.GetFavoriteViewModel;
import com.udacity.moviesapp.model.GetFavoriteViewModelFactory;
import com.udacity.moviesapp.model.MovieEndpointInterface;
import com.udacity.moviesapp.model.Review;
import com.udacity.moviesapp.model.ReviewList;
import com.udacity.moviesapp.model.Trailer;
import com.udacity.moviesapp.model.TrailerList;
import com.udacity.moviesapp.utilities.NetworkUtils;

import java.net.HttpURLConnection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.plot_synopsis_tv)TextView mPlotSynopsisTextView;
    @BindView(R.id.user_rating_tv) TextView mUserRatingTextView;
    @BindView(R.id.release_date) TextView mReleaseDateTextView;
    @BindView(R.id.movie_thumb_iv) ImageView mImageMovieTextView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.layout_trailers_list) LinearLayout mLayoutTrailerList;
    @BindView(R.id.layout_review_list) LinearLayout mLayoutReviewList;
    @BindView(R.id.ib_favorite_movie) ImageButton mFavoriteImageButton;

    private boolean isFavoriteSelected = false;
    private int mMovieID;
    private String mOriginalTitle, mPlotSynopsis, mUserRating, mReleaseDate, mImageUrl;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle(R.string.detail_activity);

        Intent intent = getIntent();

        ButterKnife.bind(this);

        mDb = AppDatabase.getInstance(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        if (intent == null) {
            closeOnError();
        }

        if (!(intent.hasExtra("OriginalTitle") && intent.hasExtra("PlotSynopsis")
                && intent.hasExtra("UserRating") && intent.hasExtra("ReleaseDate")
                && intent.hasExtra("ImageUrl") && intent.hasExtra("MovieID"))){
            closeOnError();
        }

        mOriginalTitle = intent.getStringExtra("OriginalTitle");
        mPlotSynopsis = intent.getStringExtra("PlotSynopsis");
        mUserRating = intent.getStringExtra("UserRating");
        mReleaseDate = intent.getStringExtra("ReleaseDate");
        mImageUrl = intent.getStringExtra("ImageUrl");
        final int movieID = intent.getIntExtra("MovieID",0);
        mMovieID = movieID;

        GetFavoriteViewModelFactory factory = new GetFavoriteViewModelFactory(mDb, movieID);

        final GetFavoriteViewModel viewModel
                = ViewModelProviders.of(this, factory).get(GetFavoriteViewModel.class);

        viewModel.getFavorite().observe(this, new Observer<FavoriteEntry>() {
            @Override
            public void onChanged(@Nullable FavoriteEntry favoriteEntry) {
                viewModel.getFavorite().removeObserver(this);
                populateUI(favoriteEntry, movieID);;
            }
        });
    }

    private View getTrailerView(final Trailer trailer) {
        LayoutInflater inflater = LayoutInflater.from(DetailActivity.this);
        View view = inflater.inflate(R.layout.trailer_item, mLayoutTrailerList, false);
        TextView trailerNameTextView = (TextView) view.findViewById(R.id.tv_movie_trailer);
        trailerNameTextView.setText(trailer.getName());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(NetworkUtils.YOUTUBE_BASE_URL + trailer.getKey()));
                startActivity(intent);
            }
        });
        return view;
    }

    private void populateUI(FavoriteEntry favorite, int movieID){

        mToolbar.setTitle(mOriginalTitle);

        mPlotSynopsisTextView.setText(mPlotSynopsis);

        mUserRatingTextView.setText(mUserRating + "/10");

        if (mReleaseDate.contains("-")) {
            mReleaseDate = mReleaseDate.split("-")[0];
        }

        mReleaseDateTextView.setText(mReleaseDate);

        Picasso.with(this)
                .load(NetworkUtils.IMAGE_BASE_URL + NetworkUtils.IMAGE_BASE_SIZE + mImageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(mImageMovieTextView);

        if(favorite != null){
            mFavoriteImageButton.setImageResource(R.drawable.heart_selected);
            isFavoriteSelected = true;
        }else{
            mFavoriteImageButton.setImageResource(R.drawable.heart_unselected);
            isFavoriteSelected = false;
        }

        mFavoriteImageButton.setOnClickListener(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NetworkUtils.MOVIE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MovieEndpointInterface apiService =
                retrofit.create(MovieEndpointInterface.class);

        Call<TrailerList> call;

        call = apiService.getTrailers(movieID, BuildConfig.MOVIE_API_KEY);

        call.clone().enqueue(new Callback<TrailerList>() {
            @Override
            public void onResponse(Call<TrailerList> call, Response<TrailerList> response) {
                int statusCode = response.code();
                TrailerList trailerList = response.body();

                if(statusCode != HttpURLConnection.HTTP_OK){
                    closeOnError();
                }
                if (trailerList == null) {
                    closeOnError();
                    return;
                }

                List<Trailer> trailersData = trailerList.getResults();

                if(trailersData.size() == 0){
                    TextView noTrailerTextView = (TextView) findViewById(R.id.tv_no_trailer_message);
                    noTrailerTextView.setVisibility(View.VISIBLE);
                }

                for(Trailer trailer : trailersData) {
                    View view = getTrailerView(trailer);
                    mLayoutTrailerList.addView(view);
                }
            }

            @Override
            public void onFailure(Call<TrailerList> call, Throwable t) {
                closeOnError();
            }
        });


        Call<ReviewList> callReview;
        callReview = apiService.getReviews(movieID, BuildConfig.MOVIE_API_KEY);

        callReview.clone().enqueue(new Callback<ReviewList>() {
            @Override
            public void onResponse(Call<ReviewList> call, Response<ReviewList> response) {
                int statusCode = response.code();

                if(statusCode != 200){
                    closeOnError();
                }

                ReviewList reviewList = response.body();

                if (reviewList == null) {
                    closeOnError();
                    return;
                }

                List<Review> reviewData = reviewList.getResults();

                if(reviewData.size() == 0){
                    TextView noReviewTextView = (TextView) findViewById(R.id.tv_no_review_message);
                    noReviewTextView.setVisibility(View.VISIBLE);
                }

                for(Review review : reviewData) {
                    View view = getReviewView(review);
                    mLayoutReviewList.addView(view);
                }
            }

            @Override
            public void onFailure(Call<ReviewList> call, Throwable t) {
                closeOnError();
            }
        });
    }

    private View getReviewView(final Review review) {
        LayoutInflater inflater = LayoutInflater.from(DetailActivity.this);
        View view = inflater.inflate(R.layout.review_item, mLayoutReviewList, false);

        TextView reviewAuthorTextView = (TextView) view.findViewById(R.id.tv_review_author);
        reviewAuthorTextView.setText(review.getAuthor());

        TextView reviewDescriptionTextView = (TextView) view.findViewById(R.id.tv_review_description);
        reviewDescriptionTextView.setText(review.getContent());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(review.getUrl()));
                startActivity(intent);
            }
        });
        return view;
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ib_favorite_movie:

            final FavoriteEntry favorite = new FavoriteEntry(mMovieID, mOriginalTitle,
                                                                mPlotSynopsis, mUserRating,
                                                                mReleaseDate, mImageUrl);

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {

                if(isFavoriteSelected) {
                    mDb.favoriteDao().deleteFavorite(favorite);
                }else {
                    mDb.favoriteDao().insertFavorite(favorite);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isFavoriteSelected) {
                            mFavoriteImageButton.setImageResource(R.drawable.heart_unselected);
                            isFavoriteSelected = false;
                        } else{
                            mFavoriteImageButton.setImageResource(R.drawable.heart_selected);
                            isFavoriteSelected = true;
                        }
                    }
                });
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}