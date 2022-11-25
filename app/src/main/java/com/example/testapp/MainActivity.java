package com.example.testapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

//import static android.Manifest.permission.ACCESS_FINE_LOCATION;
//import static android.Manifest.permission.CAMERA;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.security.Permission;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity{
    private ValueCallback<Uri[]> fileChooserCallback;
    private LocationManager locationManager;
    FusedLocationProviderClient fusedLocationClient;
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        view.setWebViewClient(new WebViewClient () {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                   //view.loadUrl("file:///android_asset/404.html");
//                AlertDialog.Builder builder
//                        = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("No Internet");
//
//                // set the custom layout
//                final View customLayout
//                        = getLayoutInflater()
//                        .inflate(
//                                R.layout.activity_no_internet_dialog,
//                                null);
//                builder.setView(customLayout);
//
//                AlertDialog dialog
//                        = builder.create();
//                dialog.show();
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
        view.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());

            }



            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                if(EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    FusedLocationProviderClient fusedLocationProviderClient;

                    callback.invoke("https://betochbord.netlify.app/", true, false);
                }
                else {
                    EasyPermissions.requestPermissions(MainActivity.this,
                            "Location permission required in order to locate your home",
                            11, Manifest.permission.ACCESS_FINE_LOCATION);

                }
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
    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                11);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 11){
            EasyPermissions.onRequestPermissionsResult(11, permissions, grantResults, this);
            Toast.makeText(MainActivity.this, "location permission given", Toast.LENGTH_LONG);
        }
    }
}