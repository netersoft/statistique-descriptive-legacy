package com.neteru.tixtat.classes.adapters;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.neteru.tixtat.R;
import com.neteru.tixtat.classes.models.Backups;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ark Noam on 21/11/2018.
 */
public class BackupsAdapter extends RecyclerView.Adapter<BackupsAdapter.MyViewHolder> {
    private int rowLayout;
    private Context context;
    private List<Backups> backupsList;
    private Animation slideUp, slideDown;
    private BackupsAdapterListener listener;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView date, resolution, name;
        RelativeLayout top;
        FrameLayout delete;
        LinearLayout bottom;
        BarChart chart;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            date = view.findViewById(R.id.date);
            resolution = view.findViewById(R.id.resolution);
            top = view.findViewById(R.id.top);
            bottom = view.findViewById(R.id.bottom);
            delete = view.findViewById(R.id.top_delete_frame);
            chart = view.findViewById(R.id.chart);
        }

    }

    public BackupsAdapter(List<Backups> sauvegardes, int row, Context ctx, BackupsAdapterListener l){
        context = ctx;
        rowLayout = row;
        backupsList = sauvegardes;
        listener = l;

        slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final Backups backups = backupsList.get(position);

        holder.name.setText(backups.getName());
        holder.date.setText(backups.getDate());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            holder.resolution.setText(Html.fromHtml(backups.getResolution(), Html.FROM_HTML_MODE_COMPACT));
        }else{
            holder.resolution.setText(Html.fromHtml(backups.getResolution()));
        }

        List<BarEntry> barEntries = new ArrayList<>();
        String[] xi = backups.getXi().split("_"),
                 ni = backups.getNi().split("_");
        
        for (int i = 0; i < xi.length; i++){
            barEntries.add(new BarEntry(Float.valueOf(xi[i]), Float.valueOf(ni[i])));
        }
        
        holder.chart.setTouchEnabled(true);
        holder.chart.setDragEnabled(true);
        holder.chart.setScaleEnabled(true);
        holder.chart.animateY(2000);
        BarDataSet barDataSet = new BarDataSet(barEntries, context.getString(R.string.v_bar_chart));
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(barDataSet);
        holder.chart.setData(barData);
        holder.chart.getDescription().setText(context.getString(R.string.distrib_var_d));
        holder.chart.invalidate();

        holder.top.setOnClickListener(view -> {
            if (holder.bottom.getVisibility() == View.VISIBLE){

                holder.bottom.setVisibility(View.GONE);
                holder.bottom.startAnimation(slideDown);

            }else {

                holder.bottom.setVisibility(View.VISIBLE);
                holder.bottom.startAnimation(slideUp);

            }
        });

        holder.delete.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle(R.string.deletion_title)
                .setIcon(R.mipmap.ds_launcher)
                .setMessage(R.string.deletion_msg)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> listener.onItemDeleted(backups))
                .show());
    }

    @Override
    public int getItemCount() {
        return backupsList.size();
    }

    public interface BackupsAdapterListener{
        void onItemDeleted(Backups backups);
    }
}

