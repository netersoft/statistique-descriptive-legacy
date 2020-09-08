package com.neteru.tixtat.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.neteru.tixtat.R;
import com.neteru.tixtat.classes.databases.DB_Manager;
import com.neteru.tixtat.classes.models.Backups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class QualitativeVariablesFragment extends Fragment {

    private Context context;
    private Activity activity;
    private LinearLayout entries_layout;
    private LinearLayout bloc;
    private LinearLayout checkboxes;
    private CheckBox moyenne;
    private CheckBox mode;
    private CheckBox courbe;
    private TextView resolution;
    private TextView statsTitle;
    private TextView show_calculations;
    private ScrollView scrollView;
    private PieChart pieChart;
    private PieChart c_pieChart;
    private BarChart barChart;
    private BarChart c_barChart;
    private TableLayout table;
    private CardView charts;
    private int cursor;
    private int c_cursor;
    private MenuItem saveItem;
    private MenuItem shareItem;
    private String component;
    private String componentForSave;
    private DB_Manager db_manager;
    private StringBuilder xi_join;
    private StringBuilder ni_join;
    private ImageView next;
    private ImageView previous;
    private ImageView c_next;
    private ImageView c_previous;
    private List<String> charts_list;
    private List<String> c_charts_list;
    private Animation fadeIn;
    private Animation fadeOut;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_qualitative_variables, container, false);

        if (getContext() != null){ context = getContext(); }
        if (getActivity() != null){ activity = getActivity(); }

        bloc = root.findViewById(R.id.bloc);
        charts = root.findViewById(R.id.charts);
        scrollView = root.findViewById(R.id.scrollView);
        entries_layout = root.findViewById(R.id.entries_layout);
        resolution = root.findViewById(R.id.resolution);
        table = root.findViewById(R.id.statsTable);
        pieChart = root.findViewById(R.id.pie_chart);
        barChart = root.findViewById(R.id.v_bar_chart);
        c_pieChart = root.findViewById(R.id.c_pie_chart);
        c_barChart = root.findViewById(R.id.c_v_bar_chart);
        moyenne = root.findViewById(R.id.moyenne);
        mode = root.findViewById(R.id.mode);
        courbe = root.findViewById(R.id.courbe);
        statsTitle = root.findViewById(R.id.statsTableTitle);
        next = root.findViewById(R.id.next_1);
        c_next = root.findViewById(R.id.next_2);
        previous = root.findViewById(R.id.previous_1);
        c_previous = root.findViewById(R.id.previous_2);
        show_calculations = root.findViewById(R.id.show_calculations);
        checkboxes = root.findViewById(R.id.checkboxes);
        Button add_entry = root.findViewById(R.id.add_entry),
               calc = root.findViewById(R.id.calculBut);
        component = "";

        db_manager = new DB_Manager(getContext());

        fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);

        bloc.setVisibility(View.GONE);
        charts.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        statsTitle.setVisibility(View.GONE);

        final CheckBox[] checkBoxes = new CheckBox[]{moyenne, mode, courbe};

        CheckBox all = root.findViewById(R.id.all);
        all.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                for (CheckBox c : checkBoxes){ c.setChecked(true); }
            } else {
                for (CheckBox c : checkBoxes){ c.setChecked(false); }
            }
        });

        add_entry.setOnClickListener(view -> addEntry());

        calc.setOnClickListener(view -> parsing());

        next.setOnClickListener(view -> {
            if (cursor < charts_list.size() - 1){ cursor += 1; }

            hideCharts(0);
        });

        previous.setOnClickListener(view -> {
            if (cursor > 0){ cursor -= 1; }

            hideCharts(0);
        });

        c_next.setOnClickListener(view -> {
            if (c_cursor < charts_list.size() - 1){ c_cursor += 1; }

            hideCharts(1);
        });

        c_previous.setOnClickListener(view -> {
            if (c_cursor > 0) { c_cursor -= 1; }

            hideCharts(1);
        });

        final Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up),
                slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);

        setDrawable(R.mipmap.ic_expand_less_black_18dp);
        checkboxes.setVisibility(View.GONE);

        show_calculations.setOnClickListener(view -> {
            if (checkboxes.getVisibility() == View.VISIBLE){

                checkboxes.setVisibility(View.GONE);
                checkboxes.startAnimation(slideDown);
                setDrawable(R.mipmap.ic_expand_less_black_18dp);

            }else {

                checkboxes.setVisibility(View.VISIBLE);
                checkboxes.startAnimation(slideUp);
                setDrawable(R.mipmap.ic_expand_more_black_18dp);

            }
        });

        return root;
    }

    private void setDrawable(int res){
        Drawable drawable = ContextCompat.getDrawable(context, res);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.gray));
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            show_calculations.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void addEntry(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View rowView = inflater.inflate(R.layout.var_qlt_n_model, null);

        ImageButton delete = rowView.findViewById(R.id.delete_entry);
        delete.setOnClickListener(this::removeEntry);

        // Add the new row before the add field button.
        entries_layout.addView(rowView, entries_layout.getChildCount());
    }

    private void removeEntry(View v){
        entries_layout.removeView((View) v.getParent());
    }

    private List<String[]> getEntries(){
        int nb_entries = entries_layout.getChildCount();
        List<String[]> result = new ArrayList<>();
        String[] modalities, values;

        if (nb_entries > 1){
            modalities = new String[nb_entries];
            values = new String[nb_entries];

            for (int i = 0; i < nb_entries; i++){
                View v = entries_layout.getChildAt(i);

                if (v instanceof ViewGroup){
                    ViewGroup vGroup = (ViewGroup)v;

                    EditText edit1 = (EditText)vGroup.getChildAt(0),
                             edit2 = (EditText)vGroup.getChildAt(1);

                    modalities[i] = edit1.getText().toString();
                    values[i] = edit2.getText().toString();
                }
            }

            result.add(modalities);
            result.add(values);

            return result;

        }else{
            Toast.makeText(context, getString(R.string.insufficient_data), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private void parsing(){
        component = "";
        String modeModality = "";

        table.removeAllViews();
        bloc.setVisibility(View.VISIBLE);
        charts.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        c_barChart.setVisibility(View.GONE);
        c_pieChart.setVisibility(View.GONE);
        statsTitle.setVisibility(View.GONE);

        try {

            List<String[]> data = getEntries();
            String[] modalities, values;
            List<Double> real_values = new ArrayList<>(),
                    parsed_values = new ArrayList<>(),
                    frequencies = new ArrayList<>(),
                    c_values = new ArrayList<>(),
                    c_frequencies = new ArrayList<>();

            List<String> parsed_modalities = new ArrayList<>();
            Double effectif = 0.0, moyenneX, modeX = 0.0;

            String nbStr = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("nb", "3");

            int nb = Integer.valueOf(nbStr);

            if (data != null) {
                modalities = data.get(0);
                values = data.get(1);

                for (String value : values) {
                    real_values.add(Double.valueOf(value));

                    effectif += Double.valueOf(value);
                }

                for (String m : modalities) {
                    Double temp = 0.0;
                    if (!parsed_modalities.contains(m)) {
                        for (int i = 0; i < modalities.length; i++) {
                            if (m.equals(modalities[i])) {
                                temp += real_values.get(i);
                            }
                        }
                        parsed_values.add(arrondi(temp, nb));
                        parsed_modalities.add(m);
                    }
                }

                for (int i = 0; i < parsed_values.size(); i++) {
                    frequencies.add(arrondi((parsed_values.get(i) / effectif) * 100, nb));

                    if (parsed_values.get(i) > modeX) { modeX = parsed_values.get(i); modeModality = parsed_modalities.get(i); }

                    if (i == 0){

                        c_values.add(arrondi(parsed_values.get(i), nb));
                        c_frequencies.add(arrondi(frequencies.get(i), nb));

                    }else {

                        c_values.add(arrondi(c_values.get(i - 1) + parsed_values.get(i), nb));
                        c_frequencies.add(arrondi(c_frequencies.get(i - 1) + frequencies.get(i), nb));

                    }
                }

                statsTitle.setVisibility(View.VISIBLE);

                makeStatTable(parsed_modalities, parsed_values, frequencies, c_values, c_frequencies);

                moyenneX = arrondi(effectif / parsed_values.size(), nb);

                component += "<b>"+getString(R.string.qlt_tab_title_1)+"</b> : "+getString(R.string.table_modalities)+"<br>"+
                             "<b>"+getString(R.string.qlt_tab_title_2)+"</b> : "+getString(R.string.table_effectifs)+"<br>"+
                             "<b>"+getString(R.string.qlt_tab_title_3)+"</b> : "+getString(R.string.table_frequencies)+"<br>"+
                             "<b>"+getString(R.string.qlt_tab_title_4)+"</b> : "+getString(R.string.table_c_effectifs)+"<br>"+
                             "<b>"+getString(R.string.qlt_tab_title_5)+"</b> : "+getString(R.string.table_c_frequencies)+"<br><br>";

                component += "<b><font color='blue'><u>"+getString(R.string.effectif_total)+"</u></font></b><br><br>" +
                        "<b>E = &sum;Ni</b><br>"+
                        "<font color='red'><b><u>E = "+noZero(arrondi(effectif, nb))+"</u></b></font><br><br>";

                if (moyenne.isChecked()){
                    component += "<b><font color='blue'><u>"+getString(R.string.moyenne)+"</u></font></b><br><br>" +
                            "<b>X = &sum;Ni / n</b><br>"+
                            "X = "+noZero(effectif)+" / "+parsed_values.size()+ "<br>"+
                            "<font color='red'><b><u>X = "+noZero(moyenneX)+"</u></b></font><br><br>";
                }
                if (mode.isChecked()){
                    component += "<b><font color='blue'><u>"+getString(R.string.mode)+"</u></font></b><br><br>" +
                            getString(R.string.qlt_mode)+"<br>"+
                            "<font color='red'><b><u>Mo >>> "+modeModality+"</u></b></font><br><br>";
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    resolution.setText(Html.fromHtml(component, Html.FROM_HTML_MODE_COMPACT));
                }else{
                    resolution.setText(Html.fromHtml(component));
                }

                if (courbe.isChecked()){

                    charts.setVisibility(View.VISIBLE);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                    int[][] colors = new int[][]{ColorTemplate.MATERIAL_COLORS, ColorTemplate.VORDIPLOM_COLORS, ColorTemplate.JOYFUL_COLORS,
                            ColorTemplate.COLORFUL_COLORS, ColorTemplate.PASTEL_COLORS, ColorTemplate.LIBERTY_COLORS};

                    String graphikTheme = preferences.getString("graphikTheme","0");

                    int color = Integer.valueOf(graphikTheme);

                    Set<String> defaultSelections = new HashSet<>();
                    defaultSelections.add("bar");
                    defaultSelections.add("cercle");

                    Set<String> selections = preferences.getStringSet("graphikC", defaultSelections);

                    for (String s: selections){
                        switch (s){
                            case "bar":
                                //BarChart Effectifs
                                barChart.setTouchEnabled(true);
                                barChart.setDragEnabled(true);
                                barChart.setScaleEnabled(true);
                                barChart.animateY(2000);

                                List<BarEntry> barEntries = new ArrayList<>();
                                for (int i = 0; i < parsed_values.size(); i++){
                                    barEntries.add(new BarEntry(i, parsed_values.get(i).floatValue()));
                                }

                                BarDataSet barDataSet = new BarDataSet(barEntries, getString(R.string.v_bar_chart));
                                barDataSet.setColors(colors[color]);
                                BarData barData = new BarData(barDataSet);
                                barChart.setData(barData);
                                barChart.getDescription().setText(getString(R.string.distrib_var_qlt_n));
                                barChart.invalidate();

                                //BarChart Effectifs Cumulés
                                c_barChart.setTouchEnabled(true);
                                c_barChart.setDragEnabled(true);
                                c_barChart.setScaleEnabled(true);
                                c_barChart.animateY(2000);

                                List<BarEntry> c_barEntries = new ArrayList<>();
                                for (int i = 0; i < c_values.size(); i++){
                                    c_barEntries.add(new BarEntry(i, c_values.get(i).floatValue()));
                                }

                                BarDataSet c_barDataSet = new BarDataSet(c_barEntries, getString(R.string.v_bar_chart));
                                c_barDataSet.setColors(colors[color]);
                                BarData c_barData = new BarData(c_barDataSet);
                                c_barChart.setData(c_barData);
                                c_barChart.getDescription().setText(getString(R.string.distrib_var_qlt_n));
                                c_barChart.invalidate();
                                break;

                            case "cercle":
                                //PieChart Effectifs
                                pieChart.setTouchEnabled(true);
                                pieChart.setUsePercentValues(true);
                                pieChart.setRotationEnabled(true);
                                pieChart.setCenterTextColor(Color.BLACK);
                                pieChart.setHoleRadius(25f);
                                pieChart.setTransparentCircleAlpha(0);
                                pieChart.setCenterText(getString(R.string.distrib));
                                pieChart.setCenterTextSize(10);
                                pieChart.setDrawEntryLabels(true);
                                pieChart.setEntryLabelTextSize(20);
                                pieChart.animateY(2000);

                                Legend legend = pieChart.getLegend();
                                legend.setForm(Legend.LegendForm.CIRCLE);

                                List<PieEntry> pieEntries = new ArrayList<>();
                                for (int i = 0; i < parsed_values.size(); i++){
                                    pieEntries.add(new PieEntry(parsed_values.get(i).floatValue(), i));
                                }

                                PieDataSet pieDataSet = new PieDataSet(pieEntries, getString(R.string.pie_chart));
                                pieDataSet.setColors(colors[color]);
                                PieData pieData = new PieData(pieDataSet);
                                pieChart.setData(pieData);
                                pieChart.getDescription().setText(getString(R.string.distrib_var_qlt_n));
                                pieChart.invalidate();
                                
                                //PieChart Effectifs Cumulés
                                c_pieChart.setTouchEnabled(true);
                                c_pieChart.setUsePercentValues(true);
                                c_pieChart.setRotationEnabled(true);
                                c_pieChart.setCenterTextColor(Color.BLACK);
                                c_pieChart.setHoleRadius(25f);
                                c_pieChart.setTransparentCircleAlpha(0);
                                c_pieChart.setCenterText(getString(R.string.distrib));
                                c_pieChart.setCenterTextSize(10);
                                c_pieChart.setDrawEntryLabels(true);
                                c_pieChart.setEntryLabelTextSize(20);
                                c_pieChart.animateY(2000);

                                Legend c_legend = c_pieChart.getLegend();
                                c_legend.setForm(Legend.LegendForm.CIRCLE);

                                List<PieEntry> c_pieEntries = new ArrayList<>();
                                for (int i = 0; i < c_values.size(); i++){
                                    c_pieEntries.add(new PieEntry(c_values.get(i).floatValue(), i));
                                }

                                PieDataSet c_pieDataSet = new PieDataSet(c_pieEntries, getString(R.string.pie_chart));
                                c_pieDataSet.setColors(colors[color]);
                                PieData c_pieData = new PieData(c_pieDataSet);
                                c_pieChart.setData(c_pieData);
                                c_pieChart.getDescription().setText(getString(R.string.distrib_var_qlt_n));
                                c_pieChart.invalidate();
                                break;
                        }
                    }

                    switchingDefault(selections);

                }

                componentForSave = "<b><font color='blue'><u>"+getString(R.string.data)+"</u></font></b><br><br><b>"+getString(R.string.modalities)+"</b> "
                                   +parsed_modalities.toString()+"<br><b>"+getString(R.string.effectifs)+"</b>"+parsed_values.toString()+"<br><b>"+getString(R.string.frequencies)+"</b>"
                                   +frequencies.toString()+"<br><br>";
                componentForSave += component;

                xi_join = new StringBuilder(); ni_join = new StringBuilder();
                for (int i = 0; i < parsed_values.size(); i++){
                    xi_join.append(i).append("_");
                    ni_join.append(parsed_values.get(i)).append("_");
                }

                saveItem.setEnabled(true); shareItem.setEnabled(true);
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

            }

        }catch (Exception e){

            e.printStackTrace();
            Toast.makeText(context, R.string.syntax_error,Toast.LENGTH_SHORT).show();

        }

    }

    private static Double arrondi(Double A, int B) {
        return ( (int) (A * Math.pow(10, B) + .5)) / Math.pow(10, B);
    }

    private static String noZero(Double val){
        String[] section = String.valueOf(val).split("\\.");

        if (section[1].equals("0")){
            return section[0];
        }

        return String.valueOf(val);
    }

    private void makeStatTable(List<String> xi_recup, List<Double> ni_recup, List<Double> frequencies, List<Double> c_values, List<Double> c_frequencies){

        table.setStretchAllColumns(true);

        TableRow tbrow0 = new TableRow(getContext());
        tbrow0.setBackgroundResource(R.drawable.border);

        TextView tv0 = new TextView(getContext());
        tv0.setText(R.string.qlt_tab_title_1);
        tv0.setGravity(Gravity.CENTER);
        tv0.setTextColor(Color.MAGENTA);
        tv0.setBackgroundResource(R.drawable.border);
        tv0.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv0);

        TextView tv1 = new TextView(getContext());
        tv1.setText(R.string.qlt_tab_title_2);
        tv1.setGravity(Gravity.CENTER);
        tv1.setTextColor(Color.MAGENTA);
        tv1.setBackgroundResource(R.drawable.border);
        tv1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv1);

        TextView tv2 = new TextView(getContext());
        tv2.setText(R.string.qlt_tab_title_3);
        tv2.setGravity(Gravity.CENTER);
        tv2.setTextColor(Color.BLUE);
        tv2.setBackgroundResource(R.drawable.border);
        tv2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv2);

        TextView tv3 = new TextView(getContext());
        tv3.setText(R.string.qlt_tab_title_4);
        tv3.setGravity(Gravity.CENTER);
        tv3.setTextColor(Color.BLUE);
        tv3.setBackgroundResource(R.drawable.border);
        tv3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv3);

        TextView tv4 = new TextView(getContext());
        tv4.setText(R.string.qlt_tab_title_5);
        tv4.setGravity(Gravity.CENTER);
        tv4.setTextColor(Color.BLUE);
        tv4.setBackgroundResource(R.drawable.border);
        tv4.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv4);

        table.addView(tbrow0);

        for (int i = 0; i < xi_recup.size(); i++) {

            TableRow tbrow = new TableRow(getContext());
            tbrow.setBackgroundResource(R.drawable.border);

            TextView t1v = new TextView(getContext());
            t1v.setText(xi_recup.get(i));
            t1v.setTextColor(Color.BLACK);
            t1v.setGravity(Gravity.CENTER);
            t1v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t1v);

            TextView t2v = new TextView(getContext());
            t2v.setText(noZero(ni_recup.get(i)));
            t2v.setTextColor(Color.BLACK);
            t2v.setGravity(Gravity.CENTER);
            t2v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t2v);

            TextView t3v = new TextView(getContext());
            t3v.setText(noZero(frequencies.get(i)));
            t3v.setTextColor(Color.BLACK);
            t3v.setGravity(Gravity.CENTER);
            t3v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t3v);

            TextView t4v = new TextView(getContext());
            t4v.setText(noZero(c_values.get(i)));
            t4v.setTextColor(Color.BLACK);
            t4v.setGravity(Gravity.CENTER);
            t4v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t4v);

            TextView t5v = new TextView(getContext());
            t5v.setText(noZero(c_frequencies.get(i)));
            t5v.setTextColor(Color.BLACK);
            t5v.setGravity(Gravity.CENTER);
            t5v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t5v);

            table.addView(tbrow);
        }

    }

    private void switchingDefault(Set<String> charts){
        cursor = 0; c_cursor = 0;
        charts_list = new ArrayList<>();
        c_charts_list = new ArrayList<>();

        for (String s : charts){
            charts_list.add(s);
            c_charts_list.add(s);
        }

        previous.setVisibility(View.GONE);
        previous.startAnimation(fadeOut);
        c_previous.setVisibility(View.GONE);
        c_previous.startAnimation(fadeOut);

        if (charts_list.get(0).equals("cercle")){
            pieChart.setVisibility(View.VISIBLE);
            pieChart.startAnimation(fadeIn);
        }else{
            barChart.setVisibility(View.VISIBLE);
            barChart.startAnimation(fadeIn);
        }

        if (c_charts_list.get(0).equals("cercle")){
            c_pieChart.setVisibility(View.VISIBLE);
            c_pieChart.startAnimation(fadeIn);
        }else{
            c_barChart.setVisibility(View.VISIBLE);
            c_barChart.startAnimation(fadeIn);
        }
    }

    private void hideCharts(int w){
        if (w == 0){
            pieChart.setVisibility(View.GONE);
            pieChart.startAnimation(fadeOut);
            barChart.setVisibility(View.GONE);
            barChart.startAnimation(fadeOut);

            if (charts_list.get(cursor).equals("cercle")){
                pieChart.setVisibility(View.VISIBLE);
                pieChart.startAnimation(fadeIn);
            }else{
                barChart.setVisibility(View.VISIBLE);
                barChart.startAnimation(fadeIn);
            }

            if (cursor == 0){
                previous.setVisibility(View.GONE);
                previous.startAnimation(fadeOut);
            }else{
                previous.setVisibility(View.VISIBLE);
                previous.startAnimation(fadeIn);
            }

            if (cursor == charts_list.size() - 1){
                next.setVisibility(View.GONE);
                next.startAnimation(fadeOut);
            }else {
                next.setVisibility(View.VISIBLE);
                next.startAnimation(fadeIn);
            }

        }else {
            c_pieChart.setVisibility(View.GONE);
            c_pieChart.startAnimation(fadeOut);
            c_barChart.setVisibility(View.GONE);
            c_barChart.startAnimation(fadeOut);

            if (c_charts_list.get(c_cursor).equals("cercle")){
                c_pieChart.setVisibility(View.VISIBLE);
                c_pieChart.startAnimation(fadeIn);
            }else{
                c_barChart.setVisibility(View.VISIBLE);
                c_barChart.startAnimation(fadeIn);
            }

            if (c_cursor == 0){
                c_previous.setVisibility(View.GONE);
                c_previous.startAnimation(fadeOut);
            }else{
                c_previous.setVisibility(View.VISIBLE);
                c_previous.startAnimation(fadeIn);
            }

            if (c_cursor == c_charts_list.size() - 1){
                c_next.setVisibility(View.GONE);
                c_next.startAnimation(fadeOut);
            }else {
                c_next.setVisibility(View.VISIBLE);
                c_next.startAnimation(fadeIn);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        activity.getMenuInflater().inflate(R.menu.main, menu);

        saveItem = menu.getItem(0);
        shareItem = menu.getItem(1);
        shareItem.setVisible(false);

        if (component.isEmpty()){ saveItem.setEnabled(false); shareItem.setEnabled(false); }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_save:

                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(context);
                confirmBuilder.setTitle(getString(R.string.save_stats));
                confirmBuilder.setIcon(R.mipmap.ds_launcher);

                LayoutInflater factory = LayoutInflater.from(getContext());

                @SuppressLint("InflateParams") final View confirmDialogView = factory.inflate(R.layout.confirmation, null);

                final EditText editText = confirmDialogView.findViewById(R.id.editor_1);
                editText.setHint(getString(R.string.stats_name));

                confirmBuilder
                        .setView(confirmDialogView)
                        .setPositiveButton(getString(R.string.save), (dialogInterface, i) -> {})
                        .setNegativeButton(getString(R.string.cancel), null);

                final AlertDialog confirmDialog = confirmBuilder.create();
                confirmDialog.show();

                confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {

                    if (editText.getText().toString().isEmpty()){

                        Toast.makeText(context, getString(R.string.named_this_stats), Toast.LENGTH_SHORT).show();

                    }else {

                        db_manager.db_insertBackup(new Backups(editText.getText().toString(), componentForSave, xi_join.toString(), ni_join.toString()));
                        Toast.makeText(context, getString(R.string.safeguard_done), Toast.LENGTH_SHORT).show();
                        confirmDialog.dismiss();

                    }

                });

                break;

            case R.id.action_share:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
