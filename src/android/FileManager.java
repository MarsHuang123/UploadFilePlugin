package upload;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FileManager {
	public static final String ENCODING = "UTF-8";
	private static FileManager instance;
	private static final String fileSuffix = ".m4a";
	private String fileName;
	private String TAG = "FileManager";

	public static FileManager getInstance() {
		if (instance == null) {
			instance = new FileManager();
		}
		return instance;
	}

	private FileManager() {
	}

	/**
	 * Get video's stream.
	 * @return
	 */
	public BufferedInputStream getFileStream() {

		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(getFileFullPath()), 1204);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		File f = android.os.Environment.getExternalStorageDirectory();

		return f.getPath() + "/caches/DataForder/";
	}

	public String getFileFullPath() {
		File f = android.os.Environment.getExternalStorageDirectory();

		return f.getPath() + "/caches/DataForder/" + fileName + fileSuffix;
	}

	public String getFileUplaodedFullPath() {
		File f = android.os.Environment.getExternalStorageDirectory();

		return f.getPath() + "/caches/FinishDataForder/" + fileName + fileSuffix;
	}
	public File getFileDirectory(){
		File myFile = new File(getFilePath());
		return myFile;
	}
	public File getFile(String fileName) {
		//File SDFile = android.os.Environment.getExternalStorageDirectory();

		File myFile = new File(getFilePath() + fileName + fileSuffix);
		return myFile;
	}

	public boolean deleteFileFromSDCard(String fileFullPath) throws IOException {
		File file = new File(fileFullPath);
		Log.e(TAG, "Delete file....." + fileFullPath);
		return file.delete();
	}
	public void copyFile() {
		String oldPath = getFileFullPath();
		String newPath= getFileUplaodedFullPath();
		Log.e(TAG, "Copy file start.....");
		Log.e(TAG, "oldPath is....." + oldPath);
		try {
			long byteSum = 0;
			int byteRead = 0;
			File oldFile = new File(oldPath);
			Log.e(TAG, "oldPath is exists??....." + oldFile.exists());
			if (oldFile.exists()) { //文件不存在时
				InputStream inStream = new FileInputStream(oldPath); //读入原文件
				FileOutputStream fos = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ( (byteRead = inStream.read(buffer)) != -1) {
					byteSum += byteRead; //字节数 文件大小
					System.out.println(byteSum);
					fos.write(buffer, 0, byteRead);
				}
				if(byteSum == oldFile.length()){
					Log.e(TAG, "Copy file finished.....");
				}
				deleteFileFromSDCard(oldPath);
				inStream.close();
				fos.close();
			}
		}
		catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}
}
