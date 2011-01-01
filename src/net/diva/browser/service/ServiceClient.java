package net.diva.browser.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.diva.browser.DdN;
import net.diva.browser.MusicInfo;
import net.diva.browser.PlayRecord;
import net.diva.browser.Ranking;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.net.Uri;

public class ServiceClient {
	private static final int LOGIN_DURATION = 3*60*1000;

	private DefaultHttpClient m_client = new DefaultHttpClient();
	private String m_access_code;
	private String m_password;
	private long m_lastAccess;

	public ServiceClient(String access_code, String password) {
		m_access_code = access_code;
		m_password = password;
		m_lastAccess = 0;
	}

	public void access() {
		m_lastAccess = System.currentTimeMillis();
	}

	public boolean isLogin() {
		return System.currentTimeMillis() - m_lastAccess < LOGIN_DURATION;
	}

	public String cookies() {
		if (!isLogin())
			return null;

		StringBuilder builder = new StringBuilder();
		CookieStore store = m_client.getCookieStore();
		for (Cookie cookie: store.getCookies())
			builder.append(String.format("; %s=%s", cookie.getName(), cookie.getValue()));
		return builder.substring(2).toString();
	}

	public PlayRecord login() throws LoginFailedException {
		Uri url = Uri.parse(DdN.URL.resolve("/divanet/login/").toString()).buildUpon().scheme("https").build();
		HttpPost request = new HttpPost(url.toString());
		try {
			request.setHeader("Referer", DdN.URL.toString());
			request.setHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Trident/5.0)");
			request.setHeader("Cache-Control", "no-cache");

			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("accessCode", m_access_code));
			params.add(new BasicNameValuePair("password", m_password));
			request.setEntity(new UrlEncodedFormEntity(params, "US-ASCII"));
			HttpResponse response = m_client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new LoginFailedException();
			access();
			return Parser.parseMenuPage(response.getEntity().getContent());
		}
		catch (Exception e) {
			throw new LoginFailedException(e);
		}
	}

	public void update(PlayRecord record) throws NoLoginException {
		List<MusicInfo> list = new ArrayList<MusicInfo>();
		String path = "/divanet/pv/list/0/0";
		while (path != null) {
			HttpGet request = new HttpGet(DdN.URL.resolve(path));
			try {
				HttpResponse response = m_client.execute(request);
				path = Parser.parseListPage(response.getEntity().getContent(), list);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (list.isEmpty())
			throw new NoLoginException();

		access();
		record.musics = list;
	}

	public void update(MusicInfo music) throws NoLoginException {
		URI url = DdN.URL.resolve(String.format("/divanet/pv/info/%s/0/0", music.id));
		HttpGet request = new HttpGet(url);
		try {
			HttpResponse response = m_client.execute(request);
			access();
			Parser.parseInfoPage(response.getEntity().getContent(), music);
		}
		catch (ParseException e) {
			throw new NoLoginException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void download(String path, OutputStream out) throws IOException {
		URI url = DdN.URL.resolve(path);
		HttpGet request = new HttpGet(url);
		HttpResponse response = m_client.execute(request);
		InputStream in = response.getEntity().getContent();
		byte[] buffer = new byte[1024];
		for (int read; (read = in.read(buffer)) != -1;)
			out.write(buffer, 0, read);
	}

	public List<Ranking> getRankInList() throws IOException, ParseException {
		List<Ranking> list = new ArrayList<Ranking>();
		String path = "/divanet/ranking/list/0";
		while (path != null) {
			HttpGet request = new HttpGet(DdN.URL.resolve(path));
			HttpResponse response = m_client.execute(request);
			path = Parser.parseRankingList(response.getEntity().getContent(), list);
		}

		access();
		return list;
	}
}