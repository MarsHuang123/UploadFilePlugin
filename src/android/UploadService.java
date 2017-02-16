package upload;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shig on 2017/2/7.
 */

public class UploadService {

    private String[]files;
    private String loginId;
    private String TAG = "UploadService";
    private List<String> filesNames = new ArrayList();
    private static final int FileStatusNoUpload = 0;
    private static final int FileStatusPreUpload = 1;

    private static final int FileStatusUploading = 2;
    private String uploadingFiles;
    private UploadService(){
    }
    private Context mContext;
    private static final UploadService instance = new UploadService();
    private final long needSize = 65536;
    public static UploadService getInstance() {
        return instance;
    }
    public static boolean isCanceled = false;
    private UploadDataService uploadDataService;
    private Handler handler;
    private void setProps(String[] files, String loginId, Context mContext, Handler handler){
        this.files = files;
        this.loginId = loginId;
        setContext(mContext);
        this.handler = handler;
        for(String name : files){
            if(!filesNames.contains(name)){
                filesNames.add(name);
            }
        }
    }

    public void setContext(Context context){
        this.mContext = context;
    }

    public void uploadFiles(Context context, String[]files, String loginId, Handler handler) throws Exception {
        Log.e(TAG, "uploadFiles::::");
        setProps(files, loginId, context, handler);
        uploadDataService  = new UploadDataService();
        GetUploadVoiceFileProgressAsyncTask asyncTask = new GetUploadVoiceFileProgressAsyncTask();
        asyncTask.execute((Void)null);
        isCanceled = false;
        //String fileName = "8740386_1";

        //upload();
    }
    class GetUploadVoiceFileProgressAsyncTask extends AsyncTask<Void, Void, String> {

        public GetUploadVoiceFileProgressAsyncTask() {
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            Log.e(TAG, "Start upload, filesNames size::::" + filesNames.size());

            for (int i = 0; i <= filesNames.size(); i++){
                if (isCanceled) {
                    break;
                }
                if(filesNames.size() > 0){
                    String fileName = filesNames.get(0);
                    //String fileName = "8746627_1";
                    uploadingFiles = fileName;
                    FileManager.getInstance().setFileName(fileName);
                    File f = FileManager.getInstance().getFile(mContext, fileName);
                    if(f != null){
                        Log.e(TAG, "file is::::" + f.exists());
                        Log.e(TAG, "file is::::" + f.length());
                        Log.e(TAG, "file is::::" + f.isDirectory());
                        if(f.exists()){
                            String caseId = getCaseId(fileName);
                            String index = fileName.substring(fileName.indexOf("_") + 1);
                            try {
                                String fileProgress = uploadDataService.executeGetUploadVoiceFileProgress(mContext, caseId, index);
                                if(fileProgress != null){
                                    fileProgress = fileProgress.substring(fileProgress.indexOf(":") + 1).trim();
                                    fileProgress = fileProgress.substring(0, fileProgress.indexOf("/"));
                                    Log.e(TAG, "fileProgress is::::" + fileProgress);
                                    long fileTotalLength = f.length();
                                    if(fileTotalLength > Long.valueOf(fileProgress)){
                                        upload(f, Long.valueOf(fileProgress), fileTotalLength, caseId, index, fileName);
                                    }else{
                                        //filesNames.remove(fileName);
                                        uploadFailed(fileName);
                                    }
                                }
                            } catch (Exception e) {

                                uploadFailed(fileName);
                                e.printStackTrace();
                            }
                        }else{
                            //filesNames.remove(fileName);
                            uploadFailed(fileName);
                        }
                    }else{
                        //filesNames.remove(fileName);
                        uploadFailed(fileName);
                    }
                }
            }
            Log.e(TAG, "Finished upload, filesNames size::::" + filesNames.size());
            uploadingFiles = null;
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }

    public void upload(File file, long fileProgress, long fileTotalLength, String caseId, String index, String fileName){
        if(file != null){

            BufferedInputStream bin = FileManager.getInstance().getFileStream(mContext);
            Log.e(TAG, "BufferedInputStream is::::" + bin);
            byte[] buffer = new byte[(int) needSize];
            int bytesRead = 0;
            byte[] cleanBuffer;
            long times =  fileProgress / needSize;
            boolean isSucceed = false;
            try {
                bin.skip(fileProgress);
                while ((bytesRead = bin.read(buffer)) != -1) {
                    Log.e(TAG, "times is::::" + times);
                    if (isCanceled) {
                        break;
                    }
                    if (bytesRead == needSize) {
                        cleanBuffer = buffer;
                    } else {
                        cleanBuffer = new byte[bytesRead];
                        System.arraycopy(buffer, 0, cleanBuffer, 0, bytesRead);
                    }
                    String Base64Str = Base64.encodeBytes(cleanBuffer);
                    isSucceed = uploadDataService.executeUploadVoiceFile(mContext, caseId, this.loginId, index,Base64Str, fileTotalLength);
                    if(!isSucceed){
                        Log.e(TAG, "The file is upload fail, will upload next file.....");
                        uploadFailed(fileName);
                        break;
                    }
                    Log.e(TAG, "Base64Str is::::" + Base64Str);
                }
                if(isSucceed){
                    Log.e(TAG, "The file is uploaded.....");
                    FileManager.getInstance().copyFile(mContext);
                    uploadSucceed(fileName);
                }
            } catch (IOException e) {
                // stopUpload();
                uploadFailed(fileName);
                Log.d(TAG, "IOException " + e);
            } catch (Exception e) {
                uploadFailed(fileName);
                e.printStackTrace();
            }
        }
    }

    public void uploadSucceed(String fileName){
        filesNames.remove(fileName);
        Message msg = new Message();
        msg.what = 0;
        Bundle b = new Bundle();// 存放数据
        b.putString(Upload.Bundle_Key, fileName);
        msg.setData(b);
        this.handler.sendMessage(msg);
    }

    public void uploadFailed(String fileName){

        filesNames.remove(fileName);
        Message msg = new Message();
        msg.what = 1;
        Bundle b = new Bundle();// 存放数据
        b.putString(Upload.Bundle_Key, fileName);
        msg.setData(b);
        this.handler.sendMessage(msg);
    }

    public JSONArray getFileStatus() throws JSONException {
        //uploadingFiles = "8745907_6";
        //filesNames.add("8746627_1");
        JSONArray jsonArray = new JSONArray();

        File f = FileManager.getInstance().getFileDirectory(mContext);
        if(f.isDirectory()){
            File[] files = f.listFiles();
            for(int i = 0; i < files.length; i++){
                JSONObject object = new JSONObject();
                boolean isExist = false;
                File file = files[i];
                if(file != null){
                    String fileNameInSdcard = getFileName(file.getName());
                    Log.e(TAG, "In DataForder files name is......" + fileNameInSdcard);
                    if(fileNameInSdcard != null){
                        if(fileNameInSdcard.equalsIgnoreCase(uploadingFiles)){
                            Log.d(TAG, "Uploading file is......" + uploadingFiles);
                            object.put("FileName", uploadingFiles);
                            object.put("CaseID", getCaseId(uploadingFiles));
                            object.put("FileStatus", FileStatusUploading);
                            jsonArray.put(i, object);
                            continue;
                        }
                        for(int j = 0; j < filesNames.size(); j++){
                            String name = filesNames.get(j);
                            if(fileNameInSdcard.equalsIgnoreCase(name)){
                                Log.d(TAG, "Pre Upload file is......" + name);
                                isExist = true;
                                object.put("FileName", name);
                                object.put("CaseID", getCaseId(name));
                                object.put("FileStatus", FileStatusPreUpload);
                                jsonArray.put(i, object);
                            }
                        }
                        if(!isExist){
                            Log.d(TAG, "No Upload file is......" + fileNameInSdcard);
                            object.put("FileName", fileNameInSdcard);
                            object.put("CaseID", getCaseId(fileNameInSdcard));
                            object.put("FileStatus", FileStatusNoUpload);
                            jsonArray.put(i, object);
                        }
                    }
                }
            }
        }

        return jsonArray;
    }

    private String getCaseId(String str){
        return str.substring(0, str.indexOf("_"));
    }

    private String getFileName(String str){
        return str.substring(0, str.indexOf("."));
    }

    public void stop(){
        isCanceled = true;
        filesNames.clear();
    }
}
