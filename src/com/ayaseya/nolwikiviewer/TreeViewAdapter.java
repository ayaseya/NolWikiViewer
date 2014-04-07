package com.ayaseya.nolwikiviewer;

import java.util.Set;

import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * checkboxes and simple item description.
 * 
 */
class TreeViewAdapter extends AbstractTreeViewAdapter<Long> {

	private String[] names;

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
			Context context) {
		super(treeViewListDemo, treeStateManager, numberOfLevels);
		names = context.getResources().getStringArray(R.array.menu_name);
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

			Log.v("Test", "handleItemClick()=" + info);
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