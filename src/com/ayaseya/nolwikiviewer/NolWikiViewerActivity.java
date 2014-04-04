package com.ayaseya.nolwikiviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class NolWikiViewerActivity extends Activity {

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private WebView webview;
	public static final String TAG = "Test";
	private JsoupTaskMenu menu;
	private ProgressDialog loading;

	/* ********** ********** ********** ********** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nol_wiki_viewer);

		// ////////////////////////////////////////////////
		// /NavigationDrawer
		// ////////////////////////////////////////////////

		findViewById(R.id.drawer_button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				drawerLayout.closeDrawers();

			}
		});

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				//				Log.v("Test", "onDrawerClosed()");
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				//				Log.v("Test", "onDrawerOpened()");
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				// ActionBarDrawerToggleクラス内の同メソッドにてアイコンのアニメーションの処理をしている。
				// overrideするときは気を付けること。
				super.onDrawerSlide(drawerView, slideOffset);
				//				Log.v("Test", "onDrawerSlide() : " + slideOffset);
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				// 表示済み、閉じ済みの状態：0
				// ドラッグ中状態:1
				// ドラッグを放した後のアニメーション中：2
				//				Log.v("Test", "onDrawerStateChanged()  new state : " + newState);
			}
		};

		drawerLayout.setDrawerListener(drawerToggle);

		// UpNavigationアイコン(アイコン横の<の部分)を有効に
		// NavigationDrawerではR.drawable.drawerで上書き
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// UpNavigationを有効に
		getActionBar().setHomeButtonEnabled(true);

		// ////////////////////////////////////////////////
		// /WebView
		// ////////////////////////////////////////////////
		webview = (WebView) findViewById(R.id.webView);

		// 初期ページを読み込む
		//		webview.loadUrl("http://ohmynobu.net/index.php");
		//		webview.loadUrl("http://www5a.biglobe.ne.jp/~yu-ayase/sample/");

		//		ローカルファイルを表示させる場合
		//		webview.loadUrl("file:///android_asset/hoge.html");

		// デフォルトではloadUrl()で読み込んだページ内のリンクをクリックすると、
		// 標準のブラウザが起動してしまうため、リンク先のページも
		// WebView内で表示させるためWebViewClientを設定する
		webview.setWebViewClient(client);

		String html = "<html><head><title>Test</title></head><body><h1>Hello, world!</h1></body></html>";

		try {

			FileOutputStream fos = openFileOutput("Test.html", MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			osw.append(html);
			PrintWriter writer = new PrintWriter(osw);
			writer.close();
		} catch (Exception e) {
			//			Log.d("Test", "Error");
		}
		// ファイルアクセスデレクトリの表示
		//		Log.v("Test", "file://" + getFilesDir().getPath() + "/Test.html");

		//		webview.loadUrl("http://ohmynobu.net/index.php");
		webview.loadUrl("file://" + getFilesDir().getPath() + "/Test1.html");

		// ダイアログの設定
		loading = new ProgressDialog(this);
		loading.setTitle(getString(R.string.transmitting));
		loading.setMessage(getString(R.string.wait));
		loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		loading.setCancelable(false);

		loading.show();

		// ////////////////////////////////////////////////
		// /Menuを読み込む
		// ////////////////////////////////////////////////

		menu = new JsoupTaskMenu(this, this, loading);
		menu.execute("http://ohmynobu.net/index.php");

		// ////////////////////////////////////////////////
		// /ExpandableListView
		// ////////////////////////////////////////////////

		ExpandableListView parentView = (ExpandableListView) findViewById(R.id.expandableListView_parent);

		// 親リスト
		//ArrayList<HashMap<階層名(親), 親の名前(カテゴリ名)>> groupData = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, String>> groupData = new ArrayList<HashMap<String, String>>();
		String parentName = "parent";
		// 子リスト
		ArrayList<ArrayList<HashMap<String, String>>> childData = new ArrayList<ArrayList<HashMap<String, String>>>();
		String childName = "child";

		// 孫リスト
		//		ArrayList<ArrayList<HashMap<String, String>>> grandchildData = new ArrayList<ArrayList<HashMap<String, String>>>();
		//		String grandchildName = "grandchild";

		String[] parent = getResources().getStringArray(R.array.parent);

		// 親の数だけ繰り返して親リストにデータを格納する
		for (int i = 0; i < parent.length; i++) {
			HashMap<String, String> group = new HashMap<String, String>();
			String item = parent[i];// xmlで定義した配列から親の名前(カテゴリ名)を取得する
			group.put(parentName, item);// HashMapデータに格納
			groupData.add(group);// HashMapデータを親リストに格納

			String childId = "child_" + i;

			int arrayId = getResources().getIdentifier(childId, "array", getPackageName());// child_0のR.array.idを取得する
			if (arrayId != 0) {//子要素がある場合のみ追加

				String[] child = getResources().getStringArray(arrayId);// child_iの配列要素を取得する

				ArrayList<HashMap<String, String>> childList = new ArrayList<HashMap<String, String>>();
				for (String childItem : child) {// 拡張for文でchild_iの配列要素をすべて取得する
					HashMap<String, String> childHash = new HashMap<String, String>();
					childHash.put(parentName, item);
					childHash.put(childName, childItem);
					childList.add(childHash);
				}
				childData.add(childList);// child_iの配列要素を子リストに格納
			} else {//子要素がない場合は子要素を追加しない
				ArrayList<HashMap<String, String>> childList = new ArrayList<HashMap<String, String>>();
				childData.add(childList);
			}
		}

		// 親リスト、子リストを含んだAdapterを生成
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
				getApplicationContext(), // コンテキスト
				groupData,// 親のリスト
				android.R.layout.simple_expandable_list_item_1,// 親のアイテムを表示に使用するレイアウト
				new String[] { parentName },// 親のレイアウトに表示するデータ
				new int[] { android.R.id.text1 },// データを表示するTextViewのid
				childData,// 子のリスト
				android.R.layout.simple_expandable_list_item_2,// 子のアイテムを表示に使用するレイアウト
				new String[] { childName, parentName },// 子のレイアウトに表示するデータ
				new int[] { android.R.id.text1, android.R.id.text2 });// データを表示するTextViewのid

		// ExpandableListViewにAdapterをセット
		parentView.setAdapter(adapter);

	}

	private WebViewClient client = new WebViewClient() {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//			return super.shouldOverrideUrlLoading(view, url);

			//フルパスで渡されたurlの文字列の一部を削除する
			// file:///data/data/com.ayaseya.nolwikiviewer/files/Test.html
			// ↓
			//        /data/data/com.ayaseya.nolwikiviewer/files/Test.html

			url = url.replaceAll("file://", "");
			File file = new File(url);

			// キャッシュが存在するか確認し存在したらリンク先へ移動
			// 存在しなかった場合はキャッシュを作成するためhtmlの読み込みと保存処理
			if (file.exists()) {
				Toast.makeText(NolWikiViewerActivity.this, "ファイルが見つかりました", Toast.LENGTH_SHORT).show();
				return false;

			} else {
				Toast.makeText(NolWikiViewerActivity.this, "ファイルが見つかりませんでした", Toast.LENGTH_SHORT).show();
			}
			return true;

			//			Uri request = Uri.parse(url);
			//			if (TextUtils.equals(request.getAuthority(), "ohmynobu.net")) {
			//				// リンク先のURLが寄合所と同じホスト名であるか判断する
			//				// 一致したらリンク先への移動を許可する
			//				return false;
			//			}
			//			// 一致しなかった場合はリンク先への移動を許可しない
			//			Toast.makeText(NolWikiViewerActivity.this, "外部サイトへ移動することはできません", Toast.LENGTH_SHORT).show();
			//			return true;
		}

	};

	// 戻る(タッチキー)を押した時の処理
	@Override
	public void onBackPressed() {
		// WebViewで戻るページが存在する時は一つ前に表示したページに戻る
		if (webview.canGoBack()) {
			webview.goBack();
		} else {
			// WebViewで戻るページが存在しない時は親クラスを呼び出しアプリを終了する
			super.onBackPressed();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// ActionBarDrawerToggleにandroid.id.home(up ナビゲーション)を渡す。
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nol_wiki_viewer, menu);
		return true;
	}

}
