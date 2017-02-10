package com.intellisys.rexi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * Holds the web view to display the web page.
     */
    private WebView mWebPageWebView;

    /**
     * Holds the progress bar to alert the user that something is loading.
     */
    private ProgressBar mLoadingIndicatorProgressBar;

    /**
     * Holds the broadcast receiver to get notified when the internet connection changes.
     */
    private InternetBroadcastReceiver mInternetBroadcastReceiver;

    /**
     * Holds the text view to show the lack of connectivity.
     */
    private TextView mNoInternetConnectionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    /**
     * Initializes the UI.
     */
    private void initUI() {
        setContentView(R.layout.activity_main);

        if (verifyInternetConnection()) {
            initWebView();
        } else {
            registerReceiver();
            mNoInternetConnectionTextView = (TextView) findViewById(R.id.no_internet_connection_text_view);
            mNoInternetConnectionTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Initializes the WebView.
     */
    private void initWebView() {
        mLoadingIndicatorProgressBar = (ProgressBar) findViewById(R.id.loading_indicator_progress_bar);
        mWebPageWebView = (WebView) findViewById(R.id.web_page_web_view);
        mWebPageWebView.setWebViewClient(new CustomWebViewClient());

        WebSettings webSettings = mWebPageWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        mWebPageWebView.loadUrl(BuildConfig.PAGE_URL);
    }

    /**
     * Verifies the internet connection.
     *
     * @return Whether or not there is internet connection.
     */
    private boolean verifyInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnected() ? true : false;
        return isConnected;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebPageWebView != null && mWebPageWebView.canGoBack()) {
            if (!verifyInternetConnection()) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_internet_connection_message), Toast.LENGTH_SHORT).show();
            } else {
                mWebPageWebView.goBack();
            }

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Registers the internet broadcast.
     */
    private void registerReceiver() {
        mInternetBroadcastReceiver = new InternetBroadcastReceiver();
        registerReceiver(mInternetBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver();
    }

    /**
     * Unregisters the internet broadcast.
     */
    private void unregisterReceiver() {
        if (mInternetBroadcastReceiver != null) {
            unregisterReceiver(mInternetBroadcastReceiver);
        }
    }

    /**
     * Handles the transitions between pages.
     */
    private class CustomWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mLoadingIndicatorProgressBar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mLoadingIndicatorProgressBar.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!verifyInternetConnection()) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_internet_connection_message), Toast.LENGTH_SHORT).show();
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (!verifyInternetConnection()) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_internet_connection_message), Toast.LENGTH_SHORT).show();
                return true;
            }

            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    /**
     * Handles the internet connection change broadcast.
     */
    private class InternetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (verifyInternetConnection()) {
                initWebView();
                mNoInternetConnectionTextView.setVisibility(View.GONE);
            }
        }
    }
}
