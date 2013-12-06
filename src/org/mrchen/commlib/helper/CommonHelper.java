package org.mrchen.commlib.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class CommonHelper {

	/**
	 * 生成加密后的url
	 * 
	 * @param path
	 * @return
	 */
	public static final String makeEncodeURL(String path) {
		if (path != null && path.startsWith("http://") && path.endsWith(".png")) {
			int promix = path.lastIndexOf('/') + 1;

			// 中文路径需修改
			try {
				path = path.substring(0, promix) + URLEncoder.encode(path.substring(promix), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	/**
	 * 拼接联网地址
	 */
	public static final String compareChapterUrl(String url, String tokenId, int customerType) {
		if (url == null) {
			return null;
		}

		String strUrl = url;

		if (customerType == -1) {
			int index = url.indexOf("tokenid=");
			if (index > 0) {
				int index2 = url.indexOf("&", index);
				if (index2 > 0) {
					strUrl = url.substring(0, index) + url.substring(index2 + 1);
				} else {
					strUrl = url.substring(0, index - 1);
				}
			}
		} else {
			if (tokenId != null) {
				// 丢弃掉地址中的tokenid，拼凑新的tokenid
				int index = url.indexOf("tokenid=");
				if (index > 0) {
					int index2 = url.indexOf("&", index + 1);
					if (index2 > 0) {
						strUrl = url.substring(0, index) + url.substring(index2 + 1);
					} else {
						strUrl = url.substring(0, index - 1);
					}
				}

				if (strUrl.indexOf('?') != -1) {
					strUrl += ("&tokenid=" + tokenId);
				} else {
					strUrl += ("?tokenid=" + tokenId);
				}
			}
		}
		return strUrl;
	}

	/**
	 * 转换转义字符
	 */
	public static final String getEntityRef(String entityRef) {
		if (entityRef == null) {
			return null;
		} else if (entityRef.equals("copy")) {
			return "(c)";
		} else if (entityRef.equals("amp")) {
			return "&";
		} else if (entityRef.equals("lt")) {
			return "<";
		} else if (entityRef.equals("gt")) {
			return ">";
		} else if (entityRef.equals("nbsp")) {
			return " ";
		} else if (entityRef.equals("apos")) {
			return "'";
		} else if (entityRef.equals("quot")) {
			return "\"";
		} else if (entityRef.equals("middot")) {
			return " - ";
		} else if (entityRef.startsWith("#")) {
			// unicode
			int c = (entityRef.charAt(1) == 'x' ? Integer.parseInt(entityRef.substring(2), 16) : Integer
					.parseInt(entityRef.substring(1)));
			return String.valueOf((char) c);
		} else if (entityRef.equals("raquo")) {
			// >>
			return String.valueOf((char) 187);
		} else if (entityRef.equals("laquo")) {
			// <<
			return String.valueOf((char) 171);
		} else if (entityRef.equals("rsaquo")) {
			// >
			return String.valueOf((char) 155);
		} else if (entityRef.equals("lsaquo")) {
			// <
			return String.valueOf((char) 139);
		}
		return "";
	}

	/**
	 * 获取包信息类对象
	 * 
	 * @param context
	 *            上下文对象
	 * @return 返回包信息类对象，可以从中获取到版本号(versionName)，包名(packageName)等信息
	 */
	public static PackageInfo getPackageInfo(Context context) {
		String packName = context.getPackageName();
		PackageManager pm = context.getPackageManager();
		PackageInfo info = null;
		try {
			info = pm.getPackageInfo(packName, PackageManager.GET_PERMISSIONS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} finally {

		}
		return info;
	}

	/**
	 * 获取程序的名称
	 * 
	 * @param context
	 *            上下文对象
	 * @return 返回程序名称
	 */
	public static String getApplicationName(Context context) {
		String appName = null;
		String packName = context.getPackageName();
		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo ai = pm.getApplicationInfo(packName, ApplicationInfo.FLAG_SYSTEM);
			appName = (String) pm.getApplicationLabel(ai);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return appName;
	}

	public static final int NETWORK_DISCONNETED = 0;
	public static final int NETWORK_CONNETED_WIFI = 1;
	public static final int NETWORK_CONNETED_CMNET = 2;
	public static final int NETWORK_CONNETED_CMWAP = 3;
	public static final int NETWORK_AIRPLANE = 4;

	public static int checkNetWorkConnection(Context context) {
		int dataState = CommonHelper.isDataConnection(context);
		if (dataState == 1) {
			// 网络连接通畅
			int type = CommonHelper.getNetworkType(context);
			return type;
		} else if (dataState == 0) {
			// 网络不通
			// Log.d("comic", "--没有接入点可供联网--");
			return NETWORK_DISCONNETED;
		} else if (dataState == -1) {
			// 飞行模式，无网络
			// Log.d("comic", "--当前为飞行模式,无法联网--");
			// 飞行模式的时候仍需要去判断网络情况
			int type = CommonHelper.getNetworkType(context);
			if (type != 0) {
				return type;
			}
			return NETWORK_AIRPLANE;
		}
		return NETWORK_DISCONNETED;
	}

	/**
	 * 网络连接是否通畅
	 */
	public static int isDataConnection(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();

		String Mode_airpln = Settings.System.getString(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
		if (Mode_airpln != null && Mode_airpln.equalsIgnoreCase("1")) {
			return -1;
		}

		if (networkInfo != null && networkInfo.isConnected()) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * wifi是否已经连接上
	 */
	public static boolean isWifiConnection(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean is3GNetwork(Context context) {
		// mobile 3G Data Network
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		if (mobile == State.CONNECTED || mobile == State.CONNECTING)
			return true;
		else
			return false;
	}

	/**
	 * 获取网络连接状态
	 */
	public static int getNetworkType(Context context) {
		// NetworkInfo 有以下方法
		// getDetailedState()：获取详细状态。
		// getExtraInfo()：获取附加信息(3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap)
		// getReason()：获取连接失败的原因。
		// getType()：获取网络类型(一般为移动或Wi-Fi)。
		// getTypeName()：获取网络类型名称(一般取值“WIFI”或“MOBILE”)。
		// isAvailable()：判断该网络是否可用。
		// isConnected()：判断是否已经连接。
		// isConnectedOrConnecting()：判断是否已经连接或正在连接。
		// isFailover()：判断是否连接失败。
		// isRoaming()：判断是否漫游

		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
		if (networkInfo != null) {
			int type = networkInfo.getType();
			State state = networkInfo.getState();

			if (type == ConnectivityManager.TYPE_WIFI && state == State.CONNECTED) {
				// wifi连接通畅
				return NETWORK_CONNETED_WIFI;
			} else if (type == ConnectivityManager.TYPE_MOBILE && state == State.CONNECTED) {
				String extraInfo = networkInfo.getExtraInfo();
				if (extraInfo != null) {
					// 3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap
					if (extraInfo.toLowerCase().endsWith("wap")) {
						return NETWORK_CONNETED_CMWAP;
					} else {
						return NETWORK_CONNETED_CMNET;
					}
				} else {
					return NETWORK_CONNETED_CMNET;
				}
			}
		}
		return NETWORK_DISCONNETED;
	}

	// public static boolean checkCmwapAPN(Context context) {
	// Uri uri_all = Uri.parse("content://telephony/carriers"); // 所有APN的数据URI
	// // Cursor cursor_need = context.getContentResolver().query(uri_all, null,
	// null, null, null);
	// Cursor cursor_need = context.getContentResolver().query(uri_all, null,
	// "current = 1", null, null);
	//
	// if (cursor_need != null) {
	// int count = cursor_need.getCount();
	// for (int j = 0; j < count; ++j) {
	// if (cursor_need.moveToPosition(j)) {
	// String[] names = cursor_need.getColumnNames();
	// for (int i = 0; i < names.length; ++i) {
	// String value =
	// cursor_need.getString(cursor_need.getColumnIndex(names[i]));
	//
	// }
	// }
	// }
	// }
	// return false;
	// }

	public static boolean isWapNetWork(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo == null || networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			// 没有连接 or 为wifi连接
			return false;
		}

		boolean fResult = false;
		ContentResolver localContentResolver = context.getContentResolver();
		Cursor localCursor = localContentResolver.query(Uri.parse("content://telephony/carriers/preferapn"), null,
				null, null, null);

		if (localCursor != null && localCursor.moveToFirst()
		// && (localCursor.moveToNext())
		) {
			String strApn = localCursor.getString(localCursor.getColumnIndex("apn"));

			String strProxy = localCursor.getString(localCursor.getColumnIndex("proxy"));

			String port = localCursor.getString(localCursor.getColumnIndex("port"));

			String strType = localCursor.getString(localCursor.getColumnIndex("type"));

			if (("cmwap".equals(strApn)) && ("10.0.0.172".equals(strProxy)) && ("80".equals(port)) && (strType != null)
			// && (strType.indexOf("default") != -1
			// )
			) {
				fResult = true;
			}
		}

		if (localCursor != null) {
			localCursor.close();
		}
		return fResult;
	}

	/**
	 * 计算listview的实际高度
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			try {
				listItem.measure(0, 0); // 计算子项View 的宽高
			} catch (Exception e) {
				e.printStackTrace();
			}
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount()));
		// params.height = totalHeight
		// + (listView.getDividerHeight() * (listAdapter.getCount() + 1));
		listView.setLayoutParams(params);
	}

	/**
	 * 计算listview的实际高度
	 */
	public static void setGridViewHeightBasedOnChildren(GridView gridView, int childViewHeight, int lines) {
		int totalHeight = childViewHeight * lines;
		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight;
		gridView.setLayoutParams(params);
	}

	// 获取屏幕宽高
	public static String getDisplayMetrics(Context context, int[] screenRect) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		// activity.getWindowManager( ).getDefaultDisplay( ).getMetrics( dm );
		if (screenRect != null) {
			screenRect[0] = dm.widthPixels;
			screenRect[1] = dm.heightPixels;
			// Log.d("comic", "屏幕宽高----："+ screenRect[0] +" * "+ screenRect[1]);
		}
		String softSpec = null;
		if (dm.widthPixels < 320) {
			softSpec = "android_240";
		} else if (dm.widthPixels < 360) {
			softSpec = "android_320";
		} else if (dm.widthPixels < 480) {
			softSpec = "android_360";
		} else {
			softSpec = "android_480";
		}
		return softSpec;
	}

}