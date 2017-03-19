package com.udacity.stockhawk.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.renderer.Renderer;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract.Quote;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class StockDetailFragment extends Fragment implements TabHost.OnTabChangeListener {
    public static final String ARG_CURRENT_TAB = "CURRENT_TAB";
    String stockString;
    String name;
    String price;
    String absoluteChange;
    String percentageChange;
    String history;
    String [] hs;
    String avgVol;
    private String mSelectedTab;
    @BindView(android.R.id.tabhost)
    TabHost mTabHost;
    @BindView(R.id.detailgrid)
    GridLayout detailGrid;
    @BindView(R.id.linechart)
    LineChart mLineChart;
    @BindView(R.id.avg_volume)
    TextView avgVolume;
    @BindView(R.id.stock_name)
    TextView stockNameView;
    @BindView(R.id.price)
    TextView stockPriceView;
    @BindView(R.id.stock_percent_change)
    TextView stockChangeView;

    LineDataSet mLineDataSet = new LineDataSet(new ArrayList<Entry>(),"Values");
    LineData mLineData;
    private CustomMarkerView markerView;
    // Marker views
    TextView markerDate;
    TextView markerPrice;
    LinearLayout markerLayout;

    public StockDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            stockString = arguments.getString(Quote.COLUMN_SYMBOL);
            name = arguments.getString(Quote.COLUMN_NAME);
            price = arguments.getString(Quote.COLUMN_PRICE);
            absoluteChange = arguments.getString(Quote.COLUMN_ABSOLUTE_CHANGE);
            percentageChange = arguments.getString(Quote.COLUMN_PERCENTAGE_CHANGE);
            avgVol = arguments.getString(Quote.COLUMN_AVG_VOLUME);
            history = arguments.getString(Quote.COLUMN_HISTORY);
            hs = history.split("\n");

        }
        if (savedInstanceState == null) {
            mSelectedTab = getString(R.string.stock_detail_tab2);
        } else {
            mSelectedTab = savedInstanceState.getString(ARG_CURRENT_TAB);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stock_detail, container,false);
        ButterKnife.bind(this, rootView);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (activity.getSupportActionBar() != null){
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        activity.supportStartPostponedEnterTransition();
        if (Float.valueOf(absoluteChange) > 0) {
            detailGrid.setBackgroundResource(R.drawable.gradient_green);
        } else {
            detailGrid.setBackgroundResource(R.drawable.gradient_red);
        }

        avgVolume.setText(avgVol);
        stockNameView.setText(name+" ("+stockString+")");
        stockPriceView.setText("$");
        stockPriceView.append(price);
        stockChangeView.setText(percentageChange);
        stockChangeView.append("%");

        graphStyling();

        markerView = new CustomMarkerView(getContext(),R.layout.marker_view_layout);
        mLineChart.setMarkerView(markerView);
        mLineChart.invalidate();
        setupTabs();
        populateGraph(mSelectedTab);
        return rootView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_CURRENT_TAB, mSelectedTab);
        super.onSaveInstanceState(outState);

    }

    private void populateGraph(String tabId){
        Calendar startDateMonth = Calendar.getInstance();
        switch (tabId) {
            case "1":
                startDateMonth.add(Calendar.MONTH,-1);
                break;
            case "6":
                startDateMonth.add(Calendar.MONTH,-6);
                break;
            case "12":
                startDateMonth.add(Calendar.MONTH,-12);
                break;
            default:
                startDateMonth.add(Calendar.MONTH,-6);
                break;

        }

        Date startDate = startDateMonth.getTime();

        ArrayList<String> xVaList = new ArrayList<>();
        mLineDataSet.clear();
        for (int i=0; i<hs.length ; i++) {
            // Date comparison for Graph sorting
            String[] one = hs[i].split(",");
            String dateInMillis = one[0];
            String price = one[1];

            Date date = new Date(Long.valueOf(dateInMillis));
            String newDate =DateFormat.getDateInstance().format(date);
            if (date.after(startDate)){
                mLineDataSet.addEntry(
                        // add Stock entry to yValues
                        new Entry(Float.valueOf(price), i)
                );
                // add date to xValues
                xVaList.add(newDate);
            }
        }

        mLineDataSet.setDrawValues(false);
        mLineDataSet.setDrawFilled(true);
        mLineDataSet.setColor(getResources().getColor(R.color.black));
        mLineDataSet.setCircleColor(getResources().getColor(R.color.colorAccent));

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.setDescription("");
        YAxis yAxis = mLineChart.getAxisLeft();
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        yAxis.setDrawGridLines(false);

        mLineData = new LineData(xVaList, mLineDataSet);
        mLineChart.setData(mLineData);
        mLineData.notifyDataChanged(); // let Data know its DataSet changed
        mLineChart.notifyDataSetChanged(); // let chart know its Data changed
        mLineChart.animateXY(500, 500);
        mLineChart.invalidate();
    }
//
    private class CustomMarkerView extends MarkerView {
        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            markerDate = (TextView) findViewById(R.id.marker_date);
            markerPrice = (TextView) findViewById(R.id.marker_price);
            markerLayout = (LinearLayout) findViewById(R.id.markerLayout);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (e != null) {
                Timber.d("ENTRY............"+e.toString());
                int xIndex = e.getXIndex();
                Timber.d("XINDEX............"+xIndex);
                String val = hs[xIndex].split(",")[0];
                Date date = new Date(Long.valueOf(val));
                String newDate =  DateFormat.getDateInstance().format(date);
                Timber.d("VAL............"+newDate);
                markerDate.setText(newDate);
                markerPrice.setText("$");
                markerPrice.append(String.valueOf(e.getVal()));
            }
        }

        @Override
        public int getXOffset(float xpos) {
            if(xpos < getWidth()){
                return (getWidth()/100);
            } else if (xpos >getWidth()*.9) {
                return -(getWidth());
            }
            else return -getWidth()/2;
        }

        @Override
        public int getYOffset(float ypos) {
            return -getHeight();
        }

}

    private void graphStyling(){
        YAxis yAxis = mLineChart.getAxisLeft();
        XAxis xAxis = mLineChart.getXAxis();

        // Style as Cubical Line Chart
        mLineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        // disable Draw values from chart
        mLineDataSet.setDrawValues(false);
        // disable draw circles
        mLineDataSet.setDrawCircles(true);
        mLineDataSet.setDrawCircleHole(false);
        mLineDataSet.setCircleColor(R.color.black);
        // fill chart with color
		mLineDataSet.setDrawFilled(false);

//        // data border color
        mLineDataSet.setColor(R.color.black);
        // data color transparency level 0-255

        // data fill color
		mLineDataSet.setFillColor(R.color.black);
        mLineDataSet.setFillAlpha(255);
        // disable grid lines
        xAxis.setDrawGridLines(false);
        yAxis.setDrawGridLines(false);
        // remove right side markings
        mLineChart.getAxisRight().setEnabled(false);
        // remove x axis info
//		mLineChart.getXAxis().setDrawAxisLine(false);
        mLineChart.getXAxis().setEnabled(true);
        // remove y axis info
//		mLineChart.getAxisLeft().setDrawAxisLine(false);
        yAxis.setShowOnlyMinMax(true);
        mLineChart.getAxisLeft().setEnabled(true);
//        mLineChart.setDescription();

        mLineChart.getLegend().setEnabled(false);
//		yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        mLineChart.setViewPortOffsets(0,0,0,0);
        mLineChart.setPinchZoom(false);
        mLineChart.setDoubleTapToZoomEnabled(false);

        mLineChart.invalidate();// refresh chart

    }

    @Override
    public void onTabChanged(String tabId) {
        mSelectedTab = tabId;
        populateGraph(tabId);

    }
    private void setupTabs() {
        mTabHost.setup();
        TabHost.TabSpec tabSpec;
        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab1));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab1_label));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab2));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab2_label));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab3));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab3_label));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        mTabHost.setOnTabChangedListener(this);
        if (mSelectedTab.equals(getString(R.string.stock_detail_tab2))) {
            mTabHost.setCurrentTab(1);
        } else if (mSelectedTab.equals(getString(R.string.stock_detail_tab3))) {
            mTabHost.setCurrentTab(2);
        } else if (mSelectedTab.equals(R.string.stock_detail_tab1)){
            mTabHost.setCurrentTab(0);
        } else {
            mTabHost.setCurrentTab(1);
        }
    }

}
