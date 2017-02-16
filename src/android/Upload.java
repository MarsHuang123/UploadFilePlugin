package upload;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by shig on 2017/2/9.
 */

public class Upload extends CordovaPlugin {
    private static final String TAG = "Upload";
    public static final String Bundle_Key = "FileName";
    private MyHandler myHandler = null;

    /**
     * Constructor.
     */
    public Upload() {
    }
    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("resume".equalsIgnoreCase(action)) {
            //callbackContext.success(r);
/*            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:aa()");
                }
            });*/

            myHandler = new MyHandler();
            String caseId = args.getString(0);
            JSONArray array = args.getJSONArray(1);
            Log.d("caseId", "caseId。。。。。。" + caseId);
            if(array != null){

                String[] files = new String[array.length()];
                for (int i = 0; i < array.length(); i++){
                    files[i] = array.getString(i);
                }
                try {
                    UploadService.getInstance().uploadFiles(cordova.getActivity().getApplication(), files, caseId, myHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if("getFilesStatus".equalsIgnoreCase(action)){
            UploadService.getInstance().setContext(cordova.getActivity().getApplication());
            JSONArray array = UploadService.getInstance().getFileStatus();
            callbackContext.success(array);
        }else if("stop".equalsIgnoreCase(action)){
            UploadService.getInstance().stop();
        }
        else {
            return false;
        }
        return true;
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.d("MyHandler", "handleMessage。。。。。。");
            boolean isSucceed = false;
            super.handleMessage(msg);
            if(msg != null){
                if(msg.what == 0){
                    isSucceed = true;
                }else {
                    isSucceed = false;
                }
            }
            Bundle b = msg.getData();
            String fileName = b.getString(Bundle_Key);
            Log.d("MyHandler", "fileName。。。。。。" + fileName);
            Log.d("MyHandler", "isSucceed。。。。。。" + isSucceed);
            final String str = String.format("javascript:uploadFinish('%s', %b)", fileName, isSucceed);
            Log.d("MyHandler", "str。。。。。。" + str);
            //webView.loadUrl(str);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl(str);
                }
            });
        }
    }
}
