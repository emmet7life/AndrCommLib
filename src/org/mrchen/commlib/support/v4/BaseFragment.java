package org.mrchen.commlib.support.v4;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class BaseFragment extends Fragment {

	protected String TAG = "BaseFragment";

	protected boolean isUIViewLoading;
	protected boolean isUIViewLoadCompleted;
	protected View mContentView;

	// 自定义的一个onKeyDown方法
	protected boolean onKeyDown(int keyCode) {
		return false;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.e(TAG, getClass().getSimpleName() + " onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, getClass().getSimpleName() + " onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, getClass().getSimpleName() + " onCreateView");
		if (mContentView != null) {
			ViewParent parent = mContentView.getParent();
			if (parent != null) {
				((ViewGroup) parent).removeView(mContentView);
			}
		}
		return mContentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.e(TAG, getClass().getSimpleName() + " onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.e(TAG, getClass().getSimpleName() + " onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.e(TAG, getClass().getSimpleName() + " onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.e(TAG, getClass().getSimpleName() + " onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.e(TAG, getClass().getSimpleName() + " onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.e(TAG, getClass().getSimpleName() + " onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, getClass().getSimpleName() + " onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.e(TAG, getClass().getSimpleName() + " onDetach");
	}
}
