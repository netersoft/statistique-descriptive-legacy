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
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.neteru.tixtat.R;
import com.neteru.tixtat.classes.databases.DB_Manager;
import com.neteru.tixtat.classes.models.Backups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class ContinuousVariablesFragment extends Fragment {

    private Context context;
    private Activity activity;
    private CheckBox moyenne;
    private CheckBox mediane;
    private CheckBox mode;
    private CheckBox variance;
    private CheckBox ecart;
    private CheckBox courbe;
    private CheckBox coef;
    private CheckBox quartiles;
    private CheckBox covariance;
    private TextView resolution;
    private TextView med_mod;
    private TextView statsTitle;
    private TextView show_calculations;
    private LineChart lineChart;
    private LineChart linePointChart;
    private BarChart barChart;
    private HorizontalBarChart horizontalBarChart;
    private BubbleChart bubbleChart;
    private ScatterChart scatterChart;
    private int cursor;
    private String component;
    private String componentForSave;
    private TableLayout statsTable;
    private LinearLayout bloc;
    private LinearLayout entries_layout;
    private LinearLayout checkboxes;
    private CardView charts;
    private ScrollView scrollView;
    private MenuItem saveItem;
    private MenuItem shareItem;
    private DB_Manager db_manager;
    private StringBuilder xi_join;
    private StringBuilder ni_join;
    private List<String> charts_list;
    private Animation fadeIn;
    private Animation fadeOut;
    private ImageView next;
    private ImageView previous;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_continuous_variables, container, false);

        if (getContext() != null){ context = getContext(); }
        if (getActivity() != null){ activity = getActivity(); }

        //Recuperation des elements de la vue
        moyenne = root.findViewById(R.id.moyenne);
        mediane = root.findViewById(R.id.mediane);
        mode = root.findViewById(R.id.mode);
        variance = root.findViewById(R.id.variance);
        covariance = root.findViewById(R.id.covariance);
        quartiles = root.findViewById(R.id.quartiles);
        ecart = root.findViewById(R.id.ecart);
        coef = root.findViewById(R.id.coef);
        courbe = root.findViewById(R.id.courbe);
        resolution = root.findViewById(R.id.resolution);
        statsTable = root.findViewById(R.id.statsTable);
        lineChart = root.findViewById(R.id.line_chart);
        barChart = root.findViewById(R.id.v_bar_chart);
        horizontalBarChart = root.findViewById(R.id.h_bar_chart);
        bubbleChart = root.findViewById(R.id.buble_chart);
        scatterChart = root.findViewById(R.id.point_chart);
        linePointChart = root.findViewById(R.id.line_point_chart);
        charts = root.findViewById(R.id.charts);
        med_mod = root.findViewById(R.id.med_mod);
        statsTitle = root.findViewById(R.id.statsTableTitle);
        bloc = root.findViewById(R.id.bloc);
        entries_layout = root.findViewById(R.id.entries_layout);
        scrollView = root.findViewById(R.id.scrollView);
        next = root.findViewById(R.id.next);
        previous = root.findViewById(R.id.previous);
        show_calculations = root.findViewById(R.id.show_calculations);
        checkboxes = root.findViewById(R.id.checkboxes);
        component = "";

        db_manager = new DB_Manager(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        
        bloc.setVisibility(View.GONE);
        charts.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        horizontalBarChart.setVisibility(View.GONE);
        bubbleChart.setVisibility(View.GONE);
        scatterChart.setVisibility(View.GONE);
        linePointChart.setVisibility(View.GONE);
        statsTitle.setVisibility(View.GONE);

        final CheckBox[] checkBoxes = new CheckBox[]{moyenne, mediane, mode, variance, ecart, courbe, coef, quartiles, covariance};

        CheckBox all = root.findViewById(R.id.all);
        all.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                for (CheckBox c : checkBoxes){ c.setChecked(true); }
            } else {
                for (CheckBox c : checkBoxes){ c.setChecked(false); }
            }
        });

        root.findViewById(R.id.add_entry).setOnClickListener(view -> addEntry());

        root.findViewById(R.id.calculBut).setOnClickListener(view -> parsing());

        next.setOnClickListener(view -> {
            if (cursor < charts_list.size() - 1){ cursor += 1; }

            hideCharts();
        });

        previous.setOnClickListener(view -> {
            if (cursor > 0){ cursor -= 1; }

            hideCharts();
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
        @SuppressLint("InflateParams") final View rowView = inflater.inflate(R.layout.var_qtv_c_model, null);

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
        String[] xi_1, xi_2, ni;

        if (nb_entries > 1){
            xi_1 = new String[nb_entries];
            xi_2 = new String[nb_entries];
            ni = new String[nb_entries];

            for (int i = 0; i < nb_entries; i++){
                View v = entries_layout.getChildAt(i);

                if (v instanceof ViewGroup){
                    ViewGroup vGroup = (ViewGroup)v;

                    EditText edit1 = (EditText)vGroup.getChildAt(0),
                             edit2 = (EditText)vGroup.getChildAt(1),
                             edit3 = (EditText)vGroup.getChildAt(2);

                    xi_1[i] = edit1.getText().toString();
                    xi_2[i] = edit2.getText().toString();
                    ni[i] = edit3.getText().toString();
                }
            }

            result.add(xi_1);
            result.add(xi_2);
            result.add(ni);

            return result;

        }else{
            Toast.makeText(context, getString(R.string.insufficient_data), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private void parsing(){
        component = "";
        statsTable.removeAllViews();

        bloc.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        horizontalBarChart.setVisibility(View.GONE);
        bubbleChart.setVisibility(View.GONE);
        scatterChart.setVisibility(View.GONE);
        linePointChart.setVisibility(View.GONE);
        statsTitle.setVisibility(View.GONE);

        String nbStr = preferences.getString("nb","3");

        int nb = Integer.valueOf(nbStr);

        List<String[]> data = getEntries();

        if (data != null){

            String[] ni_tab = data.get(2),
                     xi_2_tab = data.get(1),
                     xi_1_tab = data.get(0);

            double[] l1 = new double[xi_1_tab.length],
                     l2 = new double[xi_2_tab.length],
                     k = new double[ni_tab.length],
                     xi_r = new double[ni_tab.length];

            double[] ni_recup = new double[ni_tab.length],
                     xi_recup = new double[ni_tab.length],
                     xini_recup = new double[ni_tab.length],
                     xi2ni_recup = new double[ni_tab.length];

            double xi_sum = 0,
                   xini_sum = 0,
                   ni_sum = 0,
                   xi2ni_sum = 0;

            int indiceNiMax = 0, indiceUpMax = 0;
            double upMax = 0, niMax = 0, xiMax = 0, xiMin = 0;

            double[] up = new double[ni_tab.length];
            double[] down = new double[ni_tab.length];

            double[] operateurMediane = new double[ni_tab.length];
            double[] operateurFirstQuart = new double[ni_tab.length];
            double[] operateurThirdQuart = new double[ni_tab.length];
            double[] operateurFirstDecile = new double[ni_tab.length];
            double[] operateurNinthDecile = new double[ni_tab.length];

            int indiceOperateurMediane = 0, indiceOperateurFirstQuart = 0, indiceOperateurThirdQuart = 0,
                    indiceOperateurFirstDecile = 0, indiceOperateurNinthDecile = 0;

            double operateurMedianeMin = 0, operateurFirstQuartMin = 0, operateurThirdQuartMin = 0,
                    coVarianceSum = 0, crXSum = 0, crYSum = 0, operateurFirstDecileMin = 0, operateurNinthDecileMin = 0;

            double moyenneX, moyenneY, medianeX, modeX, varianceX, ecartXY, coefX, rangeX,
                    firstQuartX, thirdQuartX, interQuartX, coVarianceXY, r, ecartX, ecartY,
                     erreurX, firstDecileX, ninthDecileX;

            if (ni_tab.length == xi_1_tab.length && ni_tab.length == xi_2_tab.length){
                try{
                    for (int i=0;i < ni_tab.length;i++){

                        //Recuperation des bornes et de l'amplitude
                        l1[i] = Double.valueOf(xi_1_tab[i]);
                        l2[i] = Double.valueOf(xi_2_tab[i]);
                        k[i] = l2[i] - l1[i];

                        if (k[i] < 0){
                            throw new java.lang.Exception("K could not be negative !");
                        }

                        //Recuperation de xi et ni
                        ni_recup[i] = arrondi(Double.valueOf(ni_tab[i]), nb);
                        xi_recup[i] = arrondi((l1[i] + l2[i]) / 2, nb);

                        //Recuperation de £xi et £ni
                        ni_sum += ni_recup[i];
                        xi_sum += xi_recup[i];

                        //Recuperation de xini et de xi²ni
                        xini_recup[i] = arrondi(ni_recup[i] * xi_recup[i], nb);
                        xi2ni_recup[i] = arrondi(Math.pow(xi_recup[i],2) * ni_recup[i], nb);

                        //Recuperation de £xini et £xi²ni
                        xini_sum += xini_recup[i];
                        xi2ni_sum += xi2ni_recup[i];

                        //Recuperation de niMax
                        if (ni_recup[i] > niMax){
                            niMax = ni_recup[i];
                            indiceNiMax = i;
                        }

                        //Recuperation de xiMax
                        if (l2[i] > xiMax){
                            xiMax = l2[i];
                        }

                        //Recuperation de xiMin
                        if (i == 0){
                            xiMin = l1[i];
                        }else{
                            if (l1[i] < xiMin){
                                xiMin = l1[i];
                            }
                        }
                    }

                    for (int i = 0;i < ni_tab.length;i++){
                        //Recuperation de Ni croissant et de Ni décroissant
                        if (i == 0){
                            up[i] = arrondi(ni_recup[i], nb);
                            down[i] = arrondi(ni_sum, nb);
                        }else {
                            up[i] = arrondi(up[i-1] + ni_recup[i], nb);
                            down[i] = arrondi(down[i-1] - ni_recup[i-1], nb);
                        }

                        //Recuperation de upMax
                        if (up[i] > upMax){
                            upMax = up[i];
                            /* indiceUpMax = i; */
                        }

                        double v1 = ni_recup[i] - (ni_sum / ni_tab.length);

                        //Operateur Mediane
                        operateurMediane[i] = up[i] - (ni_sum / 2);

                        //Operateur Premier Quartile
                        operateurFirstQuart[i] = up[i] - (ni_sum / 4);

                        //Operateur Troisième Quartile
                        operateurThirdQuart[i] = up[i] - ((ni_sum * 3) / 4);

                        //Operateur Premier Decile
                        operateurFirstDecile[i] = up[i] - (ni_sum / 10);

                        //Operateur Neuvième Decile
                        operateurNinthDecile[i] = up[i] - ((ni_sum * 9) / 10);

                        //Recuperation de la somme des produits de covarianceX et covarianceY
                        coVarianceSum += (xi_recup[i] - (xi_sum / xi_recup.length)) * v1;

                        //Recuperation de la somme des (Xi - X)²
                        crXSum += Math.pow(xi_recup[i] - (xi_sum / ni_tab.length), 2);

                        //Recuperation de la somme des (Yi - Y)²
                        crYSum += Math.pow(v1, 2);
                    }

                    //Recuperation de la valeur qui succede directement à la moitié de la somme des ni
                    boolean passM = true, passFQ = true, passTQ = true, passFD = true, passND = true;
                    for (int i = 0;i < operateurMediane.length;i++){

                        if (operateurMediane[i] > 0){
                            if (passM){
                                operateurMedianeMin = operateurMediane[i];
                                indiceOperateurMediane = i;
                                passM = false;
                            }else{
                                if (operateurMediane[i] < operateurMedianeMin){
                                    operateurMedianeMin = operateurMediane[i];
                                    indiceOperateurMediane = i;
                                }
                            }
                        }

                        if (operateurFirstQuart[i] > 0){
                            if (passFQ){
                                operateurFirstQuartMin = operateurFirstQuart[i];
                                indiceOperateurFirstQuart = i;
                                passFQ = false;
                            }else{
                                if (operateurFirstQuart[i] < operateurFirstQuartMin){
                                    operateurFirstQuartMin = operateurFirstQuart[i];
                                    indiceOperateurFirstQuart = i;
                                }
                            }
                        }

                        if (operateurThirdQuart[i] > 0){
                            if (passTQ){
                                operateurThirdQuartMin = operateurThirdQuart[i];
                                indiceOperateurThirdQuart = i;
                                passTQ = false;
                            }else{
                                if (operateurThirdQuart[i] < operateurThirdQuartMin){
                                    operateurThirdQuartMin = operateurThirdQuart[i];
                                    indiceOperateurThirdQuart = i;
                                }
                            }
                        }

                        if (operateurFirstDecile[i] > 0){
                            if (passFD){
                                operateurFirstDecileMin = operateurFirstDecile[i];
                                indiceOperateurFirstDecile = i;
                                passFD = false;
                            }else{
                                if (operateurFirstDecile[i] < operateurFirstDecileMin){
                                    operateurFirstDecileMin = operateurFirstDecile[i];
                                    indiceOperateurFirstDecile = i;
                                }
                            }
                        }

                        if (operateurNinthDecile[i] > 0){
                            if (passND){
                                operateurNinthDecileMin = operateurNinthDecile[i];
                                indiceOperateurNinthDecile = i;
                                passND = false;
                            }else{
                                if (operateurNinthDecile[i] < operateurNinthDecileMin){
                                    operateurNinthDecileMin = operateurNinthDecile[i];
                                    indiceOperateurNinthDecile = i;
                                }
                            }
                        }
                    }

                    //Calculs
                    //Moyenne Pondérée
                    moyenneX = arrondi((xini_sum / ni_sum), nb);
                    //Moyenne Simple
                    moyenneY = arrondi((ni_sum / xi_recup.length), nb);
                    //Mode
                    double v2 = ni_recup[indiceNiMax] - ni_recup[indiceNiMax - 1];
                    modeX = arrondi(l1[indiceNiMax] + k[indiceNiMax]*(v2 / (v2 +(ni_recup[indiceNiMax] - ni_recup[indiceNiMax + 1]))), nb);
                    //Mediane
                    medianeX = arrondi(l1[indiceOperateurMediane] + k[indiceOperateurMediane] * (((ni_sum / 2) - up[indiceOperateurMediane - 1]) / ni_recup[indiceOperateurMediane]), nb);
                    //FirstQuart
                    firstQuartX = arrondi(l1[indiceOperateurFirstQuart] + k[indiceOperateurFirstQuart] * (((ni_sum / 4) - up[indiceOperateurFirstQuart - 1]) / ni_recup[indiceOperateurFirstQuart]), nb);
                    //ThirdQuart
                    thirdQuartX = arrondi(l1[indiceOperateurThirdQuart] + k[indiceOperateurThirdQuart] * ((((ni_sum * 3) / 4) - up[indiceOperateurThirdQuart - 1]) / ni_recup[indiceOperateurThirdQuart]), nb);
                    //FirstDecile
                    firstDecileX = arrondi(xi_recup[indiceOperateurFirstDecile], nb);
                    //NinthDecile
                    ninthDecileX = arrondi(xi_recup[indiceOperateurNinthDecile], nb);
                    //Intervalle interquartile
                    interQuartX = arrondi(thirdQuartX - firstQuartX, nb);
                    //Variance
                    varianceX = arrondi(((xi2ni_sum / ni_sum) - Math.pow(moyenneX,2)), nb);
                    //CoVariance
                    coVarianceXY = arrondi((coVarianceSum / (ni_tab.length - 1)), nb);
                    //Coefficient de corrélation linéaire
                    ecartX = Math.sqrt(crXSum / (ni_tab.length - 1));
                    ecartY = Math.sqrt(crYSum / (ni_tab.length - 1));
                    r = arrondi(coVarianceXY / (ecartX * ecartY), nb);
                    //Ecart Type
                    ecartXY = arrondi(Math.sqrt(varianceX), nb);
                    //Erreur Type
                    erreurX = arrondi(ecartXY / Math.sqrt(ni_tab.length), nb);
                    //Coefficient de variation
                    coefX = arrondi((ecartXY / moyenneX) * 100, nb);
                    //Etendue
                    rangeX = xiMax - xiMin;

                    String med_mod_str = getString(R.string.mediant_class)+noZero(l1[indiceOperateurMediane])+" - "+noZero(l2[indiceOperateurMediane])+"[\n" +
                                         getString(R.string.modal_class)+noZero(l1[indiceNiMax])+" - "+noZero(l2[indiceNiMax])+"[";

                    med_mod.setText(med_mod_str);

                    statsTitle.setVisibility(View.VISIBLE);

                    makeStatTable(xi_recup, ni_recup, xini_recup, xi2ni_recup, up, down);

                    if (moyenne.isChecked()){
                        component += "<b><font color='blue'><u>"+getString(R.string.moyenne)+"</u></font></b><br>" +
                                "<font color='magenta'>"+getString(R.string.weighted_mean)+"</font><br>"+
                                "<b>X = &sum;XiNi / &sum;Ni</b><br>"+
                                "X = "+noZero(arrondi(xini_sum, nb))+" / "+noZero(ni_sum)+ "<br>"+
                                "<font color='red'><b><u>X = "+noZero(moyenneX)+"</u></b></font><br><br>"+
                                "<font color='magenta'>"+getString(R.string.simple_mean)+"</font><br>"+
                                "<b>X = &sum;Ni / n</b><br>"+
                                "X = "+noZero(ni_sum)+" / "+xi_recup.length+ "<br>"+
                                "<font color='red'><b><u>X = "+noZero(moyenneY)+"</u></b></font><br><br>";
                    }
                    if (mode.isChecked()){
                        component += "<b><font color='blue'><u>"+getString(R.string.mode)+"</u></font></b><br><br>" +
                                "<b>Mo = L1 + k((N0 - N1) / ((N0 - N1) + (N0 - N2))</b><br>"+
                                "Mo = "+noZero(l1[indiceNiMax]) +" + "+ noZero(k[indiceNiMax])+" * "+"(("+noZero(ni_recup[indiceNiMax])+ " - "
                                       + noZero(ni_recup[indiceNiMax - 1])+") / (("+noZero(ni_recup[indiceNiMax]) +" - "+ noZero(ni_recup[indiceNiMax - 1])
                                       +")+("+noZero(ni_recup[indiceNiMax]) +"-"+ noZero(ni_recup[indiceNiMax + 1])+ ")))<br>"+ "<font color='red'><b><u>Mo = "+noZero(modeX)+"</u></b></font><br><br>";
                    }
                    if (mediane.isChecked()){
                        component += "<b><font color='blue'><u>"+getString(R.string.mediane)+"</u></font></b><br><br>" +
                                "<b>Me = L1 + k((1/2 * &sum;Ni - N1) / Ne)</b><br>"+
                                "Me = "+noZero(l1[indiceOperateurMediane]) +" + "+ noZero(k[indiceOperateurMediane]) +" * ((("+noZero(ni_sum / 2)+") - "
                                + noZero(up[indiceOperateurMediane - 1])+ ") / "+noZero(ni_recup[indiceOperateurMediane])+")<br>"+ "<font color='red'><b><u>Me = "+noZero(medianeX)+"</u></b></font><br><br>";
                    }
                    if (quartiles.isChecked()){
                        component += "<b><font color='blue'><u>"+getString(R.string.quartiles)+"</u></font></b><br><br>" +
                                "<font color='magenta'>"+getString(R.string.first_quart)+"</font><br>"+
                                getString(R.string.qtv_d_first_quart)+"<b>1/4&sum;Ni</b><br>"+
                                "<font color='red'><b><u>Q1 = "+noZero(firstQuartX)+"</u></b></font><br><br>"+
                                "<font color='magenta'>"+getString(R.string.third_quart)+"</font><br>"+
                                getString(R.string.qtv_d_third_quart)+"<b>3/4&sum;Ni</b><br>"+
                                "<font color='red'><b><u>Q3 = "+noZero(thirdQuartX)+"</u></b></font><br><br>"+
                                "<font color='magenta'>"+getString(R.string.inter_quart)+"</font><br>"+
                                "<b>IIQ = Q3 - Q1</b><br>"+
                                "IIQ = "+noZero(thirdQuartX)+" - "+noZero(firstQuartX)+"<br>"+
                                "<font color='red'><b><u>IIQ = "+noZero(interQuartX)+"</u></b></font><br><br>"+

                                "<b><font color='blue'><u>"+getString(R.string.deciles)+"</u></font></b><br><br>" +
                                "<font color='magenta'>"+getString(R.string.first_decile)+"</font><br>"+
                                getString(R.string.qtv_d_first_decile)+"<b>1/10&sum;Ni</b><br>"+
                                "<font color='red'><b><u>Q1 = "+noZero(firstDecileX)+"</u></b></font><br><br>"+
                                "<font color='magenta'>"+getString(R.string.ninth_decile)+"</font><br>"+
                                getString(R.string.qtv_d_ninth_decile)+"<b>9/10&sum;Ni</b><br>"+
                                "<font color='red'><b><u>Q3 = "+noZero(ninthDecileX)+"</u></b></font><br><br>";
                    }
                    if (variance.isChecked()){
                        component += "<b><font color='blue'><u>"+getString(R.string.variance)+"</u></font></b><br><br>" +
                                "<b>Vx² = (&sum;Xi²Ni / &sum;Ni) - X²</b><br>"+
                                "Vx² = ("+noZero(arrondi(xi2ni_sum, nb))+" / "+noZero(ni_sum)+") - "+noZero(moyenneX)+"²<br>"+
                                "<font color='red'><b><u>Vx² = "+noZero(varianceX)+"</u></b></font><br><br>";

                    }
                    if (covariance.isChecked()){
                        component += "<b><font color='blue'><u>"+getString(R.string.covariance)+"</u></font></b><br><br>" +
                                "<b>Cov(X,Y) = &sum;(Xi - X)(Yi - Y) / (n - 1)</b><br>"+
                                "Cov(X,Y) = "+noZero(arrondi(coVarianceSum, nb))+" / "+ni_tab.length+" - 1<br>"+
                                "<font color='red'><b><u>Cov(X,Y) = "+noZero(coVarianceXY)+"</u></b></font><br><br>"+
                                "<b><font color='blue'><u>"+getString(R.string.coef)+"</u></font></b><br><br>" +
                                "<b>r = Cov(X, Y) / &sigma;(x)&sigma;(y)</b><br>"+
                                "r = "+noZero(coVarianceXY)+" / ( "+noZero(arrondi(ecartX, nb))+" * "+noZero(arrondi(ecartY, nb))+" )<br>"+
                                "<font color='red'><b><u>r = "+noZero(r)+"</u></b></font><br><br>";
                    }
                    if (ecart.isChecked()){
                        component += "<b><font color='blue'><u>"+getString(R.string.ecart_type)+"</u></font></b><br><br>" +
                                "<b>&sigma; = &radic;(Vx²)</b><br>"+
                                "&sigma; = &radic;("+noZero(varianceX)+")<br>"+
                                "<font color='red'><b><u>&sigma; = "+noZero(ecartXY)+"</u></b></font><br><br>"+
                                "<b><font color='blue'><u>"+getString(R.string.standard_error)+"</u></font></b><br><br>" +
                                "<b>SE = &sigma; / &radic;n</b><br>"+
                                "SE = "+noZero(ecartXY)+" / &radic;"+ni_tab.length+"<br>"+
                                "<font color='red'><b><u>SE = "+noZero(erreurX)+"</u></b></font><br><br>";
                    }
                    if (coef.isChecked()){
                        component += "<b><font color='blue'><u>"+getString(R.string.coef_de_var)+"</u></font></b><br><br>" +
                                "<b>CV = (Vx / X) * 100</b><br>"+
                                "CV = ("+noZero(ecartXY)+" / "+noZero(moyenneX)+") * 100<br>"+
                                "<font color='red'><b><u>CV = "+noZero(coefX)+"</u></b></font><br><br>";
                        if (coefX <= 33){
                            component += "<b>"+getString(R.string.distrib_homo)+"</b><br>";
                        }else{
                            component += "<b>"+getString(R.string.distrib_hetero)+"</b><br><br>";
                        }
                    }

                    component += "<b><font color='blue'><u>"+getString(R.string.range)+"</u></font></b><br><br>" +
                            "<b>E = XiMax - XiMin</b><br>"+
                            "E = "+noZero(xiMax)+" - "+noZero(xiMin)+"<br>"+
                            "<font color='red'><b><u>E = "+noZero(rangeX)+"</u></b></font><br><br>";


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        resolution.setText(Html.fromHtml(component, Html.FROM_HTML_MODE_COMPACT));
                    }else{
                        resolution.setText(Html.fromHtml(component));
                    }

                    if (courbe.isChecked()){

                        charts.setVisibility(View.VISIBLE);

                        int[][] colors = new int[][]{ColorTemplate.MATERIAL_COLORS, ColorTemplate.VORDIPLOM_COLORS, ColorTemplate.JOYFUL_COLORS,
                                                     ColorTemplate.COLORFUL_COLORS, ColorTemplate.PASTEL_COLORS, ColorTemplate.LIBERTY_COLORS};

                        String graphikTheme = preferences.getString("graphikTheme","0");

                        int color = Integer.valueOf(graphikTheme);

                        Set<String> defaultSelections = new HashSet<>();
                        defaultSelections.add("line");
                        defaultSelections.add("v_bar");
                        defaultSelections.add("point");

                        Set<String> selections = preferences.getStringSet("graphikB", defaultSelections);

                        for (String s: selections){
                            switch (s){
                                case "line":
                                    lineChart.setTouchEnabled(true);
                                    lineChart.setDragEnabled(true);
                                    lineChart.setScaleEnabled(true);
                                    lineChart.animateY(2000);

                                    List<Entry> entries = new ArrayList<>();
                                    for (int i = 0; i < xi_recup.length; i++){
                                        entries.add(new Entry((float) xi_recup[i], (float) ni_recup[i]));
                                    }
                                    Collections.sort(entries, new EntryXComparator());

                                    LineDataSet dataSet = new LineDataSet(entries, getString(R.string.lines_chart));
                                    dataSet.setColors(colors[color]);
                                    dataSet.setDrawFilled(true);
                                    LineData lineData = new LineData(dataSet);
                                    lineChart.setData(lineData);
                                    lineChart.getDescription().setText(getString(R.string.distrib_var_c));
                                    lineChart.invalidate();
                                    break;

                                case "v_bar":
                                    barChart.setTouchEnabled(true);
                                    barChart.setDragEnabled(true);
                                    barChart.setScaleEnabled(true);
                                    barChart.animateY(2000);

                                    List<BarEntry> barEntries = new ArrayList<>();
                                    for (int i = 0; i < xi_recup.length; i++){
                                        barEntries.add(new BarEntry((float) xi_recup[i], (float) ni_recup[i]));
                                    }

                                    BarDataSet barDataSet = new BarDataSet(barEntries, getString(R.string.v_bar_chart));
                                    barDataSet.setColors(colors[color]);
                                    BarData barData = new BarData(barDataSet);
                                    barChart.setData(barData);
                                    barChart.getDescription().setText(getString(R.string.distrib_var_c));
                                    barChart.invalidate();
                                    break;

                                case "h_bar":
                                    horizontalBarChart.setTouchEnabled(true);
                                    horizontalBarChart.setDragEnabled(true);
                                    horizontalBarChart.setScaleEnabled(true);
                                    horizontalBarChart.animateY(2000);

                                    List<BarEntry> hBarEntries = new ArrayList<>();
                                    for (int i = 0; i < xi_recup.length; i++){
                                        hBarEntries.add(new BarEntry((float) xi_recup[i], (float) ni_recup[i]));
                                    }

                                    BarDataSet hBarDataSet = new BarDataSet(hBarEntries, getString(R.string.h_bar_chart));
                                    hBarDataSet.setColors(colors[color]);
                                    BarData hBarData = new BarData(hBarDataSet);
                                    horizontalBarChart.setData(hBarData);
                                    horizontalBarChart.getDescription().setText(getString(R.string.distrib_var_c));
                                    horizontalBarChart.invalidate();
                                    break;

                                case "bubble":
                                    bubbleChart.setTouchEnabled(true);
                                    bubbleChart.setDragEnabled(true);
                                    bubbleChart.setScaleEnabled(true);
                                    bubbleChart.animateY(2000);

                                    List<BubbleEntry> bubbleEntries = new ArrayList<>();
                                    for (int i = 0; i < xi_recup.length; i++){
                                        bubbleEntries.add(new BubbleEntry((float) xi_recup[i], (float) ni_recup[i], (float) ni_recup[i]));
                                    }

                                    BubbleDataSet bubbleDataSet = new BubbleDataSet(bubbleEntries, getString(R.string.bubble_chart));
                                    bubbleDataSet.setColors(colors[color]);
                                    BubbleData bubbleData = new BubbleData(bubbleDataSet);
                                    bubbleChart.setData(bubbleData);
                                    bubbleChart.getDescription().setText(getString(R.string.distrib_var_c));
                                    bubbleChart.invalidate();
                                    break;

                                case "point":
                                    scatterChart.setTouchEnabled(true);
                                    scatterChart.setDragEnabled(true);
                                    scatterChart.setScaleEnabled(true);
                                    scatterChart.animateY(2000);

                                    List<Entry> scatterEntries = new ArrayList<>();
                                    for (int i = 0; i < xi_recup.length; i++){
                                        scatterEntries.add(new Entry((float) xi_recup[i], (float) ni_recup[i]));
                                    }

                                    ScatterDataSet scatterDataSet = new ScatterDataSet(scatterEntries, getString(R.string.dispersion_chart));
                                    scatterDataSet.setColors(colors[color]);
                                    ScatterData scatterData = new ScatterData(scatterDataSet);
                                    scatterChart.setData(scatterData);
                                    scatterChart.getDescription().setText(getString(R.string.distrib_var_c));
                                    scatterChart.invalidate();
                                    break;

                                case "line_point":
                                    linePointChart.setTouchEnabled(true);
                                    linePointChart.setDragEnabled(true);
                                    linePointChart.setScaleEnabled(true);
                                    linePointChart.animateY(2000);

                                    List<Entry> linePointEntries = new ArrayList<>();
                                    for (int i = 0; i < xi_recup.length; i++){
                                        linePointEntries.add(new Entry((float) xi_recup[i], (float) ni_recup[i]));
                                    }
                                    Collections.sort(linePointEntries, new EntryXComparator());

                                    LineDataSet linePointDataSet = new LineDataSet(linePointEntries, getString(R.string.lines_points_chart));
                                    linePointDataSet.setColors(colors[color]);
                                    linePointDataSet.setHighlightEnabled(true);
                                    linePointDataSet.enableDashedLine(10f, 5f, 0f);
                                    linePointDataSet.enableDashedHighlightLine(10f, 5f, 0f);
                                    linePointDataSet.setCircleColor(Color.DKGRAY);
                                    linePointDataSet.setLineWidth(5f);
                                    linePointDataSet.setCircleRadius(10f);
                                    LineData linePointData = new LineData(linePointDataSet);
                                    linePointChart.setData(linePointData);
                                    linePointChart.getDescription().setText(getString(R.string.distrib_var_c));
                                    linePointChart.invalidate();
                                    break;
                            }
                        }

                        switchingDefault(selections);

                    }

                    componentForSave = "<b><font color='blue'><u>DONNEES</u></font></b><br><br><b>Xi :</b> "
                            + Arrays.toString(xi_recup)+"<br><b>Ni :</b> "+Arrays.toString(ni_recup)+"<br><b>XiNi :</b> "+Arrays.toString(xini_recup)
                            +"<br><b>Xi²Ni :</b> "+Arrays.toString(xi2ni_recup)+"<br><b>N+ :</b> "+Arrays.toString(up)+"<br><b>N- :</b> "+Arrays.toString(down)+"<br><br>";
                    componentForSave += component;

                    xi_join = new StringBuilder(); ni_join = new StringBuilder();
                    for (int i = 0; i < xi_recup.length; i++){
                        xi_join.append(xi_recup[i]).append("_");

                        ni_join.append(ni_recup[i]).append("_");
                    }

                    saveItem.setEnabled(true); shareItem.setEnabled(true);
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

                }catch (Exception e){

                    e.printStackTrace();
                    Toast.makeText(context,R.string.syntax_error,Toast.LENGTH_SHORT).show();

                }

            }else{
                Toast.makeText(context,R.string.length_error,Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(context,R.string.empty_field_error,Toast.LENGTH_SHORT).show();
        }
    }

    private static double arrondi(double A, int B) {
        return  (double) ( (int) (A * Math.pow(10, B) + .5)) / Math.pow(10, B);
    }

    private static String noZero(double val){
        String[] section = String.valueOf(val).split("\\.");

        if (section[1].equals("0")){
            return section[0];
        }

        return String.valueOf(val);
    }

    private void makeStatTable(double[] xi_recup, double[] ni_recup, double[] xini_recup, double[] xi2ni_recup, double[] up, double[] down){

        statsTable.setStretchAllColumns(true);

        TableRow tbrow0 = new TableRow(context);
        tbrow0.setBackgroundResource(R.drawable.border);

        TextView tv0 = new TextView(context);
        tv0.setText(R.string.table_xi);
        tv0.setGravity(Gravity.CENTER);
        tv0.setTextColor(Color.MAGENTA);
        tv0.setBackgroundResource(R.drawable.border);
        tv0.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv0);
        
        TextView tv1 = new TextView(context);
        tv1.setText(R.string.table_ni);
        tv1.setGravity(Gravity.CENTER);
        tv1.setTextColor(Color.MAGENTA);
        tv1.setBackgroundResource(R.drawable.border);
        tv1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv1);
        
        TextView tv2 = new TextView(context);
        tv2.setText(R.string.table_xini);
        tv2.setGravity(Gravity.CENTER);
        tv2.setTextColor(Color.BLUE);
        tv2.setBackgroundResource(R.drawable.border);
        tv2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv2);
        
        TextView tv3 = new TextView(context);
        tv3.setText(R.string.table_xi2ni);
        tv3.setGravity(Gravity.CENTER);
        tv3.setTextColor(Color.BLUE);
        tv3.setBackgroundResource(R.drawable.border);
        tv3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv3);

        TextView tv4 = new TextView(context);
        tv4.setText(R.string.table_up);
        tv4.setGravity(Gravity.CENTER);
        tv4.setTextColor(Color.RED);
        tv4.setBackgroundResource(R.drawable.border);
        tv4.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv4);

        TextView tv5 = new TextView(context);
        tv5.setText(R.string.table_down);
        tv5.setGravity(Gravity.CENTER);
        tv5.setTextColor(Color.RED);
        tv5.setBackgroundResource(R.drawable.border);
        tv5.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        tbrow0.addView(tv5);
        
        statsTable.addView(tbrow0);
        
        for (int i = 0; i < xi_recup.length; i++) {
            
            TableRow tbrow = new TableRow(context);
            tbrow.setBackgroundResource(R.drawable.border);

            TextView t1v = new TextView(context);
            t1v.setText(noZero(xi_recup[i]));
            t1v.setTextColor(Color.BLACK);
            t1v.setGravity(Gravity.CENTER);
            t1v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t1v);
            
            TextView t2v = new TextView(context);
            t2v.setText(noZero(ni_recup[i]));
            t2v.setTextColor(Color.BLACK);
            t2v.setGravity(Gravity.CENTER);
            t2v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t2v);
            
            TextView t3v = new TextView(context);
            t3v.setText(noZero(xini_recup[i]));
            t3v.setTextColor(Color.BLACK);
            t3v.setGravity(Gravity.CENTER);
            t3v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t3v);
            
            TextView t4v = new TextView(context);
            t4v.setText(noZero(xi2ni_recup[i]));
            t4v.setTextColor(Color.BLACK);
            t4v.setGravity(Gravity.CENTER);
            t4v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t4v);

            TextView t5v = new TextView(context);
            t5v.setText(noZero(up[i]));
            t5v.setTextColor(Color.BLACK);
            t5v.setGravity(Gravity.CENTER);
            t5v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t5v);

            TextView t6v = new TextView(context);
            t6v.setText(noZero(down[i]));
            t6v.setTextColor(Color.BLACK);
            t6v.setGravity(Gravity.CENTER);
            t6v.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.addView(t6v);

            statsTable.addView(tbrow);
        }

    }

    private void switchingDefault(Set<String> charts){
        cursor = 0;
        charts_list = new ArrayList<>();

        for (String s : charts){
            charts_list.add(s);
            Collections.sort(charts_list);
            Collections.reverse(charts_list);
        }

        previous.setVisibility(View.GONE);
        previous.startAnimation(fadeOut);

        switch (charts_list.get(0)){
            case "line":
                lineChart.setVisibility(View.VISIBLE);
                lineChart.startAnimation(fadeIn);
                break;
            case "v_bar":
                barChart.setVisibility(View.VISIBLE);
                barChart.startAnimation(fadeIn);
                break;
            case "h_bar":
                horizontalBarChart.setVisibility(View.VISIBLE);
                horizontalBarChart.startAnimation(fadeIn);
                break;
            case "bubble":
                bubbleChart.setVisibility(View.VISIBLE);
                bubbleChart.startAnimation(fadeIn);
                break;
            case "point":
                scatterChart.setVisibility(View.VISIBLE);
                scatterChart.startAnimation(fadeIn);
                break;
            case "line_point":
                linePointChart.setVisibility(View.VISIBLE);
                linePointChart.startAnimation(fadeIn);
                break;
        }
    }

    private void hideCharts(){

        lineChart.setVisibility(View.GONE);
        lineChart.startAnimation(fadeOut);
        barChart.setVisibility(View.GONE);
        barChart.startAnimation(fadeOut);
        horizontalBarChart.setVisibility(View.GONE);
        horizontalBarChart.startAnimation(fadeOut);
        bubbleChart.setVisibility(View.GONE);
        bubbleChart.startAnimation(fadeOut);
        scatterChart.setVisibility(View.GONE);
        scatterChart.startAnimation(fadeOut);
        linePointChart.setVisibility(View.GONE);
        linePointChart.startAnimation(fadeOut);

        switch (charts_list.get(cursor)){
            case "line":
                lineChart.setVisibility(View.VISIBLE);
                lineChart.startAnimation(fadeIn);
                break;
            case "v_bar":
                barChart.setVisibility(View.VISIBLE);
                barChart.startAnimation(fadeIn);
                break;
            case "h_bar":
                horizontalBarChart.setVisibility(View.VISIBLE);
                horizontalBarChart.startAnimation(fadeIn);
                break;
            case "bubble":
                bubbleChart.setVisibility(View.VISIBLE);
                bubbleChart.startAnimation(fadeIn);
                break;
            case "point":
                scatterChart.setVisibility(View.VISIBLE);
                scatterChart.startAnimation(fadeIn);
                break;
            case "line_point":
                linePointChart.setVisibility(View.VISIBLE);
                linePointChart.startAnimation(fadeIn);
                break;
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

                LayoutInflater factory = LayoutInflater.from(context);

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
