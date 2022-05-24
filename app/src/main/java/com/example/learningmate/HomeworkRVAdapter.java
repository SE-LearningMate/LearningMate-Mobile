package com.example.learningmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeworkRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView homework_name_tv;
        TextView year_tv;
        TextView month_tv;
        TextView day_tv;
        TextView score_tv;

        MyViewHolder(View view){
            super(view);
            homework_name_tv = view.findViewById(R.id.homework_name_tv);
            year_tv = view.findViewById(R.id.homework_year_tv);
            month_tv = view.findViewById(R.id.homework_month_tv);
            day_tv = view.findViewById(R.id.homework_day_tv);
            score_tv = view.findViewById(R.id.score_tv);
        }
    }


    private ArrayList<Homework> homeworkArrayList;
    HomeworkRVAdapter(ArrayList<Homework> list) {
        this.homeworkArrayList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_homework,parent,false);
        return new HomeworkRVAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HomeworkRVAdapter.MyViewHolder myViewHolder = (HomeworkRVAdapter.MyViewHolder) holder;

        myViewHolder.homework_name_tv.setText(homeworkArrayList.get(position).homeworkName);
        myViewHolder.year_tv.setText(homeworkArrayList.get(position).year + "");
        myViewHolder.month_tv.setText(homeworkArrayList.get(position).month + "");
        myViewHolder.day_tv.setText(homeworkArrayList.get(position).day+ "");
        myViewHolder.score_tv.setText(homeworkArrayList.get(position).score + "");

    }


    @Override
    public int getItemCount() {
        return homeworkArrayList.size();
    }

}