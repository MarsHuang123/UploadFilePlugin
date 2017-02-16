package upload;

import android.content.Context;
import android.util.Log;



/**
 * Created by shig on 2017/2/7.
 */

public class UploadDataService {

    private static final String TAG = HTTPRestServiceCaller.class.getSimpleName();

    public String executeGetUploadVoiceFileProgress(Context context, String caseId, String index) throws Exception {
        String functionName = "/GetUploadVoiceFileProgress";
        HTTPRestServiceCaller httpRestServiceCaller = new HTTPRestServiceCaller();

        String postJsonString = "{ \"Requests\":{ \"CaseID\":\"" + caseId + "\", \"Index\":\"" + index + "\" } }";
        Log.d(TAG, "postJsonString.... " + postJsonString);
        String urlString = "http://csdtest.acer.com.cn/MobilityCssApi/api/CaseList";
        urlString = urlString + functionName;
        String response = httpRestServiceCaller.executeHTTPRequest(context, urlString, postJsonString, 45000, HTTPRestServiceCaller.HTTP_POST_METHOD, 0);
        String header = httpRestServiceCaller.getHeader();
        Log.d(TAG, "GetUploadVoiceFileProgress:::::Response is------------ " + response);
        Log.d(TAG, "GetUploadVoiceFileProgress:::::header is------------ " + header);
        return header;
    }

    public boolean executeUploadVoiceFile(Context context, String caseId, String loginId, String index, String Base64Str, long totalLength) throws Exception {
        String functionName = "/UploadVoiceFile";
        HTTPRestServiceCaller httpRestServiceCaller = new HTTPRestServiceCaller();

        String postJsonString = "{\n" +
                "    \"Requests\":{\n" +
                "        \"CaseID\":\"" + caseId + "\",\n" +
                "        \"LoginID\":\"" + loginId + "\",\n" +
                "        \"Index\":\"" + index + "\",\n" +
                "        \"CustomerSatisfactionVoiceFile\":\n" +
                "            \"" + Base64Str + "\"\n" +
                "       \n" +
                "    }\n" +
                "}\n";
        Log.d(TAG, "executeUploadVoiceFile...Parmeters...postJsonString.... " + postJsonString);
        String urlString = "http://csdtest.acer.com.cn/MobilityCssApi/api/CaseList";
        urlString = urlString + functionName;
        String response = httpRestServiceCaller.executeHTTPRequest(context, urlString, postJsonString, 45000, HTTPRestServiceCaller.HTTP_POST_METHOD, totalLength);
        Log.d(TAG, "UploadVoiceFile:::::Response is------------ " + response);
        if(httpRestServiceCaller.getHttpStatusCode() == 200){
            return true;
        }else {
            return false;
        }
    }
}
