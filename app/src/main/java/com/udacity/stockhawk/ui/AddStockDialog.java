package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;


public class AddStockDialog extends DialogFragment {
    private String ARG_ERROR_TV_VISIBILITY = "ERROR_V";
    private String ARG_ERROR_TV_CONTENT = "ERROR";
    private String ARG_PROGRESS_VISIBILITY = "PROGRESS_V";
    private String ARG_POSITIVE_BUTTON_ENABLED = "POSITIVE_BUTTON";
    volatile boolean running;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_stock)
    EditText stock;
    @BindView(R.id.progess_circle)
    ProgressBar progressBar;
    @BindView(R.id.error_textView)
    TextView errorTV;
    String stockSymbol="";
    Boolean stockVerified=false;
    AlertDialog dialog;
    Context mContext;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        running =true;
        mContext = getActivity();
        this.setRetainInstance(true);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);
        dialog = new AlertDialog.Builder(getActivity())
                .setView(custom)
                .setCancelable(false)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_add, null) //Set to null. We override the onclick
                .setNegativeButton(R.string.dialog_cancel, null)
//                .setNeutralButton(R.string.dialog_verify,null)
                .create();

        stock.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                stock.setTextColor(getResources().getColor(R.color.black));
                errorTV.setVisibility(View.GONE);
                if (TextUtils.isEmpty(s)) {
                    // Disable ok button
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });




        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                final Button buttonC = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setBackgroundResource(R.drawable.touch_selector);
                buttonC.setBackgroundResource(R.drawable.touch_selector);
                if (savedInstanceState == null) button.setEnabled(false);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String stockString = stock.getText().toString();
                        if (PrefUtils.isStockPresent(mContext,stockString)) {
                            button.setEnabled(false);
                            showError(getString(R.string.stock_present));
                        } else if (!PrefUtils.networkUp(mContext)) {
                            Toast.makeText(mContext,getString(R.string.check_internet),Toast.LENGTH_SHORT).show();
                            showError(getString(R.string.no_internet));
                            stock.setTextColor(getResources().getColor(R.color.black));
                            button.setEnabled(true);
                        } else if (!stockString.matches("[a-zA-Z]+")) {
                            button.setEnabled(false);
                            showError(getString(R.string.only_letters));
                        } else new CheckStockTask().execute(stockString);


                    }
                });
            }
        });
        progressBar.setVisibility(View.GONE);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    private void addStock(String s) {
        Activity parent = getActivity();
        if (parent instanceof MainActivity) {
            ((MainActivity) parent).addStock(s);
        }
        dismissAllowingStateLoss();
    }


    public class CheckStockTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
            stock.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            errorTV.setVisibility(View.GONE);

        }

        @Override
        protected Void doInBackground(String... params) {
            Stock sStock=null;
            stockVerified = false;
            try {
                sStock = YahooFinance.get(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (sStock != null) {
                StockQuote quote = sStock.getQuote();
                if (quote.getPrice() != null) {
                    stockSymbol="";
                    stockSymbol += params[0];
                    stockVerified =  true;
                } else stockVerified = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!running) return;
            if (stockVerified && stockSymbol!="") {
                dialog.dismiss();
                addStock(stockSymbol);
                Toast.makeText(mContext,getString(R.string.stock_added),Toast.LENGTH_SHORT).show();



            } else {
                //Dismiss once everything is OK.
//                Toast.makeText(mContext,"Can't verify stock. Please enter correct name.", Toast.LENGTH_LONG).show();

                showError(getString(R.string.incorrect_symbol));

            }
            super.onPostExecute(aVoid);
        }
    }

    public void showError(String errorString) {
        progressBar.setVisibility(View.GONE);
        stock.setVisibility(View.VISIBLE);
        errorTV.setVisibility(View.VISIBLE);
        errorTV.setText(errorString);
        stock.setTextColor(getResources().getColor(R.color.grey));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        stock.setVisibility(View.GONE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        errorTV.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();

        if(progressBar.getParent()!=null)
            ((ViewGroup)progressBar.getParent()).removeView(progressBar);
        ((ViewGroup)errorTV.getParent()).removeView(errorTV);

        ((ViewGroup)stock.getParent()).removeView(stock);
        running = false;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_ERROR_TV_CONTENT,errorTV.getText().toString());
        outState.putInt(ARG_ERROR_TV_VISIBILITY,errorTV.getVisibility());
        outState.putInt(ARG_PROGRESS_VISIBILITY,progressBar.getVisibility());
        outState.putBoolean(ARG_POSITIVE_BUTTON_ENABLED,dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        running =true;
        if(savedInstanceState!=null) {
            if (savedInstanceState.getInt(ARG_PROGRESS_VISIBILITY) == 8) {
                progressBar.setVisibility(View.GONE);
                stock.setEnabled(true);
            } else if (savedInstanceState.getInt(ARG_PROGRESS_VISIBILITY) == 0) {
                showProgressBar();
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(savedInstanceState.getBoolean(ARG_POSITIVE_BUTTON_ENABLED));
            if (savedInstanceState.getInt(ARG_ERROR_TV_VISIBILITY) == 8) {
                errorTV.setVisibility(View.GONE);
            } else if (savedInstanceState.getInt(ARG_ERROR_TV_VISIBILITY) == 0) {
                showError(savedInstanceState.getString(ARG_ERROR_TV_CONTENT));
            }

        }
    }
}
