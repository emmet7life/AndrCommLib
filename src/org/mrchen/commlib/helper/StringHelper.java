package org.mrchen.commlib.helper;

public class StringHelper {

	public static String getPackageAndClassName(Class<?> cls) {
		if (cls != null) {
			StringBuilder builder = new StringBuilder();
			builder.append(cls.getPackage().getName());
			builder.append(".");
			builder.append(cls.getSimpleName());
			return builder.toString();
		}
		return null;
	}

}
