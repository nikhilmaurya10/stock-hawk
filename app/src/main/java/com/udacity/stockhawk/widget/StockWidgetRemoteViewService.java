package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

public class StockWidgetRemoteViewService extends RemoteViewsService {
    private static final String LOG_TAG = StockWidgetRemoteViewService.class.getSimpleName();
    public StockWidgetRemoteViewService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetItemRemoteView(this.getApplicationContext(),intent);
    }

    class WidgetItemRemoteView implements RemoteViewsService.RemoteViewsFactory{
        Context mContext;
        Cursor mCursor;
        Intent mIntent;

        public WidgetItemRemoteView(Context mContext, Intent mIntent) {
            this.mContext = mContext;
            this.mIntent = mIntent;
        }

        @Override
        public void onCreate() {
            // nothing To DO
        }

        @Override
        public int getCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        @Override
        public void onDataSetChanged() {
            // update Cursor when dataset is changed eg. stock added / removed
            // refer http://stackoverflow.com/a/16076336
            if (mCursor!=null)
                mCursor.close();

            final long pId = Binder.clearCallingIdentity();

            mCursor = getContentResolver().query(
                    Contract.Quote.URI,
                    null,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL
            );

            Binder.restoreCallingIdentity(pId);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            try{
                mCursor.moveToPosition(position);
                int priceChangeColorId;

                // get Stock Quote information
                String stockSymbol = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                String stockName = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_NAME));
                String stockPrice = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                String stockPercentChange = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

                // create List Item for Widget ListView
                RemoteViews listItemRemoteView = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_content);
                listItemRemoteView.setTextViewText(R.id.stock_symbol,stockSymbol);
                listItemRemoteView.setTextViewText(R.id.stock_name,stockName);
                listItemRemoteView.setTextColor(R.id.stock_symbol, getResources().getColor(R.color.grey));
                listItemRemoteView.setTextViewText(R.id.bid_price,"$" + stockPrice);
//                listItemRemoteView.setTextColor(R.id.bid_price, getResources().getColor(R.color.black));



                // if stock price is Up then background of price Change is Green else, Red
                if (Float.valueOf(stockPercentChange)>=0) {
                    priceChangeColorId = R.color.material_green_700;
                    stockPercentChange = "+" + stockPercentChange;
                }

                else {
                    priceChangeColorId = R.color.material_red_700;
                }
                listItemRemoteView.setTextColor(R.id.change,getResources().getColor(priceChangeColorId));
                listItemRemoteView.setTextViewText(R.id.change,stockPercentChange + "%");
//                 set Onclick Item Intent
                final Intent onClickItemIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(Contract.Quote.COLUMN_SYMBOL,stockSymbol);
                bundle.putString(Contract.Quote.COLUMN_NAME, mCursor.getString(Contract.Quote.POSITION_NAME));
                bundle.putString(Contract.Quote.COLUMN_PRICE,stockPrice);
                bundle.putString(Contract.Quote.COLUMN_ABSOLUTE_CHANGE,mCursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
                bundle.putString(Contract.Quote.COLUMN_PERCENTAGE_CHANGE,stockPercentChange);
                bundle.putString(Contract.Quote.COLUMN_AVG_VOLUME, mCursor.getString(Contract.Quote.POSITION__AVG_VOLUME));
                bundle.putString(Contract.Quote.COLUMN_HISTORY, mCursor.getString(Contract.Quote.POSITION_HISTORY));
                onClickItemIntent.putExtras(bundle);
                listItemRemoteView.setOnClickFillInIntent(R.id.widget_list_item_stock,onClickItemIntent);
                return listItemRemoteView;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(mCursor.getColumnIndex(Contract.Quote._ID));
        }

        @Override
        public void onDestroy() {
            if (mCursor!=null)
                mCursor.close();
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
