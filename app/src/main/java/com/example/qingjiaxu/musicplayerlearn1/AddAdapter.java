package com.example.qingjiaxu.musicplayerlearn1;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class AddAdapter extends BaseAdapter {
    private Context context;//上下文
    private ArrayList<String> list1;
    private ArrayList<String> list2;
    //控制CheckBox选中情况
    private static HashMap<Integer,Boolean> isSelected;
    private LayoutInflater inflater=null;//导入布局

    public AddAdapter(Context context, ArrayList<String> list1, ArrayList<String> list2) {
        this.context = context;
        this.list1 = list1;
        this.list2 = list2;
        inflater=LayoutInflater.from(context);
        isSelected=new HashMap<>();
        initData();
    }
    private void initData(){//初始化isSelected的数据
        for(int i=0;i<list1.size();i++){
            getIsSelected().put(i,false);
        }
    }

    @Override
    public int getCount() {
        return list1.size();
    }

    @Override
    public Object getItem(int position) {
        return list1.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    //listview每显示一行数据,该函数就执行一次
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if (convertView==null) {//当第一次加载ListView控件时  convertView为空
            convertView=inflater.inflate(R.layout.add_item, null);//所以当ListView控件没有滑动时都会执行这条语句
            holder=new ViewHolder();
            holder.tv1=convertView.findViewById(R.id.item_tv1);
            holder.tv2=convertView.findViewById(R.id.item_tv2);
            holder.cb=convertView.findViewById(R.id.item_cb);
            convertView.setTag(holder);//为view设置标签

        }
        else{//取出holder
            holder=(ViewHolder) convertView.getTag();//the Object stored in this view as a tag
        }
        //设置list的textview显示
        //holder.tv.setTextColor(Color.WHITE);
        holder.tv1.setText(list1.get(position));
        holder.tv2.setText(list2.get(position));
        holder.tv1.setTextColor(Color.rgb(50, 50, 50));//49, 130, 172
        holder.tv2.setTextColor(Color.rgb(100, 100, 100));
        // 根据isSelected来设置checkbox的选中状况
        holder.cb.setChecked(getIsSelected().get(position));
        return convertView;
    }
    static class ViewHolder {
        TextView tv1;
        TextView tv2;
        CheckBox cb;
    }
    public static HashMap<Integer, Boolean> getIsSelected(){
        return isSelected;
    }
    public static void setIsSelected(HashMap<Integer, Boolean> isSelected){
        AddAdapter.isSelected=isSelected;
    }

}

