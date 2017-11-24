package com.dmsoft.videoplayerdemo;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, View.OnClickListener,
            SeekBar.OnSeekBarChangeListener{
    SurfaceView surfaceView;
    ImageView backPressed, playVideo;
    TextView videoName;
    MediaPlayer mediaPlayer;
    SurfaceHolder surfaceHolder;

    String demoVideoPath = "/sdcard/Download/batman.mp4";
    String demoVideoName = "batman.mp4";

    SeekBar videoSeekBar;
    TextView startTime, endTime;

    Handler videoHandler;
    Runnable videoRunnable;

    ImageView audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initialize();

        setHandler();
        initializeSeekBar();
    }

    private void initializeSeekBar() {
        videoSeekBar.setProgress(0);
        videoSeekBar.setOnSeekBarChangeListener(this);
    }

    private void initialize() {
        surfaceView = findViewById(R.id.surfaceView);
        backPressed = findViewById(R.id.backPressed);
        playVideo = findViewById(R.id.playVideo);
        videoName = findViewById(R.id.videoName);

        startTime = findViewById(R.id.startTime);
        videoSeekBar = findViewById(R.id.videoSeekBar);
        endTime = findViewById(R.id.endTime);

        audioTrack = findViewById(R.id.audioTrack);

        mediaPlayer = new MediaPlayer();

        surfaceHolder = surfaceView.getHolder();
        videoName.setText(demoVideoName);
        surfaceHolder.addCallback(this);

        surfaceView.setKeepScreenOn(true);//TO Keep screen on while playing video

        playVideo.setOnClickListener(this);
        backPressed.setOnClickListener(this);
        audioTrack.setOnClickListener(this);
    }

    /*
    * Now We will create a handler for startTime, endTime and for seeking the videoSeekBar with MediaPlayer
    * */

    private void setHandler() {
        videoHandler = new Handler();
        videoRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.getDuration() > 0) {
                    int currentVideoDuration = mediaPlayer.getCurrentPosition();
                    videoSeekBar.setProgress(currentVideoDuration);
                    startTime.setText("" + convertIntoTime(currentVideoDuration));
                    endTime.setText("-" + convertIntoTime(mediaPlayer.getDuration() - currentVideoDuration));

                    /*
                    * mediaPlayer.getDuration() = Total Time of Video
                    * currentVideoDuration = currentTime of Video
                    * */
                }

                videoHandler.postDelayed(this, 0);
            }
        };

        /*
        * To Start the handler
        * */

        videoHandler.postDelayed(videoRunnable, 500);
    }

    /*
    * This function can convert the time from int milliSecond/ long milliSecond to String format  like 12:00, 23:00
    * */

    private String convertIntoTime(int ms) {
        String time;
        int x, seconds, minutes, hours;
        x = (int) (ms / 1000);
        seconds = x % 60;
        x /= 60;
        minutes = x % 60;
        x /= 60;
        hours = x % 24;
        if (hours != 0)
            time = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        else time = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        return time;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
        try {
            mediaPlayer.setDataSource(demoVideoPath);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        /*
        * Now to set the max Value of SeekBar
        * */
        videoSeekBar.setMax(mediaPlayer.getDuration());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backPressed:
                onBackPressed();
                break;
            case R.id.playVideo:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playVideo.setImageResource(R.drawable.ic_play_circle);
                } else {
                    mediaPlayer.start();
                    playVideo.setImageResource(R.drawable.ic_pause_circle);
                }
                break;
            case R.id.audioTrack:
                addMultiAudioTrack();
                break;
        }
    }

    private void addMultiAudioTrack() {
        MediaPlayer.TrackInfo trackInfos[] = mediaPlayer.getTrackInfo();
        ArrayList<Integer> audioTracksIndex = new ArrayList<>();

        for (int i = 0; i < trackInfos.length; i++) {
            if (trackInfos[i].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO){
                audioTracksIndex.add(i);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Audio Tracks");

        String values[] = new String[audioTracksIndex.size()];
        for (int i = 0; i < audioTracksIndex.size(); i++) {
            values[i] = String.valueOf("Track " + i);
        }

        /*
        * SingleChoice means RadioGroup
        * */
        builder.setSingleChoiceItems(values, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.selectTrack(which);
                Toast.makeText(MainActivity.this, "Track " + which + " Selected", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            /*
            * Remove Callback from the handler...Important
            * */
            videoHandler.removeCallbacks(videoRunnable);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseMediaPlayer();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.videoSeekBar:
                if (fromUser) {//only if user manually seeks the seekBar....Important
                    mediaPlayer.seekTo(progress);
                    int currentVideoDuration = mediaPlayer.getCurrentPosition();
                    startTime.setText("" + convertIntoTime(currentVideoDuration));
                    endTime.setText("-" + convertIntoTime(mediaPlayer.getDuration() - currentVideoDuration));
                }
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
