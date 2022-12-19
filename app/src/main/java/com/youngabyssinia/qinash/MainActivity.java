package com.youngabyssinia.qinash;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import static android.Manifest.permission.ACCESS_FINE_LOCATION;
//import static android.Manifest.permission.CAMERA;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity {
    private ValueCallback<Uri[]> fileChooserCallback;
    private LocationManager locationManager;
    FusedLocationProviderClient fusedLocationClient;
    AlertDialog.Builder builder;
    String geolocationOrigin;
    GeolocationPermissions.Callback geolocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        builder = new AlertDialog.Builder(this);
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.custom_layout,
                        null);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();

        Uri home;
//        if(!DetectConnection.checkInternetConnection(MainActivity.this)){
//            home = Uri.parse("file:///android_asset/404.html");
//        }
//        else {
        home = Uri.parse("https://abyssiniatoday.com");
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
        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_LONG);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dialog.cancel();
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
                String perm = Manifest.permission.ACCESS_FINE_LOCATION;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                        ContextCompat.checkSelfPermission(MainActivity.this, perm) == PackageManager.PERMISSION_GRANTED) {
                    LocationServices services = null;
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        callback.invoke(origin, true, false);
                    } else {
                        //isGPSEnabled = false;
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(10000 / 2);

                        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();

                        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
                        locationSettingsRequestBuilder.setAlwaysShow(true);

                        SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);
                        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
                        task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                            @Override
                            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                Toast.makeText(MainActivity.this, "location turned on", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    // callback.invoke(origin, true, false);
                } else {
                    if (!EasyPermissions.hasPermissions(MainActivity.this, perm)) {
                        // ask the user for permission
                        EasyPermissions.requestPermissions(MainActivity.this, "hello", 11, new String[]{perm});

                        // we will use these when user responds
                        geolocationOrigin = origin;
                        geolocationCallback = callback;
                    }
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
        if (requestCode == 11) {
            EasyPermissions.onRequestPermissionsResult(11, permissions, grantResults, this);
            geolocationCallback.invoke(geolocationOrigin, true, false);
            Toast.makeText(MainActivity.this, "location permission given", Toast.LENGTH_LONG);
        }
    }
}