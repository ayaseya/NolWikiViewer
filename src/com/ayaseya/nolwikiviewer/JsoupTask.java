package com.ayaseya.nolwikiviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

public class JsoupTask extends AsyncTask<String, Void, String> {

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

	private Document document;
	private Activity activity;
	private Context context;
	private ProgressDialog loading;
	private String file_name;
	private String path;

	// コンストラクタ
	public JsoupTask(Activity activity, Context context, ProgressDialog loading) {
		super();
		this.activity = activity;
		this.context = context;
		this.loading = loading;
	}

	// バックグラウンドで実行する処理
	// returnでonPostExecute()へデータを受け渡す
	@Override
	protected String doInBackground(String... params) {
		// 引数をファイル名に設定する(例：信On入門)
		file_name = params[0];

		// ファイル名をエンコードする
		// 信On入門→%BF%AEOn%C6%FE%CC%E7
		try {
			path = URLEncoder.encode(file_name, "EUC-JP");
			Log.v("Test", "Path=" + path);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		// ファイル名とURLを文字列結合
		String url = "http://ohmynobu.net/index.php?" + path;
		Log.v("Test", "URL=" + url);
		
		// 指定したページをJsoupでスクレイピングする
		// http://ja.wikipedia.org/wiki/%E3%82%A6%E3%82%A7%E3%83%96%E3%82%B9%E3%82%AF%E3%83%AC%E3%82%A4%E3%83%94%E3%83%B3%E3%82%B0
		try {
			document = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// div id=bodyの中身を取得する
		Element body = document.getElementById("body");
		if (body.getElementsByTag("img").size() != 0) {
			// body内のimgタグを取得する
			Elements imgTag = body.getElementsByTag("img");
			for (Element img : imgTag) {
				img.remove();// 拡張for文でbody内のimgタグを削除する
			}
		}
		String result = body.toString();

		return result;

	}

	// バッググラウンドで実行した処理が終了したら実行される
	@Override
	protected void onPostExecute(String result) {

		// metaタグは"で文字列が終了したと認識させないようにするため、
		// "の前に\(エスケープシークエンス)を記述する
		String html = "<html><head><title></title>"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
				+ "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"file:///android_asset/pukiwiki.css\" charset=\"Shift_JIS\" />"
				+ "</head><body>"
				+ result + "</body></html>";
		WebView webview = (WebView) activity.findViewById(R.id.webView);

		int comment_separate = file_name.indexOf("/");
		if (comment_separate != -1) {// コメントページの場合の処理
			try {
				file_name = file_name.substring(comment_separate + 1);
				FileOutputStream fos = new FileOutputStream(
						new File(context.getFilesDir().getPath() + "/comment/" + file_name + ".html"));

				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				osw.append(html);
				PrintWriter writer = new PrintWriter(osw);
				writer.close();

			} catch (Exception e) {
				Log.v("Test", "Error=" + e);
			}
			webview.loadUrl("file://" + context.getFilesDir().getPath() + "/comment/" + file_name + ".html");

		} else {// 通常のページの処理
			try {
				FileOutputStream fos = context.openFileOutput(file_name + ".html", Context.MODE_PRIVATE);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				osw.append(html);
				PrintWriter writer = new PrintWriter(osw);
				writer.close();

			} catch (Exception e) {
				Log.v("Test", "Error=" + e);
			}
			webview.loadUrl("file://" + context.getFilesDir().getPath() + "/" + file_name + ".html");
		}
		// 通信中ダイアログの表示を消す
		loading.dismiss();

	}
}
