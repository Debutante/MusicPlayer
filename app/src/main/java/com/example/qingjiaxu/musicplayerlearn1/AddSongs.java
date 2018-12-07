package com.example.qingjiaxu.musicplayerlearn1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AddSongs extends AppCompatActivity {

    private AddAdapter adapter;
    private ArrayList<String> list1;
    private ArrayList<String> list2;
    private File[] songFiles;
    private ArrayList<String> songPaths;
    private static final String TAG = "AddSongs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_list);
        Toast.makeText(this, "本地歌曲列表", Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(AddSongs.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddSongs.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            return;
        }

        //判断是否是AndroidN以及更高的版本 N=24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();//音乐列表
        songPaths = new ArrayList<>();

        String sdpath = "sdcard/Music"; //获得手机SD卡路径
        File path = new File(sdpath);      //获得SD卡的mp3文件夹
        //返回以.mp3结尾的文件 (自定义文件过滤)
        songFiles = path.listFiles(new MusicFilter(".mp3"));
        for (File file : songFiles) {
            //list.add(file.getAbsolutePath().substring(14, file.getAbsolutePath().length()));
            list1.add(file.getAbsolutePath().substring(14, file.getAbsolutePath().indexOf("_")));//获取文件的绝对路径
            list2.add(file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("_") + 1, file.getAbsolutePath().indexOf(".")));
        }

        adapter = new AddAdapter(this, list1, list2);

        ListView listView = findViewById(R.id.listView);

        listView.setAdapter(adapter);

        AdapterView.OnItemClickListener listItemClickListener=new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //// 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
                songPaths.clear();
                AddAdapter.ViewHolder viewHolder = (AddAdapter.ViewHolder) view.getTag();
                viewHolder.cb.toggle();// 把CheckBox的选中状态改为当前状态的反,gridview确保是单一选中
                AddAdapter.getIsSelected().put(position, viewHolder.cb.isChecked());//将CheckBox的选中状况记录下来
                Log.d(TAG, "onItemClick: "+AddAdapter.getIsSelected().toString());
                Iterator iterator = AddAdapter.getIsSelected().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry)iterator.next();
                    if ((boolean)entry.getValue() == true)
                        songPaths.add(songFiles[(int)entry.getKey()].getAbsolutePath());
                }
                Log.d(TAG, "onCreate: "+songPaths.toString());
            }
        };
        listView.setOnItemClickListener(listItemClickListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AddSongs.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("songPaths", songPaths);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
