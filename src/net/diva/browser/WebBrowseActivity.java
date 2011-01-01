package net.diva.browser;

import net.diva.browser.service.LoginFailedException;
import net.diva.browser.service.ServiceClient;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebBrowseActivity extends Activity {
	private WebView m_view;
	private ServiceClient m_service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_service = DdN.getServiceClient();

		m_view = new WebView(this);
		m_view.setWebViewClient(new ViewClient());
		m_view.setWebChromeClient(new WebChromeClient());

		Intent intent = getIntent();
		String url = intent.getDataString();
		String cookies = intent.getStringExtra("cookies");
		if (cookies == null)
			new LoginTask().execute(url);
		else
			displayPage(url, cookies);
	}

	private void displayPage(String url, String cookies) {
		CookieManager manager = CookieManager.getInstance();
		manager.setAcceptCookie(true);
		manager.setCookie(DdN.url("/"), cookies);
		CookieSyncManager.getInstance().sync();

		m_view.loadUrl(url);
		setContentView(m_view);
	}

	private class ViewClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			m_service.access();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (m_service.isLogin())
				return super.shouldOverrideUrlLoading(view, url);

			new LoginTask().execute(url);
			return true;
		}
	}

	private class LoginTask extends AsyncTask<String, Void, String[]> {
		@Override
		protected void onPreExecute() {
			setContentView(R.layout.login_waiting);
		}

		@Override
		protected String[] doInBackground(String... url) {
			try {
				m_service.login();
				return new String[] { url[0], m_service.cookies() };
			}
			catch (LoginFailedException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String[] args) {
			displayPage(args[0], args[1]);
		}
	}
}
