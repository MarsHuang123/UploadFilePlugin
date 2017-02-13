package upload;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLSocketFactoryEx extends SSLSocketFactory {
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        public SSLSocketFactoryEx(KeyStore truststore)
                        throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
                super(truststore);
                
                TrustManager tm = new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}  
    
              
            public void checkClientTrusted(
                            java.security.cert.X509Certificate[] chain, String authType)
                                            throws CertificateException {}


            public void checkServerTrusted(
                            java.security.cert.X509Certificate[] chain, String authType)
                                            throws CertificateException {
                            try {
                                    chain[0].checkValidity();
                                } catch (Exception e) {
                                    throw new CertificateException("Certificate not valid or trusted.");
                                }
                            }
        };  
        sslContext.init(null, new TrustManager[] { tm }, null);
    }  
    
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port,autoClose);  
    }  
    
    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();  
    }  
}