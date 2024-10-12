package com.example.musicplayer2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class activity_playsong extends AppCompatActivity {
    TextView song_name, startDuration, endDuration;
    ImageButton prev, play, next;
    String songname;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    ArrayList<String> songPaths;  // Changed to store song paths
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playsong);

        // Initialize UI elements
        song_name = findViewById(R.id.song_name);
        prev = findViewById(R.id.prev);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        startDuration = findViewById(R.id.startDuration);
        endDuration = findViewById(R.id.endDuration);
        seekBar = findViewById(R.id.seekBar);

        // Get the song data from the Intent
        Intent intent = getIntent();
        songPaths = intent.getStringArrayListExtra("songPaths");  // Fetch paths instead of File objects
        songname = intent.getStringExtra("currentSong");
        position = intent.getIntExtra("position", 0);

        // Set the song name
        song_name.setText(songname);
        song_name.setSelected(true);

        // Play the selected song
        playSong(position);

        // Set up SeekBar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        // Thread to update the seek bar
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            Message msg = new Message();
                            msg.what = mediaPlayer.getCurrentPosition();
                            handler.sendMessage(msg);
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // Play/Pause button listener
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play.setImageResource(R.drawable.play_foreground);
                } else {
                    mediaPlayer.start();
                    play.setImageResource(R.drawable.pause_foreground);
                }
            }
        });

        // Previous button listener
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPreviousSong();
            }
        });

        // Next button listener
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNextSong();
            }
        });
    }

    // Helper method to play a song based on position
    private void playSong(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Uri uri = Uri.parse(songPaths.get(position));
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        play.setImageResource(R.drawable.pause_foreground);

        // Update seek bar and duration
        seekBar.setMax(mediaPlayer.getDuration());
        String totalTime = createTimeLabel(mediaPlayer.getDuration());
        endDuration.setText(totalTime);

        // Set song name
        songname = new File(songPaths.get(position)).getName().replace(".mp3", "").replace(".mpeg", "");
        song_name.setText(songname);

        // Update media player prepared listener
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();
        });
    }

    // Helper method to play the next song
    private void playNextSong() {
        if (position < songPaths.size() - 1) {
            position++;
        } else {
            position = 0;
        }
        playSong(position);
    }

    // Helper method to play the previous song
    private void playPreviousSong() {
        if (position > 0) {
            position--;
        } else {
            position = songPaths.size() - 1;
        }
        playSong(position);
    }

    // Handler to update the SeekBar progress and start time label
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int currentTime = msg.what;
            seekBar.setProgress(currentTime);
            String currentTimeLabel = createTimeLabel(currentTime);
            startDuration.setText(currentTimeLabel);
        }
    };

    // Create time label for duration
    public String createTimeLabel(int duration) {
        String timeLabel = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        timeLabel += min + ":";
        if (sec < 10) {
            timeLabel += "0";
        }
        timeLabel += sec;
        return timeLabel;
    }

    // Clean up the MediaPlayer when the activity is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
