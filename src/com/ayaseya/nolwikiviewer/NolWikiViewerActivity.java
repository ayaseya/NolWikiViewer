package com.ayaseya.nolwikiviewer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import net.nend.android.NendAdListener;
import net.nend.android.NendAdView;
import net.nend.android.NendAdView.NendError;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

public class NolWikiViewerActivity extends Activity
		implements OnQueryTextListener, NendAdListener {

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private WebView webview;
	public static final String TAG = "Test";
	private ProgressDialog loading;
	private ProgressDialog refresh_loading;

	private final Set<Long> selected = new HashSet<Long>();
	private TreeViewList treeView;
	private static final int LEVEL_NUMBER = 3;
	private TreeStateManager<Long> manager = null;
	private TreeViewAdapter simpleAdapter;

	private String[] names;
	private JsoupTask Jsoup;
	private File file;
	private AlertDialog finish_dialog;
	private EditText searchEditText;

	/* ********** ********** ********** ********** */

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nol_wiki_viewer);
		Log.v(TAG, "/* ********** ********** ********** ********** */");

		// ////////////////////////////////////////////////
		// /NavigationDrawer
		// ////////////////////////////////////////////////

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
		webview.getSettings().setBuiltInZoomControls(true);

		// ファイルアクセスデレクトリの表示
		//		Log.v("Test", "file://" + getFilesDir().getPath() + "/Test.html");

		//		webview.loadUrl("http://ohmynobu.net/index.php");

		//		webview.loadUrl("file://" + getFilesDir().getPath() + "/Test.html");

		// ダイアログの設定
		loading = new ProgressDialog(this);
		loading.setTitle(getString(R.string.transmitting));
		loading.setMessage(getString(R.string.wait));
		loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		loading.setCancelable(false);

		//		loading.show();

		// ////////////////////////////////////////////////
		// /TestTreeView
		// ////////////////////////////////////////////////

		names = getResources().getStringArray(R.array.menu_name);

		if (savedInstanceState == null) {
			manager = new InMemoryTreeStateManager<Long>();
			final TreeBuilder<Long> treeBuilder = new TreeBuilder<Long>(manager);

			for (int i = 0; i < names.length; i++) {
				treeBuilder.sequentiallyAddNextNode((long) i,
						Integer.parseInt(names[i].substring(0, 1)));
			}

		} else {
			manager = (TreeStateManager<Long>) savedInstanceState.getSerializable("treeManager");
			if (manager == null) {
				manager = new InMemoryTreeStateManager<Long>();
			}

		}

		treeView = (TreeViewList) findViewById(R.id.mainTreeView);
		simpleAdapter = new TreeViewAdapter(this, selected, manager, LEVEL_NUMBER, this, this);
		treeView.setAdapter(simpleAdapter);
		manager.collapseChildren(null);

		File dir = this.getFileStreamPath("comment");

		if (dir.exists()) {
			//			Toast.makeText(NolWikiViewerActivity.this, "フォルダが見つかりました", Toast.LENGTH_SHORT).show();
		} else {
			//			Toast.makeText(NolWikiViewerActivity.this, "フォルダが見つかりませんでした", Toast.LENGTH_SHORT).show();

			new File("file://" + getFilesDir().getPath() + "/comment");
			dir.mkdir();
		}

		//			webview.loadUrl("file:///android_asset/pukiwiki.css");

		LayoutInflater inflater = LayoutInflater.from(NolWikiViewerActivity.this);
		View dialog = inflater.inflate(R.layout.finish_dialog,
				(ViewGroup) findViewById(R.id.finish_dialog));

		NendAdView nendAdView = (NendAdView) dialog.findViewById(R.id.nend);
		nendAdView.setListener(this);
		nendAdView.loadAd();

		((Button) dialog.findViewById(R.id.finish_cancelBtn))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {

						finish_dialog.dismiss();

					}
				});

		((Button) dialog.findViewById(R.id.finish_okBtn))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {

						finish_dialog.dismiss();
						finish();

					}
				});

		finish_dialog = new AlertDialog.Builder(NolWikiViewerActivity.this)
				.setView(dialog)
				.setCancelable(false)
				.create();

	}

	private WebViewClient client = new WebViewClient() {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//			return super.shouldOverrideUrlLoading(view, url);

			// フルパスで渡されたurlの文字列の一部を削除する
			// file:///data/data/com.ayaseya.nolwikiviewer/files/Test.html
			// ↓
			//        /data/data/com.ayaseya.nolwikiviewer/files/Test.html

			String file_name = null;
			String anchor = null;

			String comment_file_name = null;

			Uri request = Uri.parse(url);

			if (TextUtils.equals(request.getAuthority(), "ohmynobu.net")) {
				// リンク先のURLが寄合所と同じホスト名であるか判断する
				// 一致したらリンク先への移動を許可する
				Log.v("Test", "URL=" + url);

				file_name = url.replaceAll("http://ohmynobu.net/index.php\\?", "");// URLの"http://ohmynobu.net/index.php?"を削除
				Log.v("Test", "File_Name=" + file_name);

				int separate = file_name.indexOf("#");
				if (separate != -1) {

					anchor = file_name.substring(separate + 1);
					Log.v("Test", "Anchor_Link=" + anchor);

					file_name = file_name.substring(0, separate);
					Log.v("Test", "separate_File_Name=" + file_name);
				}

				try {
					file_name = URLDecoder.decode(file_name, "EUC-JP");
					Log.v("Test", "file_name=" + file_name);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				int comment_separate = file_name.indexOf("/");
				if (comment_separate != -1 && separate == -1) {
					comment_file_name = file_name.substring(comment_separate + 1);
					//					file = new File("file://" + getFilesDir().getPath() + "/comment/" + comment_file_name + ".html");
					file = new File(getFilesDir().getPath() + "/comment/" + comment_file_name + ".html");

				} else {
					file = getFileStreamPath(file_name + ".html");
				}

				// キャッシュが存在するか確認し存在したらリンク先へ移動
				// 存在しなかった場合はキャッシュを作成するためhtmlの読み込みと保存処理
				if (file.exists()) {
					//					Toast.makeText(NolWikiViewerActivity.this, "ファイルが見つかりました", Toast.LENGTH_SHORT).show();

					if (comment_separate == -1 && separate == -1) {
						Log.v("Test", "リンク先のページへ遷移");
						webview.loadUrl("file://" + getFilesDir().getPath() + "/" + file_name + ".html");
					} else if (comment_separate != -1) {
						Log.v("Test", "コメントページへ遷移 ");
						webview.loadUrl("file://" + getFilesDir().getPath() + "/comment/" + comment_file_name + ".html");
					} else if (separate != -1) {
						Log.v("Test", "リンク先のアンカーリンクへ遷移");
						webview.loadUrl("file://" + getFilesDir().getPath() + "/" + file_name + ".html" + "#" + anchor);
					}

				} else {
					//					Toast.makeText(NolWikiViewerActivity.this, "ファイルが見つかりませんでした", Toast.LENGTH_SHORT).show();
					loading.show();
					Jsoup = new JsoupTask(NolWikiViewerActivity.this, NolWikiViewerActivity.this, loading);
					Log.v("Test", "Jsoup.execute()=" + file_name);
					Jsoup.execute(file_name);
				}

				return true;
			}
			// 一致しなかった場合はリンク先への移動を許可しない
			Toast.makeText(NolWikiViewerActivity.this, "外部サイトへ移動することはできません", Toast.LENGTH_SHORT).show();
			return true;

		}

	};
	private String search_word;

	// 戻る(タッチキー)を押した時の処理
	@Override
	public void onBackPressed() {
		// WebViewで戻るページが存在する時は一つ前に表示したページに戻る
		if (webview.canGoBack()) {
			webview.goBack();
		} else {

			// WebViewで戻るページが存在しない時は確認ダイアログを表示する
			finish_dialog.show();
			// WebViewで戻るページが存在しない時は親クラスを呼び出しアプリを終了する
			//			super.onBackPressed();
		}
	}

	// ////////////////////////////////////////////////
	// /Menu
	// ////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nol_wiki_viewer, menu);

		final String idString = "@*android:drawable/ic_menu_refresh";
		final int id = getResources().getIdentifier(idString, null, null);
		menu.findItem(R.id.menu_refresh).setIcon(id);

		// SearchViewを取得する
		MenuItem searchItem = menu.findItem(R.id.menu_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setOnQueryTextListener(this);

		menu.findItem(R.id.menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.findItem(R.id.menu_close).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}

	// ////////////////////////////////////////////////
	// /OnQueryTextListener
	// ////////////////////////////////////////////////
	@Override
	public boolean onQueryTextSubmit(String query) {
//		Log.v("Test", "onQueryTextSubmit()");

		if (query.equals(search_word)) {
			webview.findNext(true);
		} else {
			search_word = query;
			webview.findAll(query);
		}

		return false;
	}

	@Override
	public boolean onQueryTextChange(String aquery) {
		//		Log.v("Test", "onQueryTextChange()");
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// ActionBarDrawerToggleにandroid.id.home(up ナビゲーション)を渡す。
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		int id = item.getItemId();
		if (id == R.id.menu_search) {

		} else if (id == R.id.menu_refresh) {
			Log.v("Test", "menu_refresh=" + webview.getUrl());

			String url = webview.getUrl();
			Log.v("Test", "URL=" + url);
			if (null != url) {

				String file_name = null;
				String anchor = null;
				String comment_file_name = null;

				file_name = url.replaceAll("file://" + getFilesDir().getPath() + "/", "");// URLの"http://ohmynobu.net/index.php?"を削除
				file_name = file_name.replaceAll(".html", "");
				Log.v("Test", "File_Name=" + file_name);

				int separate = file_name.indexOf("#");
				if (separate != -1) {

					anchor = file_name.substring(separate + 1);
					Log.v("Test", "Anchor_Link=" + anchor);

					file_name = file_name.substring(0, separate);
					Log.v("Test", "separate_File_Name=" + file_name);
				}

				try {
					file_name = URLDecoder.decode(file_name, "UTF-8");
					Log.v("Test", "file_name=" + file_name);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				int comment_separate = file_name.indexOf("/");
				if (comment_separate != -1 && separate == -1) {
					comment_file_name = file_name.substring(comment_separate + 1);
					file = new File(getFilesDir().getPath() + "/comment/" + comment_file_name + ".html");

				} else {
					file = getFileStreamPath(file_name + ".html");
				}

				// ダイアログの設定
				refresh_loading = new ProgressDialog(this);
				refresh_loading.setTitle(getString(R.string.transmitting));
				refresh_loading.setMessage(getString(R.string.wait));
				refresh_loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				refresh_loading.setCancelable(false);

				refresh_loading.show();
				Jsoup = new JsoupTask(NolWikiViewerActivity.this, NolWikiViewerActivity.this, refresh_loading);
				Log.v("Test", "Jsoup.execute()=" + file_name);
				Jsoup.execute(file_name);
			}

		} else if (id == R.id.menu_close) {
			finish_dialog.show();
		}

		return super.onOptionsItemSelected(item);
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
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putSerializable("treeManager", manager);
		super.onSaveInstanceState(outState);
	}

	/** 受信エラー通知 */
	@Override
	public void onFailedToReceiveAd(NendAdView nendAdView) {
		//		Toast.makeText(getApplicationContext(), "onFailedToReceiveAd", Toast.LENGTH_LONG).show();

		NendError nendError = nendAdView.getNendError();
		Log.v(TAG, "NendError=" + nendError.getMessage());

	}

	/** 受信成功通知 */
	@Override
	public void onReceiveAd(NendAdView nendAdView) {
		//		Toast.makeText(getApplicationContext(), "onReceiveAd", Toast.LENGTH_LONG).show();
	}

	/** クリック通知 */
	@Override
	public void onClick(NendAdView nendAdView) {
		//		Toast.makeText(getApplicationContext(), "onClick", Toast.LENGTH_LONG).show();
	}

	/** 復帰通知 */
	@Override
	public void onDismissScreen(NendAdView nendAdView) {
		//		Toast.makeText(getApplicationContext(), "onDismissScreen", Toast.LENGTH_LONG).show();
	}

}
