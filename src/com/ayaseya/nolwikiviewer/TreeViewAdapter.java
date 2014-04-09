package com.ayaseya.nolwikiviewer;

import java.io.File;
import java.util.Set;

import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * checkboxes and simple item description.
 * 
 */
class TreeViewAdapter extends AbstractTreeViewAdapter<Long> {

	private String[] names;
	private WebView webview;
	private Activity activity;
	private ProgressDialog loading;
	private Context context;
	private JsoupTask Jsoup;

	//    private final Set<Long> selected;
	//
	//    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
	//        @Override
	//        public void onCheckedChanged(final CompoundButton buttonView,
	//                final boolean isChecked) {
	//            final Long id = (Long) buttonView.getTag();
	//            changeSelected(isChecked, id);
	//        }
	//
	//    };
	//
	//    private void changeSelected(final boolean isChecked, final Long id) {
	//        if (isChecked) {
	//            selected.add(id);
	//        } else {
	//            selected.remove(id);
	//        }
	//    }

	public TreeViewAdapter(final NolWikiViewerActivity treeViewListDemo,
			final Set<Long> selected,
			final TreeStateManager<Long> treeStateManager,
			final int numberOfLevels,
			Context context,
			Activity activity) {
		super(treeViewListDemo, treeStateManager, numberOfLevels);
		names = context.getResources().getStringArray(R.array.menu_name);
		this.context = context;
		this.activity = activity;
		//        this.selected = selected;
	}

	private String getDescription(final long id) {
		//        final Integer[] hierarchy = getManager().getHierarchyDescription(id);
		//        return "Node " + id + Arrays.asList(hierarchy);
		return String.valueOf(id);
	}

	@Override
	public View getNewChildView(final TreeNodeInfo<Long> treeNodeInfo) {
		final LinearLayout viewLayout = (LinearLayout) getActivity()
				.getLayoutInflater().inflate(R.layout.treeview_list_item, null);
		return updateView(viewLayout, treeNodeInfo);
	}

	@Override
	public LinearLayout updateView(final View view,
			final TreeNodeInfo<Long> treeNodeInfo) {
		final LinearLayout viewLayout = (LinearLayout) view;
		final TextView descriptionView = (TextView) viewLayout
				.findViewById(R.id.demo_list_item_description);
		//        final TextView levelView = (TextView) viewLayout
		//                .findViewById(R.id.demo_list_item_level);

		//		descriptionView.setText("<" + getDescription(treeNodeInfo.getId()) + ">");
		int index = Integer.parseInt(getDescription(treeNodeInfo.getId()));
		String str = names[index];
		int separate = str.indexOf(":");

		str = str.substring(separate + 1);
		descriptionView.setText(str);

		//        levelView.setText(Integer.toString(treeNodeInfo.getLevel()));
		//        final CheckBox box = (CheckBox) viewLayout
		//                .findViewById(R.id.demo_list_checkbox);
		//        box.setTag(treeNodeInfo.getId());
		//        if (treeNodeInfo.isWithChildren()) {
		//            box.setVisibility(View.GONE);
		//        } else {
		//            box.setVisibility(View.VISIBLE);
		//            box.setChecked(selected.contains(treeNodeInfo.getId()));
		//        }
		//        box.setOnCheckedChangeListener(onCheckedChange);
		return viewLayout;
	}

	@Override
	public void handleItemClick(final View view, final Object id) {
		final Long longId = (Long) id;

		final TreeNodeInfo<Long> info = getManager().getNodeInfo(longId);

		if (info.isWithChildren()) {
			super.handleItemClick(view, id);
		} else {

			// ダイアログの設定
			loading = new ProgressDialog(context);
			loading.setTitle(context.getString(R.string.transmitting));
			loading.setMessage(context.getString(R.string.wait));
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			loading.setCancelable(false);

			//			loading.show();

			int index = Integer.parseInt(getDescription(longId));
			String file_name = names[index];
			int separate = file_name.indexOf(":");
			file_name = file_name.substring(separate + 1);

			File file = context.getFileStreamPath(file_name + ".html");

			webview = (WebView) activity.findViewById(R.id.webView);

			// キャッシュが存在するか確認し存在したらリンク先へ移動
			// 存在しなかった場合はキャッシュを作成するためhtmlの読み込みと保存処理
			if (file.exists()) {

				webview.loadUrl("file://" + activity.getFilesDir().getPath() + "/" + file_name + ".html");

			} else {
//				Toast.makeText(context, "ファイルが見つかりませんでした", Toast.LENGTH_SHORT).show();
				loading.show();
				Jsoup = new JsoupTask(activity, context, loading);
				Jsoup.execute(file_name);

			}

			//			Log.v("Test", "http://ohmynobu.net/index.php?" + file_name);

			//            final ViewGroup vg = (ViewGroup) view;
			//            final CheckBox cb = (CheckBox) vg
			//                    .findViewById(R.id.demo_list_checkbox);
			//            cb.performClick();
		}
	}

	@Override
	public long getItemId(final int position) {
		return getTreeId(position);
	}

}
