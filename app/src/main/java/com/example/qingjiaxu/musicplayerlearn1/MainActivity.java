package com.example.qingjiaxu.musicplayerlearn1;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DisplayAdapter.ItemOnClick, DisplayAdapter.ItemOnDelete{

    protected MusicService musicService;
    protected Intent intent;
    protected TextView currentTime;
    protected TextView endTime;
    protected SeekBar seekBar;
    protected ImageView imageView;
    protected SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    protected Button previousBtn;
    protected Button playBtn;
    protected Button stopBtn;
    protected Button quitBtn;
    protected Button nextBtn;
    protected Button modeBtn;
    protected Button displayBtn;
    protected Button sensorBtn;
    protected TextView state;
    protected TextView musicPath;
    protected ArrayList<String> songPaths;
    protected RecyclerView recyclerView;
    protected DisplayAdapter adapter;
    protected RecyclerView.LayoutManager layoutManager;
    protected ArrayList<String> list1;
    protected ArrayList<String> list2;
    protected int position = -1;
    protected Boolean loop = false;
    private ShakeUtils mShakeUtils = null;
    private static final String TAG = "MainActivity";


    private ServiceConnection serviceConnection = new ServiceConnection() {
        // bindService成功后回调onServiceConnected函数
        // 通过IBinder获取Service对象,实现Activity与Service的绑定

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MyBinder)service).getService();

        }
        // 解除绑定
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    // 定义handler来更新UI
    private Handler handler = new Handler();
    // 重写Runnable接口的run方法
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (musicService != null && musicService.mediaPlayer != null) {
                // 更新当前播放时间
                currentTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
                // 更新结束时间
                endTime.setText(time.format(musicService.mediaPlayer.getDuration()));
                // 更新拖动条的当前进度
                seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
                // 更新拖动条最大值
                seekBar.setMax(musicService.mediaPlayer.getDuration());
                // 若当前音乐在播放，更新图片的rotation，令其随着音乐播放旋转
                if (musicService.mediaPlayer.isPlaying()) {
                    imageView.setRotation((imageView.getRotation() + 0.2f) % 360);
                }
                // 重复执行该方法，延迟时间为10ms
                handler.postDelayed(this, 10);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_songs:
                seekBar.setEnabled(false);
                handler.removeCallbacks(runnable);
                if (musicService != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            state.setText(R.string.stop);  // 状态变为stop
                            playBtn.setText(R.string.play);  // 按钮文本变为play
                        }
                    });
                    // 调用service的stop方法
                    musicService.stop();
                    stopService(intent);
                    unbindService(serviceConnection);
                }
                Intent intent = new Intent(MainActivity.this, AddSongs.class);
                startActivity(intent);
                finish();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // connection: activity启动时绑定service
//        Intent intent = new Intent(this, MusicService.class);
//        // 通过Context.bindService方法启动service
//        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        // find view
        previousBtn = findViewById(R.id.previous);
        playBtn = findViewById(R.id.play);
        stopBtn = findViewById(R.id.stop);
        nextBtn = findViewById(R.id.next);
        quitBtn = findViewById(R.id.quit);
        modeBtn = findViewById(R.id.mode);
        displayBtn = findViewById(R.id.display);
        sensorBtn = findViewById(R.id.sensor);

        seekBar = findViewById(R.id.seekBar);
        state = findViewById(R.id.state);
        musicPath = findViewById(R.id.musicPath);
        currentTime = findViewById(R.id.currentTime);
        endTime = findViewById(R.id.endTime);
        imageView = findViewById(R.id.imageView);
        seekBar.setEnabled(false);
        mShakeUtils = new ShakeUtils( this );
        
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            //songPaths = bundle.getStringArrayList("songPaths");
            songPaths = bundle.getStringArrayList("songPaths");
//            Log.d(TAG, "onCreate: "+songPaths.toString());

            list1 = new ArrayList<>();
            list2 = new ArrayList<>();//音乐列表

            for (int i = 0; i < songPaths.size(); i++) {
                //list.add(file.getAbsolutePath().substring(14, file.getAbsolutePath().length()));
                //list1.add(songPaths.get(i).substring(14, songPaths.get(i).indexOf("_")));//获取文件的绝对路径
                list1.add(songPaths.get(i).substring(33, songPaths.get(i).indexOf("_")));//获取文件的绝对路径
                list2.add(songPaths.get(i).substring(songPaths.get(i).indexOf("_") + 1, songPaths.get(i).indexOf(".")));
            }

            adapter = new DisplayAdapter(list1, list2);

            recyclerView = findViewById(R.id.recyclerView);
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(adapter);
            adapter.setItemOnClick(this);
            adapter.setItemOnDelete(this);

            imageView.setVisibility(View.INVISIBLE);

            intent = new Intent(this, MusicService.class);
            bundle = new Bundle();
            bundle.putStringArrayList("songPaths", songPaths);
            intent.putExtras(bundle);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        }
//        Intent intent = new Intent(this, MusicService.class);
//        // 通过Context.bindService方法启动service
//        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        // 点击上一首按钮
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousSong(position);
            }
        });

        //点击播放按钮
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songPaths.size() > 0) {
                    seekBar.setEnabled(true);
                    // 调用postDelayed方法更新UI
                    handler.postDelayed(runnable, 10);
                    // 获取点击按钮的文本
                    String text = playBtn.getText().toString();
                    if (text.equals("播放")) {  // 按钮显示play表明未播放
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                state.setText(R.string.play);  // 状态变为Playing
                                playBtn.setText(R.string.pause);  // 按钮文本由play变pause
                            }
                        });
                    } else if (text.equals("暂停")) {  // 按钮显示pause表明正在播放
                        handler.removeCallbacks(runnable);  // 暂停时不更新进度条和图片变化的UI
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                state.setText(R.string.pause);   // 状态变为pause
                                playBtn.setText(R.string.play);  // 按钮文本由pause变为play
                            }
                        });
                    }
                    // 调用service的play方法
                    if (musicService != null) {
                        //Toast.makeText(musicService, ""+position, Toast.LENGTH_SHORT).show();
                        musicService.play();
                    } else{
                        Toast.makeText(musicService, "重启服务", Toast.LENGTH_SHORT).show();
                    }
                }
                else Toast.makeText(musicService, "列表中还没有歌曲哦，快去添加吧", Toast.LENGTH_SHORT).show();
            }
        });

        // 点击停止按钮
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                if (musicService != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            state.setText(R.string.stop);  // 状态变为stop
                            playBtn.setText(R.string.play);  // 按钮文本变为play
                        }
                    });
                    // 调用service的stop方法
                    musicService.stop();
                }
            }
        });

        // 点击下一首按钮
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong(position);
            }
        });

        // 点击退出按钮
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 停止服务，解除绑定
                handler.removeCallbacks(runnable);
                unbindService(serviceConnection);
                try {
                    MainActivity.this.finish();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //点击模式选择按钮
        modeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String text = modeBtn.getText().toString();
                if (text.equals("顺序播放")) {
                    musicService.switchMode(false);
                    modeBtn.setText("列表循环");
                    loop = true;
                }
                else if (text.equals("列表循环")) {
                    musicService.switchMode(true);
                    modeBtn.setText("单曲循环");
                }
                else if (text.equals("单曲循环")) {
                    musicService.switchMode(false);
                    modeBtn.setText("顺序播放");
                    loop = false;
                }
            }
        });

        displayBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String text = displayBtn.getText().toString();
                if (text.equals("听歌模式")) {
                    imageView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                    displayBtn.setText("列表模式");
                }
                else if (text.equals("列表模式")) {
                    imageView.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    displayBtn.setText("听歌模式");
                }
            }
        });

        sensorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = sensorBtn.getText().toString();
                if (text.equals("开摇一摇")) {
                    mShakeUtils.setOnShakeListener(new ShakeUtils.OnShakeListener() {
                        @Override
                        public void onShake() {
                            randomSong();
                        }
                    });
                    sensorBtn.setText("关摇一摇");
                }
                else if (text.equals("关摇一摇")) {
                    sensorBtn.setText("开摇一摇");
                    mShakeUtils.setOnShakeListener(new ShakeUtils.OnShakeListener() {
                        @Override
                        public void onShake() {
                            Toast.makeText(MainActivity.this,"快去开启摇一摇吧",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        // 设置拖动条的监听器
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {   // 判断是否来自用户
                    currentTime.setText(time.format(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                musicService.mediaPlayer.seekTo(progress);
            }
        });

    }

    public void previousSong(int position) {
        if (position - 1 >= 0 || loop) {
            int previousIndex = (songPaths.size() + position - 1) % songPaths.size();
            this.position = previousIndex;
            seekBar.setEnabled(true);
            // 调用postDelayed方法更新UI
            handler.postDelayed(runnable, 10);
            playBtn.setText(R.string.pause);
            musicService.start(songPaths.get(previousIndex));
            //musicPath.setText(songPaths.get(previousIndex).substring(14));
            musicPath.setText(songPaths.get(previousIndex).substring(33));
            changeColor(position, previousIndex);
            musicService.mediaPlayer.setOnCompletionListener(new CompletionListener());
//            Log.e(TAG, "previousSong: " + previousIndex);
        }
        else {
            seekBar.setEnabled(false);
            Toast.makeText(musicService, "没有上一首歌曲了", Toast.LENGTH_SHORT).show();
            musicPath.setText("musicPath");
            musicService.stop();
        }

    }

    public void nextSong(int position) {
        if (position + 1 < songPaths.size() || loop) {
            int nextIndex = (position + 1) % songPaths.size();
            this.position = nextIndex;
            seekBar.setEnabled(true);
            // 调用postDelayed方法更新UI
            handler.postDelayed(runnable, 10);
            playBtn.setText(R.string.pause);
            Log.e(TAG, "nextSong: "+nextIndex);
            musicService.start(songPaths.get(nextIndex));
            changeColor(position, nextIndex);
            //musicPath.setText(songPaths.get(nextIndex).substring(14));
            musicPath.setText(songPaths.get(nextIndex).substring(33));
            musicService.mediaPlayer.setOnCompletionListener(new CompletionListener());

        }
        else {
            seekBar.setEnabled(false);
            Toast.makeText(musicService, "没有下一首歌曲了", Toast.LENGTH_SHORT).show();
            musicPath.setText("musicPath");
            musicService.stop();
        }
    }

    public void randomSong() {
        int index = (int)(Math.random() * songPaths.size());
        seekBar.setEnabled(true);
        // 调用postDelayed方法更新UI
        handler.postDelayed(runnable, 10);
        playBtn.setText(R.string.pause);
//        Log.e(TAG, "randomSong: "+index);
        musicService.start(songPaths.get(index));
        changeColor(this.position, index);
        this.position = index;
        //musicPath.setText(songPaths.get(nextIndex).substring(14));
        musicPath.setText(songPaths.get(index).substring(33));
        Toast.makeText(this, "已为您切换到\n" + songPaths.get(index).substring(33), Toast.LENGTH_SHORT).show();
    }

    public void changeColor(int former, int latter) {
        View view;
        LinearLayout layout;
        TextView tv1;
        TextView tv2;

        if (former != -1) {
            view = layoutManager.findViewByPosition(former);
            //if (view != null) {
                layout = (LinearLayout)view;
                tv1 = layout.findViewById(R.id.item_tv1);
                tv2 = layout.findViewById(R.id.item_tv2);
                tv1.setTextColor(Color.rgb(0, 0, 0));
                tv2.setTextColor(Color.rgb(156, 156, 156));
            //}
        }

        view = layoutManager.findViewByPosition(latter);
        //if (view != null) {
            layout = (LinearLayout)view;
            tv1 = layout.findViewById(R.id.item_tv1);
            tv2 = layout.findViewById(R.id.item_tv2);
            tv1.setTextColor(Color.rgb(65, 105, 225));
            tv2.setTextColor(Color.rgb(102, 205, 170));
        //}
    }


    @Override
    public void testOnClick(int position) {

        seekBar.setEnabled(true);
        // 调用postDelayed方法更新UI
        handler.postDelayed(runnable, 10);
        state.setText(R.string.play);
        playBtn.setText(R.string.pause);
        musicService.start(songPaths.get(position));
        //musicPath.setText(songPaths.get(position).substring(14));
        musicPath.setText(songPaths.get(position).substring(33));
        changeColor(this.position, position);
        musicService.mediaPlayer.setOnCompletionListener(new CompletionListener());
        this.position = position;
    }

    private final class CompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            nextSong(position);
            Log.e(TAG, "onCompletion: "+position);
        }
    }

    @Override
    public void testOnDelete(int position) {

        songPaths.remove(position);

//        if (musicService.delete(position) == 0) {
//            this.position = position;
//            nextSong(this.position, "delete");
//        }
//        else if (musicService.delete(position) == -1) {
//            this.position = position;
//        }
        if (this.position == position) {
            musicService.stop();

            if (position < songPaths.size()) {
                musicService.start(songPaths.get(position));
                changeColor(position, position);
                //musicPath.setText(songPaths.get(position).substring(14));
                musicPath.setText(songPaths.get(position).substring(33));
            }

            else if (loop == true && songPaths.get(0) != null) {
                if (songPaths.get(0) != null) {
                    musicService.start(songPaths.get(0));
                    changeColor(0, 0);
                    musicPath.setText(songPaths.get(0).substring(14));
                }
                else Toast.makeText(musicService, "没有歌曲了", Toast.LENGTH_SHORT).show();
                musicPath.setText("musicPath");
            }

            else {
                Toast.makeText(musicService, "没有下一首歌曲了", Toast.LENGTH_SHORT).show();
                this.position = -1;
            }

        }
        else if (this.position > position) {
            this.position -= 1;
        }

//        Log.e(TAG, "testOnDelete: " + this.position);
    }

//    @Override
//    public void testOnCompletion(int position) {
//        nextSong(position, "complete");
//    }

    @Override
    protected void onResume() {
        super.onResume();
        mShakeUtils.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mShakeUtils.onPause( );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}
