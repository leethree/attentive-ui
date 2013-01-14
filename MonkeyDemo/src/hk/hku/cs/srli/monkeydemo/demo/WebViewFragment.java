package hk.hku.cs.srli.monkeydemo.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import hk.hku.cs.srli.monkeydemo.R;

public class WebViewFragment extends DemoFragmentBase {
    
    private static final String URL = "https://www.github.com/404";
    
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        webView = (WebView) rootView.findViewById(R.id.webview);
        webView.loadUrl(URL);
        return rootView;
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_webview;
    }
    
}
