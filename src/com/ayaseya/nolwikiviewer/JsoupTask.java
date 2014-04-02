package com.ayaseya.nolwikiviewer;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

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

	private TextView textView;
	private Document document;
	private Activity activity;

	// コンストラクタ
	public JsoupTask(Activity activity) {
		super();
		this.activity = activity;

		Log.v("Test", "JsoupTask()");
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

		String html = document.title();

		return html;
	}

	// バッググラウンドで実行した処理が終了したら実行される
	@Override
	protected void onPostExecute(String result) {

		//		textView = (TextView) activity.findViewById(R.id.parseResultView);
		//		textView.setText(result);
	}
}
