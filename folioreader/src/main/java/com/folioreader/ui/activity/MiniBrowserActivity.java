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


package com.folioreader.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.folioreader.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MiniBrowserActivity extends AppCompatActivity {

    private WebView webView = null;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wv);

        this.webView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        this.webView.setWebViewClient(new WebViewClient());

        Intent intent = getIntent();
        String note;
        note = intent.getStringExtra("word");
        String GoogleURL = "https://www.google.com/search?q=" + note;
        webView.loadUrl(GoogleURL);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeScreenshot();
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            this.webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private void takeScreenshot() {

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = getApplicationContext().getExternalFilesDir(null) + "/epubviewer/web.jpg";

            // create bitmap screen capture
            View v1 = this.webView;
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            this.webView.getSettings().setJavaScriptEnabled(false);

            File imageFile = new File(mPath);
            if (!imageFile.exists())
                imageFile.getParentFile().mkdir();

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            Intent intent = new Intent();
            intent.putExtra("bitmap", mPath);

            MiniBrowserActivity.this.setResult(200, intent);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }
    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

}
