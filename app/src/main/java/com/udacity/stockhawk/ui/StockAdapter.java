package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import com.udacity.stockhawk.data.Contract.Quote;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {

        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);
        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);
        holder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        holder.name.setText(cursor.getString(Quote.POSITION_NAME));
        holder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange >= 0) {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
//            holder.listCards.setBackgroundResource(R.drawable.gradient_green);
        } else {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
//            holder.listCards.setBackgroundResource(R.drawable.gradient_red);
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            holder.change.setText(change);
        } else {
            holder.change.setText(percentage);
        }

    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(Bundle bundle);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.listcard)
        CardView listCards;

        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.stock_name)
        TextView name;
        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            Bundle bundle = new Bundle();
            bundle.putString(Quote.COLUMN_SYMBOL,cursor.getString(Quote.POSITION_SYMBOL));
            bundle.putString(Quote.COLUMN_NAME, cursor.getString(Quote.POSITION_NAME));
            bundle.putString(Quote.COLUMN_PRICE,cursor.getString(Quote.POSITION_PRICE));
            bundle.putString(Quote.COLUMN_ABSOLUTE_CHANGE,cursor.getString(Quote.POSITION_ABSOLUTE_CHANGE));
            bundle.putString(Quote.COLUMN_PERCENTAGE_CHANGE,cursor.getString(Quote.POSITION_PERCENTAGE_CHANGE));
            bundle.putString(Quote.COLUMN_AVG_VOLUME, cursor.getString(Quote.POSITION__AVG_VOLUME));
            bundle.putString(Quote.COLUMN_HISTORY, cursor.getString(Quote.POSITION_HISTORY));
            clickHandler.onClick(bundle);

        }


    }

}
