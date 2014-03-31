package com.ayaseya.nolwikiviewer;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class NolWikiViewerActivity extends Activity {

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nol_wiki_viewer);

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
				Log.v("Test", "onDrawerClosed()");
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				Log.v("Test", "onDrawerOpened()");
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				// ActionBarDrawerToggleクラス内の同メソッドにてアイコンのアニメーションの処理をしている。
				// overrideするときは気を付けること。
				super.onDrawerSlide(drawerView, slideOffset);
				Log.v("Test", "onDrawerSlide() : " + slideOffset);
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				// 表示済み、閉じ済みの状態：0
				// ドラッグ中状態:1
				// ドラッグを放した後のアニメーション中：2
				Log.v("Test", "onDrawerStateChanged()  new state : " + newState);
			}
		};

		drawerLayout.setDrawerListener(drawerToggle);

		// UpNavigationアイコン(アイコン横の<の部分)を有効に
		// NavigationDrawerではR.drawable.drawerで上書き
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// UpNavigationを有効に
		getActionBar().setHomeButtonEnabled(true);

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
