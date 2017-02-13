package upload;


import android.content.Context;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.security.KeyStore;

/**
 * Call web service asynchronously.
 */

public class HTTPRestServiceCaller {

	private static final String TAG = HTTPRestServiceCaller.class.getSimpleName();
	
	public static final int HTTP_DELETE_METHOD = 0;
	public static final int HTTP_POST_METHOD = 1; // requestHttpMethod
	public static final int HTTP_GET_METHOD = 2; // requestHttpMethod
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_VALUE = "application/json";

	private static final String API_KEY = "API-Key";
	private static final String API_KEY_VALUE = "a2ff41f0-ded0-4040-ab17-ba0f1aa4e12b";

	private static final String Total_Length = "Total-Length";
	private static final String Total_Length_VALUE = "";

	private HttpClient httpClient;


	private String header = null;
	/**
	 * Connect to the server for getting the response. StatusCode = 201, means
	 * creating account or creating offer GUID successfully .
	 * 
	 * @param context
	 * @return String
	 * @throws Exception

	 */

	public String executeHTTPRequest(Context context, String serverUrl, String postJson, int timeOut, int httpMethodFlag, long totalLength) throws Exception {
		if (httpClient == null) {
			httpClient = this.getHttpClient(timeOut, serverUrl, context);
		}
		HttpResponse response = null;
		Log.d(TAG, "serverUrl is: " + serverUrl);

		try {
			if (httpMethodFlag == HTTP_POST_METHOD) {
				StringEntity tmp = new StringEntity(postJson, HTTP.UTF_8);
				// Log.d(TAG ,"http flag: httpPost" );
				HttpPost httpPost = new HttpPost();
				httpPost = (HttpPost) this.getHttpUriRequest(HTTP_POST_METHOD, serverUrl, totalLength);
				httpPost.setEntity(tmp);
				response = httpClient.execute(httpPost);
			} else if (httpMethodFlag == HTTP_GET_METHOD) {
				// Log.d(TAG ,"http flag: HttpGet" );
				HttpGet httpGet = new HttpGet();
				httpGet = (HttpGet) this.getHttpUriRequest(HTTP_GET_METHOD, serverUrl, totalLength);
				response = httpClient.execute(httpGet);
			} else {
				HttpDelete httpDelete;
				httpDelete = (HttpDelete) this.getHttpUriRequest(HTTP_DELETE_METHOD, serverUrl, totalLength);
				response = httpClient.execute(httpDelete);
			}
		} catch (Exception e) {
			throw new Exception();
		}
		Log.d(TAG, "url is::::: " + serverUrl + "........response http code is: " + response.getStatusLine().getStatusCode());
		httpStatusCode = response.getStatusLine().getStatusCode();
		String responseString = getHttpResponse(response, serverUrl);
		return responseString;
	}

	private int httpStatusCode;
	public int getHttpStatusCode(){
		return httpStatusCode;
	}

	/**
	 * Get the http HttpResponse
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String getHttpResponse(HttpResponse response, String url) throws Exception {
		
		String responseString = null;

		if (response != null) {
			try {
				if (response.getEntity() != null) {
					responseString = EntityUtils.toString(response.getEntity());
					if (response.getLastHeader("Content-Range") != null) {
						header = response.getLastHeader("Content-Range").toString();
					}
				}
			} catch (Exception e) {
				Log.d(TAG, "Exception:::url::: " + url);
				e.printStackTrace();
				throw new Exception();
			}
		}
		Log.e(TAG, "url=====" + url + "-------------->urlResponse: " + responseString);
		Log.e(TAG, "url=====" + url + "-------------->header: " + header);
		return responseString;
	}

	public String getHeader(){
		return header;
	}

	/**
	 * Get the HttpClient, Set Timeout is 30000
	 * 
	 * @return HttpClient
	 */
	public HttpClient getHttpClient(int timeOut, String serverUrl, Context context) {

		//HttpClient httpClient = getNewHttpClient(timeOut, context);
		HttpClient httpClient = getNewHttpClient(timeOut);
		return httpClient;
	}

	/**
	 * Get HttpUriRequest, it will be HttpPost or HttpGet by different
	 * requestHttpMethod.
	 * 
	 * @param requestHttpMethod
	 *            POST_TYPE or GET_TYPE
	 * @param url
	 * @return HttpPost or HttpGet
	 */
	public Object getHttpUriRequest(int requestHttpMethod, String url, long totalLength) {
		if (url != null) {
			url = url.replaceAll(" ", "%20");
		}
		// Log.d("lastModified" ,"getHttpUriRequest===========lastModified: "+
		// lastModified);
		if (requestHttpMethod == HTTP_POST_METHOD) {// POST_TYPE
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
			httpPost.setHeader(API_KEY, API_KEY_VALUE);
			if(totalLength > 0){
				httpPost.setHeader("Total-Length", String.valueOf(totalLength));
			}
			return httpPost;
		} else if (requestHttpMethod == HTTP_GET_METHOD) {// GET_TYPE

			// Log.d(TAG ,"GET_TYPE: ");
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
			httpGet.setHeader(API_KEY, API_KEY_VALUE);

			return httpGet;
		} else {
			HttpDelete httpDelete = new HttpDelete(url);
			httpDelete.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
			return httpDelete;
		}
	}
	private static HttpClient getNewHttpClient(int timeOut) {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);

			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpProtocolParams.setUseExpectContinue(params, true);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));
			HttpConnectionParams.setConnectionTimeout(params, timeOut);
			HttpConnectionParams.setSoTimeout(params, timeOut);
			ConnManagerParams.setMaxTotalConnections(params, 5);
			ConnPerRouteBean connPerRoute = new ConnPerRouteBean(5);
		    ConnManagerParams.setMaxConnectionsPerRoute(params,connPerRoute);  
			//PoolingClientConnectionManager  ccm = new PoolingClientConnectionManager(registry);
			ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(params, registry);
			//ccm.setMaxTotal(20);
			//ccm.setDefaultMaxPerRoute(20);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}
}
