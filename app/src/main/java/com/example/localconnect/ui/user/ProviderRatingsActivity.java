package com.example.localconnect.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.data.AppDatabase;
import com.example.localconnect.model.Rating;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProviderRatingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RatingsAdapter adapter;
    private TextView tvEmpty;
    private String providerId;
    private String providerName;

    @javax.inject.Inject
    com.google.firebase.firestore.FirebaseFirestore firestore;

    @javax.inject.Inject
    com.example.localconnect.data.dao.RatingDao ratingDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_ratings);

        providerId = getIntent().getStringExtra("provider_id");
        providerName = getIntent().getStringExtra("provider_name");

        TextView tvTitle = findViewById(R.id.tvRatingTitle);
        tvTitle.setText("Reviews for " + (providerName != null ? providerName : "Provider"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rvRatings);
        tvEmpty = findViewById(R.id.tvEmptyRatings);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RatingsAdapter();
        recyclerView.setAdapter(adapter);

        loadRatings();
    }

    private void loadRatings() {
        firestore.collection("ratings")
                .whereEqualTo("providerId", providerId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Rating> ratings = queryDocumentSnapshots.toObjects(Rating.class);
                    if (!ratings.isEmpty()) {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRatings(ratings);
                        // Sync to Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Rating r : ratings) ratingDao.insert(r);
                        });
                    } else {
                        // Fallback to Room
                        com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<Rating> localRatings = ratingDao.getRatingsForProvider(providerId);
                            runOnUiThread(() -> {
                                if (localRatings == null || localRatings.isEmpty()) {
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    tvEmpty.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    adapter.setRatings(localRatings);
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to Room
                    com.example.localconnect.data.AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<Rating> localRatings = ratingDao.getRatingsForProvider(providerId);
                        runOnUiThread(() -> {
                            if (localRatings != null) {
                                adapter.setRatings(localRatings);
                            }
                        });
                    });
                });
    }

    private static class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.ViewHolder> {
        private List<Rating> ratings = new ArrayList<>();

        void setRatings(List<Rating> ratings) {
            this.ratings = ratings;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rating, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Rating rating = ratings.get(position);
            holder.tvUser.setText(rating.userName != null ? rating.userName : "Anonymous");
            holder.ratingBar.setRating(rating.rating);
            holder.tvReview.setText(rating.review);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(rating.timestamp)));
        }

        @Override
        public int getItemCount() {
            return ratings.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvUser, tvReview, tvDate;
            RatingBar ratingBar;

            ViewHolder(View itemView) {
                super(itemView);
                tvUser = itemView.findViewById(R.id.tvReviewUser);
                tvReview = itemView.findViewById(R.id.tvReviewText);
                tvDate = itemView.findViewById(R.id.tvReviewDate);
                ratingBar = itemView.findViewById(R.id.rbReviewStars);
            }
        }
    }
}
