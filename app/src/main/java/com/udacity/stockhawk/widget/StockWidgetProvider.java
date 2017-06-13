package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

public class StockWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final String LOG_TAG = StockWidgetProvider.class.getSimpleName();
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update all StockWidgets
        for (int widgetId: appWidgetIds){
            Intent intent = new Intent(context,StockWidgetRemoteViewService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);

            // create Widget
            RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.stock_widget);
            views.setRemoteAdapter(R.id.widget_list_view,intent);

            // Open Main Activity on clicking on Header
            Intent mainIntent = new Intent(context, MainActivity.class);
            PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_heading, mainPendingIntent);

            // Open Graph on List Item click
            //doesn't work untill API is live again
            Intent intentStockGraph = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(intentStockGraph)
                    .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list_view,pendingIntent);
            views.setEmptyView(R.id.widget_list_view, R.id.widget_empty);

            // Update Widget on HomeScreen
            appWidgetManager.updateAppWidget(widgetId,views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Receive Broadcast About Stock Data Update
        super.onReceive(context, intent);
        if (ACTION_DATA_UPDATED.equals(intent.getAction())){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,getClass()));
            // update All Widgets
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_list_view);
        }
    }
}
