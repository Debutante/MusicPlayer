package com.example.qingjiaxu.musicplayerlearn1;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordActivity extends AppCompatActivity {

    private MediaRecorder myAudioRecorder;
    private MediaPlayer myMediaPlayer;
    private String oldFilePath = null;
    private String newFilePath = null;
    private TextView songName;
    private Button start,stop,play,pause;
    SimpleDateFormat df = new SimpleDateFormat("MM月dd日HH时mm分ss秒");
    private boolean accompanied = false;
    private int index = 0;
    protected ArrayList<String> songPaths;
    private static final String TAG = "RecordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        songName = findViewById(R.id.songText);

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            //songPaths = bundle.getStringArrayList("songPaths");
            songPaths = bundle.getStringArrayList("songPaths");

            if (songPaths.size() > 0) {
                accompanied = true;
                myMediaPlayer = new MediaPlayer();
                songName.setText(songPaths.get(index).substring(33));
            }
            Log.e(TAG, "onCreate: " + accompanied);
        }

        start = findViewById(R.id.start_button);
        stop = findViewById(R.id.stop_button);
        play = findViewById(R.id.play_button);
        pause = findViewById(R.id.pause_button);

        stop.setEnabled(false);
        play.setEnabled(false);
        pause.setEnabled(false);

        requestAllPower();
        myAudioRecorder = new MediaRecorder();

    }

    private void prepareRecorder(){
        oldFilePath = "/storage/emulated/0/xiami/audios/" + df.format(new Date()) + "_许清嘉.mp3";
        newFilePath = oldFilePath;

        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        myAudioRecorder.setOutputFile(oldFilePath);
        myAudioRecorder.setAudioChannels(1);
        myAudioRecorder.setAudioSamplingRate(44100);
        myAudioRecorder.setAudioEncodingBitRate(192000);

        Log.e(TAG, "prepareRecorder: ");

    }

    private void preparePlayer(){
        try {
            songName.setText(songPaths.get(index).substring(33));
            myMediaPlayer.setDataSource(songPaths.get(index));
            myMediaPlayer.prepare();
            myMediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(this, "歌曲不可用", Toast.LENGTH_SHORT).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void stopPlayer() {
        myMediaPlayer.stop();
        myMediaPlayer.reset();
    }

    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }


    public void start(View view){
        try {
            prepareRecorder();

            if (accompanied == true){
                preparePlayer();
                Log.e(TAG, "start: " + accompanied);
            }
            else {
                songName.setText("录我的歌");
            }
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        start.setEnabled(false);
        pause.setEnabled(true);
        stop.setEnabled(true);
        play.setEnabled(false);
        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();

    }

    public void pause(View view){
        if (((Button)view.findViewById(R.id.pause_button)).getText().toString().equals("暂停")) {
            if (accompanied == true) {
                myMediaPlayer.pause();
            }
            myAudioRecorder.pause();
            ((Button)view.findViewById(R.id.pause_button)).setText("继续");
        }
        else if (((Button)view.findViewById(R.id.pause_button)).getText().toString().equals("继续")){
            if (accompanied == true) {
                myMediaPlayer.start();
            }

            myAudioRecorder.resume();
            ((Button)view.findViewById(R.id.pause_button)).setText("暂停");
        }
    }

    public void stop(View view){
        myAudioRecorder.stop();
        myAudioRecorder.reset();
        stop.setEnabled(false);
        start.setEnabled(true);
        play.setEnabled(true);
        pause.setEnabled(false);
        if (accompanied) {
            stopPlayer();
            index += 1;
            if (index >= songPaths.size())
                accompanied = false;
        }

        Toast.makeText(getApplicationContext(), "Audio recorded successfully",
                Toast.LENGTH_LONG).show();
        queryDialog();
    }

    public void play(View view) throws IllegalArgumentException,
            SecurityException, IllegalStateException, IOException{

        MediaPlayer m = new MediaPlayer();
        m.setDataSource(newFilePath);
        m.prepare();
        m.start();
        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
    }

    private void queryDialog() {
        final LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.query_layout, null);
        new AlertDialog.Builder(this)
                .setTitle("为歌曲重新取个好听的名字叭")
                .setView(linearLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String txtRename=((EditText)linearLayout.findViewById(R.id.rename_text)).getText().toString();
                        File oldFile = new File(oldFilePath);
                        newFilePath = "/storage/emulated/0/xiami/audios/" + txtRename + "_许清嘉.mp3";
                        File newFile = new File(newFilePath);
                        oldFile.renameTo(newFile);

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;
        Intent intent = new Intent(RecordActivity.this, AddSongs.class);
        startActivity(intent);
        finish();
    }
}
