package com.udacity.moviesapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private TextView mPlotSynopsisTextView;
    private TextView mUserRatingTextView;
    private TextView mReleaseDateTextView;
    private ImageView mImageMovieTextView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle(R.string.detail_activity);

        Intent intent = getIntent();

        if (intent == null) {
            closeOnError();
        }

        if (!(intent.hasExtra("OriginalTitle") && intent.hasExtra("PlotSynopsis")
                && intent.hasExtra("UserRating") && intent.hasExtra("ReleaseDate")
                && intent.hasExtra("ImageUrl"))){
            closeOnError();
        }

        String originalTitle = intent.getStringExtra("OriginalTitle");
        String plotSynopsis = intent.getStringExtra("PlotSynopsis");
        String userRating = intent.getStringExtra("UserRating");
        String releaseDate = intent.getStringExtra("ReleaseDate");
        String imageUrl = intent.getStringExtra("ImageUrl");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(originalTitle);

        mPlotSynopsisTextView = (TextView) findViewById(R.id.plot_synopsis_tv);
        mPlotSynopsisTextView.setText(plotSynopsis);

        mUserRatingTextView = (TextView) findViewById(R.id.user_rating_tv);
        mUserRatingTextView.setText(userRating + "/10");

        mReleaseDateTextView = (TextView) findViewById(R.id.release_date);

        if (releaseDate.contains("-")) {
            releaseDate = releaseDate.split("-")[0];
        }

        mReleaseDateTextView.setText(releaseDate);

        mImageMovieTextView = (ImageView) findViewById(R.id.movie_thumb_iv);

        Picasso.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(mImageMovieTextView);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show();
    }
}
