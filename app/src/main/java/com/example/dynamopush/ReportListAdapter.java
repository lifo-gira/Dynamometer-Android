package com.example.dynamopush;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ReportViewHolder> {

    private List<ReportListClass> reportList = new ArrayList<>();

    public ReportListAdapter(List<ReportListClass> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_list, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportListClass report = reportList.get(position);
        holder.testCount.setText("TEST " + report.getTestCount());
        holder.repCount.setText(report.getRepCount());
        holder.musclesCovered.setText(report.getMusclesCovered() + "%");

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, Report.class);

            // Pass the entire record (JSONObject)
            intent.putExtra("testCount", report.getTestCount());
            intent.putExtra("repCount", report.getRepCount());
            intent.putExtra("musclesCovered", report.getMusclesCovered());
            intent.putExtra("record", report.getRecord().toString());  // Convert JSONObject to string

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    // Method to update the list dynamically
    public void updateList(List<ReportListClass> newList) {
        reportList.clear();
        reportList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView testCount, repCount, musclesCovered;

        public ReportViewHolder(View itemView) {
            super(itemView);
            testCount = itemView.findViewById(R.id.testCount);
            repCount = itemView.findViewById(R.id.RepCount);
            musclesCovered = itemView.findViewById(R.id.musclesCovered);
        }
    }
}
