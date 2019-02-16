package com.udacity.moviesapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.udacity.moviesapp.model.Movie;
import com.udacity.moviesapp.utilities.NetworkUtils;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {
    private List mMoviesData;
    private Context context;

    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);

    }

    public MoviesAdapter(Context context, List mMoviesData, ListItemClickListener listener) {
        this.context = context;
        mOnClickListener = listener;
        this.mMoviesData = mMoviesData;
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;

        public MoviesAdapterViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.iv_movie_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);

        }
    }

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);

        MoviesAdapterViewHolder holder = new MoviesAdapterViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder holder, final int position) {

        Movie movie = (Movie) mMoviesData.get(position);

        String imageUrl = movie.getPosterPath();

        Picasso.with(context)
                .load(NetworkUtils.IMAGE_BASE_URL + NetworkUtils.IMAGE_BASE_SIZE + imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return mMoviesData.size();
    }

    public void setMoviesData(List<Movie> moviesData) {
        mMoviesData = moviesData;

        notifyDataSetChanged();
    }
}