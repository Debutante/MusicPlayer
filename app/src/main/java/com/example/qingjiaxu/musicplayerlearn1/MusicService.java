package com.example.qingjiaxu.musicplayerlearn1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.AbstractList;
import java.util.ArrayList;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    //private int currentPosition;
    private ArrayList<String> songPaths;
    private Boolean mode = false;
    //private MusicOnCompletion musicOnCompletion;

    // 这里设置为public属性，以便activity里面能直接获取
    public MediaPlayer mediaPlayer;
    // 记录当前状态
    private String curState = "";

    // 通过Binder来保持Activity和Service的通信
    public final IBinder binder = new MyBinder();

    public MusicService() {
    }

    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {  // 返回IBinder对象
        // TODO: Return the communication channel to the service.
        Bundle bundle = intent.getExtras();


        songPaths = bundle.getStringArrayList("songPaths");
        Log.d(TAG, "onBind: "+songPaths);
        return binder;
    }

//    interface MusicOnCompletion{
//        void testOnCompletion(int position);
//    }
//
//    public MusicOnCompletion getMusicOnCompletion() {
//        return musicOnCompletion;
//    }
//
//    public void setMusicOnCompletion(MusicOnCompletion musicOnCompletion) {
//        this.musicOnCompletion = musicOnCompletion;
//    }
//
//    public int getCurrentPosition() {
//        return currentPosition;
//    }
//
//    public void setCurrentPosition(int currentPosition) {
//        this.currentPosition = currentPosition;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {  // 停止并释放资源
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public void start(String url) {
        try {
            // 创建mediaPlayer
            //currentPosition = position;
            mediaPlayer.reset();

            mediaPlayer.setDataSource(url);
            getNotificationManager().notify(1, getNotification(url.substring(14, url.indexOf("_")),
                    url.substring(url.indexOf("_") + 1, url.indexOf("."))));
            mediaPlayer.prepare();
            mediaPlayer.start();
            //mediaPlayer = MediaPlayer.create(this, R.raw.beautiful);
            // 通过MediaPlayer.create方法创建，已经初始化，不需要prepare
            mediaPlayer.setLooping(mode);  // 设置循环播放
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchMode(Boolean mode) {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(mode);
            this.mode = mode;
        }
    }

    public void play() {
        if (mediaPlayer.isPlaying()) {  // 当前状态是播放，点击按钮即暂停
            mediaPlayer.pause();
            curState = "暂停";
        } else {
            try {
                if (curState.equals("停止")) {  // 在点击stop之后点击播放
                    mediaPlayer.prepare();      // 这次就需要调用prepare()了
                    mediaPlayer.seekTo(0);      // 跳转到开始处
                }
                mediaPlayer.start();        // 还未开始播放，点击按钮即播放
                curState = "播放";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            curState = "停止";
        }
    }

//    public int delete(int position) {
//        if (currentPosition == position) {
//            mediaPlayer.stop();
//            return 0;
//        }
//        else if (currentPosition > position)
//            return -1;
//        else return 1;
//    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String name, String singer) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        Notification.Builder builder;
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                R.layout.notification_layout);// 获取remoteViews（参数一：包名；参数二：布局资源）
        remoteViews.setTextViewText(R.id.item_tv1, name);
        remoteViews.setTextViewText(R.id.item_tv2, singer);

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= 26)
        {
            //当sdk版本大于26
            String id = "channel_1";
            String description = "143";
            int importance = getNotificationManager().IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(id, description, importance);
//                     channel.enableLights(true);
//                     channel.enableVibration(true);//
            manager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, id)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContent(remoteViews)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            //manager.notify(1, notification);
        }
        else
        {
            //当sdk版本小于26
            builder = new Notification.Builder(this)
                    .setContentTitle("This is content title")
                    .setContentText("This is content text")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher);
            //manager.notify(1,notification);
        }
        return builder.build();
    }


}
