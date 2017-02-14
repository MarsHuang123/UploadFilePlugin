package upload;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;

import java.io.BufferedInputStream;
import java.io.File;
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
	public BufferedInputStream getFileStream(Context context) {

		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(getFileFullPath(context)), 1204);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath(Context context) {
		//File f = android.os.Environment.getExternalStorageDirectory();
		File f = context.getFilesDir();
		return f.getPath() + "/files/AudioFolder/";
	}

	public String getFileFullPath(Context context) {
		//File f = android.os.Environment.getExternalStorageDirectory();
		File f = context.getFilesDir();
		return f.getPath() + "/files/AudioFolder/" + fileName;
	}

	public String getFileUplaodedFullPath(Context context) {
		File f = context.getFilesDir();

		return f.getPath() + "/files/FinishDataFolder/" + fileName + fileSuffix;
	}

	public void createFileUploadDir(Context context){
		File f = context.getFilesDir();
		File saveFile = new File(f.getAbsoluteFile() + "/files/FinishDataFolder/");
		if(!saveFile.exists()){
			saveFile.mkdirs();
		}
		Log.e(TAG, "saveFile is....." + saveFile.getAbsolutePath());
		//return saveFile;
	}

	public File getFile(Context context, String fileName) {
		//File SDFile = android.os.Environment.getExternalStorageDirectory();

		File myFile = new File(getFilePath(context) + fileName);
		return myFile;
	}
	public File getFileDirectory(Context context){
		File myFile = new File(getFilePath(context));
		return myFile;
	}

	public boolean deleteFileFromSDCard(String fileFullPath) throws IOException {
		File file = new File(fileFullPath);
		Log.e(TAG, "Delete file....." + fileFullPath);
		return file.delete();
	}
	public void copyFile(Context context) {
		createFileUploadDir(context);
		String oldPath = getFileFullPath(context);
		String newPath= getFileUplaodedFullPath(context);
		Log.e(TAG, "Copy file start.....");
		Log.e(TAG, "oldPath is....." + oldPath);
		Log.e(TAG, "newPath is....." + newPath);
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
