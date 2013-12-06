package org.mrchen.commlib.helper;

import android.view.View;
import android.view.ViewGroup;

public class ViewHelper {

	/**
	 * Find the topmost child under the given point within the parent view's
	 * coordinate system.
	 * 
	 * @param x
	 *            X position to test in the parent's coordinate system
	 * @param y
	 *            Y position to test in the parent's coordinate system
	 * @return The topmost child view under (x, y) or null if none found.
	 */
	public static View findTopChildUnder(ViewGroup mParentView, int x, int y) {
		final int childCount = mParentView.getChildCount();
		for (int i = childCount - 1; i >= 0; i--) {
			final View child = mParentView.getChildAt(i);
			if (x >= child.getLeft() && x < child.getRight() && y >= child.getTop() && y < child.getBottom()) {
				return child;
			}
		}
		return null;
	}

}
