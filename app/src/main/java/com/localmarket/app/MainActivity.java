package com.localmarket.app;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout offlineLayout;
    private Button retryButton;

    private String lastUrl = "";
    private boolean isError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        offlineLayout = findViewById(R.id.offlineLayout);
        retryButton = findViewById(R.id.retryButton);

        // WebView સેટિંગ્સ
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                // WhatsApp, Call અને Email લિંક્સ સીધી જે-તે એપમાં ખોલવા માટે
                if (url.startsWith("whatsapp:") || url.contains("wa.me") || url.contains("api.whatsapp.com")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "WhatsApp ઈન્સ્ટોલ નથી", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (url.startsWith("tel:") || url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // ખાલી પેજ સિવાયનું સાચું URL સેવ રાખો
                if (url != null && !url.equals("about:blank") && !url.startsWith("chrome-error://")) {
                    lastUrl = url;
                }
                isError = false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    isError = true;
                    webView.setVisibility(View.GONE);
                    offlineLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isError && url != null && !url.equals("about:blank") && !url.startsWith("chrome-error://")) {
                    offlineLayout.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }
            }
        });

        // Retry બટન પર ક્લિક થાય ત્યારે વ્હાઈટ સ્ક્રીન વગર પેજ લોડ કરવા માટે
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isError = false;
                offlineLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);

                if (lastUrl != null && !lastUrl.isEmpty()) {
                    webView.loadUrl(lastUrl);
                } else {
                    webView.loadUrl(getString(R.string.website_url));
                }
            }
        });

        // પહેલીવાર એપ ખુલે ત્યારે વેબસાઈટ લોડ કરો
        String homeUrl = getString(R.string.website_url);
        lastUrl = homeUrl;
        webView.loadUrl(homeUrl);
    }

    // મોબાઈલનું બેક બટન દબાવતા એપ બંધ થવાને બદલે પાછલા પેજ પર જવા માટે
    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

