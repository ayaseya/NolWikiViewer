package com.ayaseya.nolwikiviewer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class JsoupTaskMenu extends AsyncTask<String, Void, String> {

	/*
	 * AsyncTask<型1, 型2,型3>
	 *
	 *   型1 … Activityからスレッド処理へ渡したい変数の型
	 *          ※ Activityから呼び出すexecute()の引数の型
	 *          ※ doInBackground()の引数の型
	 *
	 *   型2 … 進捗度合を表示する時に利用したい型
	 *          ※ onProgressUpdate()の引数の型
	 *
	 *   型3 … バックグラウンド処理完了時に受け取る型
	 *          ※ doInBackground()の戻り値の型
	 *          ※ onPostExecute()の引数の型
	 *
	 *   ※ それぞれ不要な場合は、Voidを設定すれば良い
	 */

	private TextView textView;
	private Document document;
	private Activity activity;
	private Context context;

	// コンストラクタ
	public JsoupTaskMenu(Activity activity, Context context) {
		super();
		this.activity = activity;
		this.context = context;

		Log.v("Test", "JsoupTaskMenu()");
	}

	// バックグラウンドで実行する処理
	// returnでonPostExecute()へデータを受け渡す
	@Override
	protected String doInBackground(String... params) {

		String url = params[0];
		try {
			document = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String menubar = document.getElementById("menubar").toString();

		return menubar;
	}

	// バッググラウンドで実行した処理が終了したら実行される
	@Override
	protected void onPostExecute(String result) {

		Pattern pattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+"
				, Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(result);

		StringBuffer str = new StringBuffer();

		while (matcher.find()) {
			str.append(matcher.group());
			str.append("<br>");
		}

		// metaタグは"で文字列が終了したと認識させないようにするため、
		// "の前に\(エスケープシークエンス)を記述する
		String html = "<html><head><title></title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>"
				+ str.toString() + "</body></html>";

		try {

			FileOutputStream fos = context.openFileOutput("menu.html", Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			osw.append(html);
			PrintWriter writer = new PrintWriter(osw);
			writer.close();
		} catch (Exception e) {
			Log.v("Test", "Error");
		}

		WebView webview = (WebView) activity.findViewById(R.id.webView);
		webview.loadUrl("file://" + context.getFilesDir().getPath() + "/menu.html");

		//		textView = (TextView) activity.findViewById(R.id.parseResultView);
		//		textView.setText(result);
	}
}
