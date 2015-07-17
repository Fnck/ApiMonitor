package com.stubhub.demo.api.monitor.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	private final String content_type = "Content-Type";
	private final String accept = "Accept";
	
	
	@SuppressWarnings("deprecation")
	public String HttpDownloadFile(String url, Header[] headers, String path) throws Exception{
		HttpGet get = new HttpGet(url);
		if(headers != null){
			for(Header h : headers){
				get.addHeader(h);
			}
		}
		
		String filePath = null;
		HttpClient client = new DefaultHttpClient();
		
		try {
			SSLContext ctx;
			ctx = SSLContext.getInstance("TLS");

			X509TrustManager tm = new X509TrustManager() {

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain, String authType)
								throws CertificateException {
				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain, String authType)
								throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}};
			ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
			SSLSocketFactory sf = new SSLSocketFactory(ctx);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme sch = new Scheme("https", 443, sf);
			client.getConnectionManager().getSchemeRegistry().register(sch);
			HttpResponse resp = null;
			try{
				resp = client.execute(get);
			}catch(ConnectException cx){
				//retry 3 times 
				for(int i=0;i<3;i++){
					client.getConnectionManager().shutdown();
					resp = client.execute(get);
				}
			}
			if(resp == null){
				throw new Exception("http request failed after retry!!");
			}
			if(resp.getStatusLine().getStatusCode() == 200){
				String fileName = null;
				if(resp.getFirstHeader("Content-Disposition")!=null){
					Header fileInfo = resp.getFirstHeader("Content-Disposition");
					System.out.println(fileInfo.getValue());
					fileName = fileInfo.getValue().split("=")[1];
				}
				
				File f = new File(path);
				if(!f.exists()){
					f.mkdirs();
				}
				
				if(resp.getFirstHeader("Content-Type").getValue().equalsIgnoreCase("application/java-archive")){
					fileName = url.split("/")[url.split("/").length - 1];
				}
				if(resp.getFirstHeader("Content-Type").getValue().equalsIgnoreCase("application/xml")){
					fileName = "pom.xml";
				}
				BufferedInputStream buffer = new BufferedInputStream(resp.getEntity().getContent());
				
				filePath = path+fileName;
				File oldFile = new File(filePath);
				if(oldFile.exists()){
					oldFile.delete();
				}
				BufferedOutputStream bufferOut = new BufferedOutputStream(new FileOutputStream(filePath));
				int byteInt = 0;
				while((byteInt = buffer.read())!= -1){
					bufferOut.write(byteInt);
				}
				buffer.close();
				bufferOut.close();
			}
			else{
				InputStream in = resp.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				StringBuilder sb = new StringBuilder();
				String line;
				while((line = br.readLine())!= null){
					sb.append(line);
				}
				in.close();
				String result = sb.toString();
				System.out.println(resp.getStatusLine().getStatusCode());
				System.out.println(result);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			client.getConnectionManager().shutdown();
		}
		return filePath;
	}
	
	public static String httpsGetRequest(String url, Header[] headers) throws Exception{
		int httpcode = 0;
		String result = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, false);
		httpclient.setHttpRequestRetryHandler(retryHandler);

		try {
			SSLContext ctx;
			ctx = SSLContext.getInstance("TLS");

			X509TrustManager tm = new X509TrustManager() {

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain, String authType)
								throws CertificateException {
				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain, String authType)
								throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}};
			ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
			SSLSocketFactory sf = new SSLSocketFactory(ctx);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme sch = new Scheme("https", 443, sf);
			httpclient.getConnectionManager().getSchemeRegistry().register(sch);

			HttpGet get = new HttpGet(url);
			if (headers != null) {
				if (headers.length > 0) {
					for (Header h : headers) {
						get.addHeader(h);
					}
				}
			}

			HttpResponse response = null;
			try{
				response = httpclient.execute(get);
			}catch(ConnectException cx){
				//retry 3 times 
				for(int i=0;i<3;i++){
					httpclient.getConnectionManager().shutdown();
					response = httpclient.execute(get);
				}
			}
			if(response == null){
				throw new Exception("http request failed after retry!!");
			}
			
			HttpEntity resentity = response.getEntity();
			httpcode = response.getStatusLine().getStatusCode();
			int retryWhenFailed = 5;
			//System.out.println("Calling "+url+" get response code:" + httpcode);
			while((httpcode != 200)&&(retryWhenFailed >0)){
				System.out.println("Start retry request: " + retryWhenFailed);
				EntityUtils.consume(resentity);
				response = null;
				response = httpclient.execute(get);
				resentity = response.getEntity();
				httpcode = response.getStatusLine().getStatusCode();
				//System.out.println("Calling "+url+" get response code:" + httpcode);
				retryWhenFailed --;				
			}
			InputStream in = resentity.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			in.close();
			result = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			httpclient.getConnectionManager().shutdown();
		}
		// System.out.println(result);
		if (httpcode > 0) {
			return result;
		} else {
			return "errors!";
		}

	}
	
	public HttpResponse HttpsGetResponse(String url, Header[] headers){
		int httpcode = 0;
		HttpResponse result = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();		
		
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("SSL");
		
		X509TrustManager tm = new X509TrustManager() {
			
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}};
		ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
		SSLSocketFactory sf = new SSLSocketFactory(ctx);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", 443, sf);
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);

		HttpGet get = new HttpGet(url);
		if(headers!= null){
			if(headers.length > 0){
				for(Header h : headers){
					get.addHeader(h);
				}
			}
		}

		result = httpclient.execute(get);
		httpcode = result.getStatusLine().getStatusCode();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {			
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        //System.out.println(result);
        if(httpcode > 0){
        	return result;
        }
        else{
        	return null;
        }
        
	}
	
	public String HttpsPostStringRequest(String url, String json, Header[] headers, String contentType, String acceptStr){
		int httpcode = 0;
		String result = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();		
		
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("SSL");
		
			X509TrustManager tm = new X509TrustManager() {
			
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}};
			ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
			SSLSocketFactory sf = new SSLSocketFactory(ctx);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        Scheme sch = new Scheme("https", 443, sf);
	        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
	
			HttpPost post = new HttpPost(url);
			
			if(headers!= null){
				if(headers.length > 0){
					for(Header h : headers){
						post.addHeader(h);
					}
				}
			}
			if(contentType != null){
				post.addHeader(this.content_type, contentType);
			}
			if(acceptStr != null){
				post.addHeader(this.accept, acceptStr);
			}
			StringEntity body = new StringEntity(json);
			post.setEntity(body);
	
			HttpResponse response = httpclient.execute(post);
			HttpEntity resentity = response.getEntity();
			httpcode = response.getStatusLine().getStatusCode();
	        System.out.println(httpcode);
	        InputStream in = resentity.getContent();
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        StringBuilder sb = new StringBuilder();
	        String line;
	        while((line = br.readLine())!= null){
	        	sb.append(line);
	        }
	        in.close();
	        httpclient = null;
	        result = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {			
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        //System.out.println(result);
        if(httpcode > 0){
        	return result;
        }
        else{
        	return "errors!";
        }
        
	}
	
	public HttpResponse HttpsPostBodyRequest(String url, List<NameValuePair> body, Header[] headers){
		int httpcode = 0;
		HttpResponse response = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();		
		
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("SSL");
		
			X509TrustManager tm = new X509TrustManager() {
			
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}};
			ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
			SSLSocketFactory sf = new SSLSocketFactory(ctx);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        Scheme sch = new Scheme("https", 443, sf);
	        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
	
			HttpPost post = new HttpPost(url);
			
			if(headers!= null){
				if(headers.length > 0){
					for(Header h : headers){
						post.addHeader(h);
					}
				}
			}
			post.setEntity(new UrlEncodedFormEntity(body));
			
			response = httpclient.execute(post);
			httpcode = response.getStatusLine().getStatusCode();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {			
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        //System.out.println(result);
        if(httpcode > 0){
        	return response;
        }
        else{
        	return null;
        }
        
	}
	
	public String HttpsPostBodyRequestGetStr(String url, List<NameValuePair> body, Header[] headers){
		int httpcode = 0;
		String result = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();		
		
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("SSL");
		
			X509TrustManager tm = new X509TrustManager() {
			
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}};
			ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
			SSLSocketFactory sf = new SSLSocketFactory(ctx);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        Scheme sch = new Scheme("https", 443, sf);
	        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
	
			HttpPost post = new HttpPost(url);
			
			if(headers!= null){
				if(headers.length > 0){
					for(Header h : headers){
						post.addHeader(h);
					}
				}
			}
			post.setEntity(new UrlEncodedFormEntity(body));
	
			HttpResponse response = httpclient.execute(post);
			HttpEntity resentity = response.getEntity();
			httpcode = response.getStatusLine().getStatusCode();
	        System.out.println(httpcode);
	        InputStream in = resentity.getContent();
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        StringBuilder sb = new StringBuilder();
	        String line;
	        while((line = br.readLine())!= null){
	        	sb.append(line);
	        }
	        in.close();
	        httpclient = null;
	        result = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {			
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        //System.out.println(result);
        if(httpcode > 0){
        	return result;
        }
        else{
        	return "errors!";
        }
        
	}
	
	public String HttpGetRequest(String url, Header[] headers){
		int httpcode = 0;
		String result = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();	
		
		HttpGet get = new HttpGet(url);
		
		if(headers!= null){
			if(headers.length > 0){
				for(Header h : headers){
					get.addHeader(h);
				}
			}
		}

		HttpResponse response;
		try {
			response = httpclient.execute(get);
		
			HttpEntity resentity = response.getEntity();
			httpcode = response.getStatusLine().getStatusCode();
	        System.out.println(httpcode);
	        InputStream in = resentity.getContent();
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        StringBuilder sb = new StringBuilder();
	        String line;
	        while((line = br.readLine())!= null){
	        	sb.append(line);
	        }
	        in.close();
	        httpclient = null;
	        result = sb.toString();	
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       // System.out.println(result);
        if(httpcode > 0){
        	return result;
        }
        else{
        	return "errors!";
        }
        
	}
}
