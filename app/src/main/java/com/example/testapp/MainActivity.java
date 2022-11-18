package com.example.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends Activity{
    private ValueCallback<Uri[]> fileChooserCallback;
    //TextView tx = findViewById(R.id.textView);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri home;
//        if(!DetectConnection.checkInternetConnection(MainActivity.this)){
//            home = Uri.parse("file:///android_asset/404.html");
//        }
//        else {
            home = Uri.parse("https://betochbord.netlify.app/");
       // }
        WebView view = new WebView(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));


        WebSettings settings = view.getSettings();
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setJavaScriptEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setSupportMultipleWindows(true);


        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                   //view.loadUrl("file:///android_asset/404.html");
                AlertDialog.Builder builder
                        = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("No Internet");

                // set the custom layout
                final View customLayout
                        = getLayoutInflater()
                        .inflate(
                                R.layout.activity_no_internet_dialog,
                                null);
                builder.setView(customLayout);

                AlertDialog dialog
                        = builder.create();
                dialog.show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView vw, WebResourceRequest request) {
                if (request.getUrl().toString().contains(home.getHost())) {
                    vw.loadUrl(request.getUrl().toString());
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                    vw.getContext().startActivity(intent);
                }



                return true;
            }

        });
        view.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }

            @Override
            public boolean onShowFileChooser(WebView vw, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(null);
                }
                fileChooserCallback = filePathCallback;

                Intent selectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                selectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                selectionIntent.setType("*/*");

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, selectionIntent);
                startActivityForResult(chooserIntent, 0);

                return true;
            }

        });
        view.setOnKeyListener((v, keyCode, event) ->
        {
            WebView vw = (WebView) v;
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK && vw.canGoBack()) {
                vw.goBack();

                return true;
            }

            return false;
        });
        view.setDownloadListener((uri, userAgent, contentDisposition, mimetype, contentLength) -> handleURI(uri));
        view.setOnLongClickListener(v -> {
            handleURI(((WebView) v).getHitTestResult().getExtra());

            return true;
        });

        view.loadUrl(home.toString());

        setContentView(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        fileChooserCallback.onReceiveValue(new Uri[]{Uri.parse(intent.getDataString())});
        fileChooserCallback = null;
    }

    private void handleURI(String uri) {
        if (uri != null) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(uri.replaceFirst("^blob:", "")));

            startActivity(i);
        }
    }
}