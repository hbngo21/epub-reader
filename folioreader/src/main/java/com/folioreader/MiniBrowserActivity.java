//package com.folioreader;
//import android.os.Bundle;
//
//
//import androidx.fragment.app.FragmentActivity;
//
//import com.folioreader.ui.fragment.DictionaryFragment;
//
//public class MiniBrowserActivity extends FragmentActivity {
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//
////        super.onCreate(savedInstanceState);
////        if (savedInstanceState == null){
////            getSupportFragmentManager().beginTransaction()
////                    .add(android.R.id.content, new DictionaryFragment()).commit();}
//    }
//}


package com.folioreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MiniBrowserActivity extends AppCompatActivity {

    private WebView webView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wv);

        this.webView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        this.webView.setWebViewClient(new WebViewClient());

        Intent intent=getIntent();
        String note;
        note = intent.getStringExtra("word");
        String GoogleURL = "https://www.google.com/search?q=" + note;
        webView.loadUrl(GoogleURL);
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            this.webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
