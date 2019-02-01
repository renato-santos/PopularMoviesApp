package com.udacity.moviesapp.utilities;

import com.udacity.moviesapp.model.Movie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public final class OpenMoviesJsonUtils {

    public static ArrayList<Movie> getSimpleMovieListFromJson(String moviesJsonStr)
            throws JSONException {

        final String OWM_RESULTS = "results";
        final String BASE_URL =  "http://image.tmdb.org/t/p/";
        final String BASE_IMAGE_SIZE = "w185";

        final String OWM_ORIGINAL_TITLE = "original_title";
        final String OWM_THUMBNAIL = "poster_path";
        final String OWM_PLOT = "overview";
        final String OWM_USER_RATING = "vote_average";
        final String OWM_RELEASE_DATE = "release_date";

        final String OWM_MESSAGE_CODE = "cod";

        Movie movieObj;

        JSONObject forecastJson = new JSONObject(moviesJsonStr);

        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return null;
                default:
                    return null;
            }
        }

        JSONArray moviesArray = forecastJson.getJSONArray(OWM_RESULTS);
        ArrayList<Movie> parsedMoviesData = new ArrayList<>();

        for (int i = 0; i < moviesArray.length(); i++) {
            String originalTitle;
            String posterPath;
            String plot;
            String userRating;
            String releaseDate;

            JSONObject movieJSONObj = moviesArray.getJSONObject(i);

            originalTitle = movieJSONObj.getString(OWM_ORIGINAL_TITLE);
            posterPath = movieJSONObj.getString(OWM_THUMBNAIL);
            plot = movieJSONObj.getString(OWM_PLOT);
            userRating = movieJSONObj.getString(OWM_USER_RATING);
            releaseDate = movieJSONObj.getString(OWM_RELEASE_DATE);

            movieObj = new Movie();

            movieObj.setOriginalTitle(originalTitle);
            movieObj.setPosterPath(BASE_URL + BASE_IMAGE_SIZE + posterPath);
            movieObj.setPlot(plot);
            movieObj.setUserRating(userRating);
            movieObj.setReleaseDate(releaseDate);

            parsedMoviesData.add(movieObj);
        }

        return parsedMoviesData;
    }
}