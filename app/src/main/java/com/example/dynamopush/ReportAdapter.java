package com.example.dynamopush;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private List<ReportClass> reportList;
    private Context context;

    public ReportAdapter(List<ReportClass> reportList, Context context) {
        this.reportList = reportList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportClass report = reportList.get(position);
        holder.forceCount.setText(report.getForceCount());
        holder.muscleName.setText(report.getMuscleName());

        // Populate LineChart
        setupChart(holder.recycleChart, report.getChartData());
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView forceCount, muscleName;
        LineChart recycleChart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            forceCount = itemView.findViewById(R.id.forceCount);
            muscleName = itemView.findViewById(R.id.muscleName);
            recycleChart = itemView.findViewById(R.id.recycle_chart);
        }
    }

    private void setupChart(LineChart lineChart, List<Float> dataPoints) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataPoints.size(); i++) {
            entries.add(new Entry(i, dataPoints.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Force Data");
        dataSet.setColor(0xFF3CAEF5); // Same color as before
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false); // No dots
        dataSet.setDrawValues(false); // No value labels
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0x803CAEF5);
        dataSet.setFillAlpha(20);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Set up the marker view
        CustomMarkerView markerView = new CustomMarkerView(lineChart.getContext(), R.layout.custom_marker_view);
        lineChart.setMarker(markerView);

        // Remove description text
        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);

        lineChart.invalidate(); // Refresh chart
    }

}
