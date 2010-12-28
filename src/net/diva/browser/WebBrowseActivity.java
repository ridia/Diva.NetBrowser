package net.diva.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebBrowseActivity extends Activity {
	private WebView m_view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_view = new WebView(this);
		m_view.setWebViewClient(new WebViewClient());
		m_view.setWebChromeClient(new WebChromeClient());
		setContentView(m_view);

		Intent intent = getIntent();
		if (intent == null)
			return;

		CookieManager manager = CookieManager.getInstance();
		manager.setAcceptCookie(true);
		manager.setCookie(DdN.url("/"), intent.getStringExtra("cookies"));
		CookieSyncManager.getInstance().sync();

		m_view.loadUrl(intent.getDataString());
	}
}
