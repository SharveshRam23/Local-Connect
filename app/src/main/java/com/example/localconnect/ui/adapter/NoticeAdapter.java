package com.example.localconnect.ui.adapter;

import android.media.MediaPlayer;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localconnect.R;
import com.example.localconnect.model.Notice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> notices = new ArrayList<>();
    private OnNoticeActionListener listener;
    private boolean isAdmin = false;
    private MediaPlayer currentMediaPlayer = null;
    private NoticeViewHolder currentPlayingHolder = null;

    public interface OnNoticeActionListener {
        void onEdit(Notice notice);
        void onDelete(Notice notice);
    }

    public void setNotices(List<Notice> notices) {
        if (notices == null) {
            this.notices = new ArrayList<>();
        } else {
            this.notices = notices;
        }
        notifyDataSetChanged();
    }

    public void setOnNoticeActionListener(OnNoticeActionListener listener) {
        this.listener = listener;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    public void releaseMediaPlayer() {
        if (currentMediaPlayer != null) {
            if (currentMediaPlayer.isPlaying()) {
                currentMediaPlayer.stop();
            }
            currentMediaPlayer.release();
            currentMediaPlayer = null;
        }
        if (currentPlayingHolder != null) {
            currentPlayingHolder.resetPlayButton();
            currentPlayingHolder = null;
        }
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = notices.get(position);
        holder.bind(notice, isAdmin, listener, this);
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvTime;
        private final View llAdminActions;
        private final View btnEdit;
        private final View btnDelete;
        private final View llAudioPlayer;
        private final ImageButton btnPlayPause;
        private final TextView tvAudioDuration;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvContent = itemView.findViewById(R.id.tvNoticeContent);
            tvTime = itemView.findViewById(R.id.tvNoticeTime);
            llAdminActions = itemView.findViewById(R.id.llAdminActions);
            btnEdit = itemView.findViewById(R.id.btnEditNotice);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotice);
            llAudioPlayer = itemView.findViewById(R.id.llAudioPlayer);
            btnPlayPause = itemView.findViewById(R.id.btnPlayPause);
            tvAudioDuration = itemView.findViewById(R.id.tvAudioDuration);
        }

        public void bind(Notice notice, boolean isAdmin, OnNoticeActionListener listener, NoticeAdapter adapter) {
            tvTitle.setText(notice.title);
            tvContent.setText(notice.content);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            tvTime.setText(sdf.format(new Date(notice.scheduledTime)));

            // Handle audio player visibility
            if (notice.hasAudio && notice.audioUrl != null && !notice.audioUrl.isEmpty()) {
                llAudioPlayer.setVisibility(View.VISIBLE);
                setupAudioPlayer(notice, adapter);
            } else {
                llAudioPlayer.setVisibility(View.GONE);
            }

            if (isAdmin) {
                llAdminActions.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEdit(notice);
                });
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDelete(notice);
                });
            } else {
                llAdminActions.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> {
                    android.content.Context context = v.getContext();
                    android.content.Intent intent = new android.content.Intent(context, com.example.localconnect.ui.user.NoticeDetailActivity.class);
                    intent.putExtra("notice_id", notice.id);
                    intent.putExtra("notice_title", notice.title);
                    intent.putExtra("notice_content", notice.content);
                    intent.putExtra("notice_time", notice.scheduledTime);
                    intent.putExtra("notice_has_audio", notice.hasAudio);
                    intent.putExtra("notice_audio_url", notice.audioUrl);
                    context.startActivity(intent);
                });
            }
        }

        private void setupAudioPlayer(Notice notice, NoticeAdapter adapter) {
            resetPlayButton();
            
            btnPlayPause.setOnClickListener(v -> {
                if (adapter.currentMediaPlayer != null && adapter.currentPlayingHolder == this) {
                    // Pause current playback
                    if (adapter.currentMediaPlayer.isPlaying()) {
                        adapter.currentMediaPlayer.pause();
                        btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        adapter.currentMediaPlayer.start();
                        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                    }
                } else {
                    // Stop any other playing audio
                    adapter.releaseMediaPlayer();
                    // Start new playback
                    playAudio(notice, adapter);
                }
            });
        }

        private void playAudio(Notice notice, NoticeAdapter adapter) {
            try {
                // Decode Base64 audio
                byte[] audioBytes = Base64.decode(notice.audioUrl, Base64.DEFAULT);
                
                // Create temp file
                File tempFile = File.createTempFile("notice_audio", ".mp3", itemView.getContext().getCacheDir());
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(audioBytes);
                fos.close();

                // Setup MediaPlayer
                adapter.currentMediaPlayer = new MediaPlayer();
                adapter.currentMediaPlayer.setDataSource(tempFile.getAbsolutePath());
                adapter.currentMediaPlayer.prepare();
                
                // Update duration
                int duration = adapter.currentMediaPlayer.getDuration();
                tvAudioDuration.setText(formatDuration(duration));
                
                adapter.currentMediaPlayer.setOnCompletionListener(mp -> {
                    resetPlayButton();
                    adapter.currentMediaPlayer = null;
                    adapter.currentPlayingHolder = null;
                });

                adapter.currentMediaPlayer.start();
                adapter.currentPlayingHolder = this;
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(itemView.getContext(), "Failed to play audio", Toast.LENGTH_SHORT).show();
            }
        }

        public void resetPlayButton() {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }

        private String formatDuration(int milliseconds) {
            int seconds = milliseconds / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
}
