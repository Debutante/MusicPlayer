package com.example.qingjiaxu.musicplayerlearn1;

import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DisplayAdapter extends RecyclerView.Adapter<DisplayAdapter.ViewHolder>{
    private ArrayList<String> mList1;
    private ArrayList<String> mList2;

    private ItemOnClick mItemOnClick;
    private ItemOnDelete mItemOnDelete;

    interface ItemOnClick {
        void testOnClick(int position);
    }
    interface ItemOnDelete {
        void testOnDelete(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv1;
        TextView tv2;
        ImageButton ib;
        View recyclerView;

        public ViewHolder(View view) {
            super(view);
            recyclerView = view;
            tv1 = view.findViewById(R.id.item_tv1);
            tv2 = view.findViewById(R.id.item_tv2);
            ib = view.findViewById(R.id.item_ib);
        }
    }

    public DisplayAdapter(ArrayList<String> list1, ArrayList<String> list2) {
        mList1 = list1;
        mList2 = list2;
    }

    public void removeData(int position) {
        mList1.remove(position);
        mList2.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    @Override
    public DisplayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.recyclerView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //holder.tv1.setTextColor(Color.rgb(65, 105, 225));//49, 130, 172;63, 81, 181
                //holder.tv2.setTextColor(Color.rgb(102, 205, 170));
                mItemOnClick.testOnClick(holder.getAdapterPosition());

            }
        });
        holder.ib.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(), "delete"+holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
                mItemOnDelete.testOnDelete(holder.getAdapterPosition());
                removeData(holder.getAdapterPosition());

            }
        });
        return holder;
    }



    @Override
    public void onBindViewHolder(DisplayAdapter.ViewHolder holder, int position) {
        holder.tv1.setText(mList1.get(position));
        holder.tv2.setText(mList2.get(position));
    }

    @Override
    public int getItemCount() {
        return mList1.size();
    }

    public ItemOnClick getItemOnClick() {
        return mItemOnClick;
    }

    public void setItemOnClick(ItemOnClick mItemOnClick) {
        this.mItemOnClick = mItemOnClick;
    }

    public ItemOnDelete getItemOnDelete() {
        return mItemOnDelete;
    }

    public void setItemOnDelete(ItemOnDelete mItemOnDelete) {
        this.mItemOnDelete = mItemOnDelete;
    }
}
