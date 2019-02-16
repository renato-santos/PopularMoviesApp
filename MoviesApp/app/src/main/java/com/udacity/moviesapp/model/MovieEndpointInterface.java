package com.udacity.moviesapp.model;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieEndpointInterface {
    // Request method and URL specified in the annotation

    @GET("movie/top_rated")
    Call<MovieList> getTopRated(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/popular")
    Call<MovieList> getMostPopular(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/{id}/videos")
    Call<TrailerList> getTrailers(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewList> getReviews(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/details")
    Call<Movie> getDetails(@Path("id") int id, @Query("api_key") String apiKey);
}
