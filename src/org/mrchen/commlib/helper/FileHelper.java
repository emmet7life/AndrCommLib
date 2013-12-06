package org.mrchen.commlib.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

public class FileHelper {

	// public FileHelper() {
	// }

	public static void createFile(String fileName) throws IOException {
		File file = new File(fileName);

		if (!file.exists()) {
			file.createNewFile();
		}

	}

	public static boolean checkDirExist(String dirPath) {
		if (dirPath == null)
			return false;

		File dir = new File(dirPath);
		if (dir.exists()) {
			if (dir.isDirectory())
				return true;
		}

		return false;
	}

	public static File[] getFiles(String dirName) {
		File[] result = null;

		File fileDir = new File(dirName);
		if (fileDir.exists()) {
			if (!fileDir.isDirectory()) {
				fileDir = new File(fileDir.getParent());
			}
			result = fileDir.listFiles();

		}
		return result;
	}

	public static boolean deleteFile(String fileName) {
		boolean result = true;
		File file = new File(fileName);
		if (file.exists()) {
			result = file.delete();
		}

		return result;
	}

	public static byte[] readFile(String fileName) throws IOException {
		byte[] result = null;
		File file = new File(fileName);
		if (file.exists() && file.isFile() && file.length() > 0) {
			result = new byte[(int) file.length()];
			FileInputStream inStream = new FileInputStream(fileName);
			inStream.read(result);

			inStream.close();
		}
		return result;
	}

	public static boolean CopyFile(String sourceFileName, String objFileName) {
		return CopyFile(new File(sourceFileName), new File(objFileName));
	}

	public static boolean CopyFile(File sourceFile, File objFile) {
		return saveFileToLocal(sourceFile, objFile);
	}

	// TODO 临时注释
	// public static boolean[] CopyFile_New(File sourceFile, File objFile) {
	// boolean[] b = new boolean[2];
	// b[0] = saveFileToLocal(sourceFile, objFile);
	// b[1] = saveFileToFtp(objFile);
	// return b;
	// }

	// TODO 临时注释
	// public static boolean CopyFile_from_Http(String url, String objFile) {
	// try {
	// saveFileToFtp(saveToFile(new URL(url), objFile));
	// } catch (IOException e) {
	// e.printStackTrace();
	// return false;
	// }
	// return true;
	// }

	/**
	 * @description：<p>拷贝目录</p>
	 * @param sourceDir
	 *            源目录路径
	 * @param targetDir
	 *            目标路径
	 * @throws IOException
	 * @author：tianjianying
	 * @time：2013-7-8下午04:21:30
	 */
	public static void copyDirectiory(String sourceDir, String targetDir) throws IOException {

		// 新建目标目录

		(new File(targetDir)).mkdirs();

		// 获取源文件夹当下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();

		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());

				CopyFile(sourceFile, targetFile);

			}

			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();

				copyDirectiory(dir1, dir2);
			}
		}

	}

	/**
	 * @description：<p>将一个目录下的文件、子目录拷贝到新的目录</p>
	 * @param sourceDirPath
	 *            源目录路径
	 * @param sourceDirPath
	 *            目标目录
	 * @throws IOException
	 * @author：tianjianying
	 * @time：2013-7-8下午04:16:28
	 */
	public void copyDirToNewDir(String sourceDirPath, String targetDirPath) throws IOException {
		// 创建目标文件夹
		(new File(targetDirPath)).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File[] file = (new File(sourceDirPath)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 复制文件
				CopyFile(file[i], new File(targetDirPath + file[i].getName()));
			}
			if (file[i].isDirectory()) {
				// 复制目录
				String sorceDir = sourceDirPath + File.separator + file[i].getName();
				String targetDir = targetDirPath + File.separator + file[i].getName();
				copyDirectiory(sorceDir, targetDir);
			}
		}
	}

	public static boolean saveFile(String fileName, String data) {
		if (data == null || data == "")
			return false;

		boolean result = false;
		try {
			File file = new File(fileName);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fWriter = new java.io.FileWriter(fileName);
			fWriter.write(data);
			fWriter.flush();
			fWriter.close();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	// TODO 临时注释
	// private static File saveToFile(URL url, String objFile) throws
	// IOException {
	// FileOutputStream fos = null;
	// BufferedInputStream bis = null;
	// HttpURLConnection httpUrl = null;
	// byte[] buf = new byte[1024];
	// int size = 0;
	// // 建立链接
	// httpUrl = (HttpURLConnection) url.openConnection();
	// // 连接指定的资源
	// httpUrl.connect();
	// // 获取网络输入流
	// bis = new BufferedInputStream(httpUrl.getInputStream());
	// // 建立文件
	// fos = new FileOutputStream(objFile);
	//
	// System.out.println("正在获取链接[ " + url.getFile() + "]的内容...\n将其保存为文件[ " +
	// objFile + "] ");
	// // 保存文件
	// while ((size = bis.read(buf)) != -1) {
	// fos.write(buf, 0, size);
	// // ++s;
	// System.out.println(size);
	// // System.out.println(s/1024+ "M ");
	// }
	// fos.close();
	// bis.close();
	// httpUrl.disconnect();
	// return new File(objFile);
	// }

	private static boolean saveFileToLocal(File sourceFile, File objFile) {
		try {
			if (!objFile.exists()) {
				objFile.getParentFile().mkdirs();
				objFile.createNewFile();
			} else {
				objFile.delete();
			}

			if (!sourceFile.exists()) {
				return false;
			}

			int byteread = 0;
			FileInputStream inStream = new FileInputStream(sourceFile);
			FileOutputStream fs = new FileOutputStream(objFile);
			byte[] buffer = new byte[1024];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			inStream.close();

			fs.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 保存文件
	 * 
	 * @param fileName
	 * @param datas
	 * @return
	 */
	public static boolean saveFile(String fileName, List<byte[]> datas) {
		if (datas == null)
			return false;

		boolean result = false;
		try {
			File file = new File(fileName);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fos = new FileOutputStream(fileName);

			for (int i = 0; i < datas.size(); i++) {
				fos.write(datas.get(i));
			}
			fos.flush();
			fos.close();

			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean saveFileToFtp(String fileName, final String data) {
		boolean result = false;
		List<byte[]> datas = new ArrayList<byte[]>();
		try {
			datas.add(data.getBytes());
			// TODO 临时注释
			// if (saveFile(fileName, datas))
			// Context.uploadFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getFileNameNoExtention(String fileName) {
		String result = "";
		if (fileName.lastIndexOf(".") > 0) {
			result = fileName.substring(0, fileName.lastIndexOf(".") - 1);
		} else {
			result = fileName;
		}

		return result;
	}

	public static String getExtentionName(String fileName) {
		String result = "";
		if (fileName.lastIndexOf(".") > 0) {
			result = fileName.substring(fileName.lastIndexOf("."));
		}

		return result.toLowerCase();
	}

	public static void deleteDir(String dirPath) throws IOException {
		File f = new File(dirPath);// 定义文件路径
		if (f.exists() && f.isDirectory()) {// 判断是文件还是目录
			if (f.listFiles().length == 0) {// 若目录下没有文件则直接删除
				f.delete();
			} else {// 若有则把文件放进数组，并判断是否有下级目录
				File delFile[] = f.listFiles();
				int i = f.listFiles().length;
				for (int j = 0; j < i; j++) {
					if (delFile[j].isDirectory()) {
						deleteDir(delFile[j].getAbsolutePath());// 递归调用del方法并取得子目录路径
					}
					delFile[j].delete();// 删除文件
				}
			}
		}
	}

	public static void deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new File(dir, children[i]));
			}
		}
		dir.delete();
	}

	/**
	 * 判断打包文件名长度大小不能超过
	 * 
	 * @param zipFilePath
	 *            打包文件绝对路径
	 * @param fileMaxLenth
	 *            brew 平台下的长度不超过38
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ZipException
	 * @return List<String> 返回超过长度的文件路径
	 */
	public static List<String> checkZipFileNameLength(String zipFilePath, int fileMaxLenth) throws IOException,
			FileNotFoundException, ZipException {
		ZipFile zf = new ZipFile(zipFilePath, "utf-8");// 支持中文
		List<String> fileNames = new ArrayList<String>();
		Enumeration<?> e = zf.getEntries();

		while (e.hasMoreElements()) {
			ZipEntry ze2 = (ZipEntry) e.nextElement();
			String entryName = ze2.getName();
			if (entryName.length() > fileMaxLenth) {
				fileNames.add(entryName);
				break;
			}
		}
		return fileNames;
	}

	/**
	 * 以字符串的方式写文件
	 * 
	 * @param filePath
	 *            文件的绝对路径
	 * @param fileContent
	 *            要写的文件内容
	 * @return 成功返回true，失败返回false
	 */
	public static boolean writeFileByString(String filePath, String fileContent) {
		if (null == filePath || null == fileContent) {
			return false;
		}
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath));
			outputStreamWriter.write(fileContent, 0, fileContent.length());
			outputStreamWriter.flush();
			outputStreamWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 以字符方式写文件
	 * 
	 * @param filePath
	 *            文件的绝对路径
	 * @param fileContent
	 *            要写的内容
	 * @return 成功返回true，失败返回false
	 */
	public static Boolean writeFileByByte(String filePath, byte[] fileContent) {
		try {
			if (null == filePath || null == fileContent) {
				return false;
			}
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			fileOutputStream.write(fileContent);
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// /**
	// * @description：<p>将一个目录下的文件、子目录拷贝到新的目录</p>
	// * @param sourceDirPath
	// * 源目录路径
	// * @param sourceDirPath
	// * 目标目录
	// * @throws IOException
	// * @author：tianjianying
	// * @time：2013-7-8下午04:16:28
	// */
	// public void copyDirToNewDir(String sourceDirPath, String targetDirPath)
	// throws IOException {
	// // 创建目标文件夹
	// (new File(targetDirPath)).mkdirs();
	// // 获取源文件夹当前下的文件或目录
	// File[] file = (new File(sourceDirPath)).listFiles();
	// for (int i = 0; i < file.length; i++) {
	// if (file[i].isFile()) {
	// // 复制文件
	// this.copyFile(file[i], new File(targetDirPath + file[i].getName()));
	// }
	// if (file[i].isDirectory()) {
	// // 复制目录
	// String sorceDir = sourceDirPath + File.separator + file[i].getName();
	// String targetDir = targetDirPath + File.separator + file[i].getName();
	// copyDirectiory(sorceDir, targetDir);
	// }
	// }
	// }

	public void copyFile(File sourcefile, File targetFile) throws IOException {

		// 新建文件输入流并对它进行缓冲
		FileInputStream input = new FileInputStream(sourcefile);
		BufferedInputStream inbuff = new BufferedInputStream(input);

		// 新建文件输出流并对它进行缓冲
		FileOutputStream out = new FileOutputStream(targetFile);
		BufferedOutputStream outbuff = new BufferedOutputStream(out);

		// 缓冲数组
		byte[] b = new byte[1024 * 5];
		int len = 0;
		while ((len = inbuff.read(b)) != -1) {
			outbuff.write(b, 0, len);
		}

		// 刷新此缓冲的输出流
		outbuff.flush();

		// 关闭流
		inbuff.close();
		outbuff.close();
		out.close();
		input.close();

	}

	// /**
	// * @description：<p>拷贝目录</p>
	// * @param sourceDir
	// * 源文件路径
	// * @param targetDir
	// * 目标文件路径
	// * @throws IOException
	// * @author：tianjianying
	// * @time：2013-7-8下午04:21:30
	// */
	// public void copyDirectiory(String sourceDir, String targetDir) throws
	// IOException {
	// // 新建目标目录
	//
	// (new File(targetDir)).mkdirs();
	//
	// // 获取源文件夹当下的文件或目录
	// File[] file = (new File(sourceDir)).listFiles();
	//
	// for (int i = 0; i < file.length; i++) {
	// if (file[i].isFile()) {
	// // 源文件
	// File sourceFile = file[i];
	// // 目标文件
	// File targetFile = new File(new File(targetDir).getAbsolutePath() +
	// File.separator + file[i].getName());
	//
	// copyFile(sourceFile, targetFile);
	//
	// }
	//
	// if (file[i].isDirectory()) {
	// // 准备复制的源文件夹
	// String dir1 = sourceDir + file[i].getName();
	// // 准备复制的目标文件夹
	// String dir2 = targetDir + "/" + file[i].getName();
	//
	// copyDirectiory(dir1, dir2);
	// }
	// }
	//
	// }

}
