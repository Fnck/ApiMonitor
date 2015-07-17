package com.stubhub.demo.api.monitor.aether.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.resource.Resource;
import org.apache.maven.wagon.shared.http.HtmlFileListParser;

public class WagonHttpsClient
extends StreamWagon
{
private String previousProxyExclusions;

private String previousHttpProxyHost;

private String previousHttpProxyPort;

private HttpURLConnection putConnection;

/**
 * Whether to use any proxy cache or not.
 * 
 * @plexus.configuration default="false"
 */
private boolean useCache;

/** @plexus.configuration */
private Properties httpHeaders;

/**
 * Builds a complete URL string from the repository URL and the relative path passed.
 * 
 * @param path the relative path
 * @return the complete URL
 */
private String buildUrl( String path )
{
    final String repoUrl = getRepository().getUrl();

    path = path.replace( ' ', '+' );

    if ( repoUrl.charAt( repoUrl.length() - 1 ) != '/' )
    {
        return repoUrl + '/' + path;
    }

    return repoUrl + path;
}

public void fillInputData( InputData inputData )
    throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
{
    Resource resource = inputData.getResource();
    
    
    try
    {
    	// Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
            }
            @Override
            public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        } };
        
        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance( "SSL" );
        sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        

        // Tell the url connection object to use our socket factory which bypasses security checks
        
        
        URL url = new URL( buildUrl( resource.getName() ) );
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        //BYPASS THE SSL
        ((HttpsURLConnection)urlConnection).setSSLSocketFactory( sslSocketFactory );
        urlConnection.setRequestProperty( "Accept-Encoding", "gzip" );
        if ( !useCache )
        {
            urlConnection.setRequestProperty( "Pragma", "no-cache" );
        }

        addHeaders( urlConnection );

        // TODO: handle all response codes
        int responseCode = urlConnection.getResponseCode();
        if	(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
        	throw new TransferFailedException( "File not found:" + buildUrl( resource.getName() ) );
        }        
        if ( responseCode == HttpURLConnection.HTTP_FORBIDDEN
            || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED )
        {
            throw new AuthorizationException( "Access denied to: " + buildUrl( resource.getName() ) );
        }

        InputStream is = urlConnection.getInputStream();
        String contentEncoding = urlConnection.getHeaderField( "Content-Encoding" );
        boolean isGZipped = contentEncoding == null ? false : "gzip".equalsIgnoreCase( contentEncoding );
        if ( isGZipped )
        {
            is = new GZIPInputStream( is );
        }
        inputData.setInputStream( is );
        resource.setLastModified( urlConnection.getLastModified() );
        resource.setContentLength( urlConnection.getContentLength() );
    }
    catch ( MalformedURLException e )
    {
        throw new ResourceDoesNotExistException( "Invalid repository URL: " + e.getMessage(), e );
    }
    catch ( FileNotFoundException e )
    {
        throw new ResourceDoesNotExistException( "Unable to locate resource in repository", e );
    }
    catch ( IOException e )
    {
        throw new TransferFailedException( "Error transferring file: " + e.getMessage(), e );
    } catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (KeyManagementException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

private void addHeaders( URLConnection urlConnection )
{
    if ( httpHeaders != null )
    {
        for ( Iterator i = httpHeaders.keySet().iterator(); i.hasNext(); )
        {
            String header = (String) i.next();
            urlConnection.setRequestProperty( header, httpHeaders.getProperty( header ) );
        }
    }
}

public void fillOutputData( OutputData outputData )
    throws TransferFailedException
{
    Resource resource = outputData.getResource();
    try
    {
        URL url = new URL( buildUrl( resource.getName() ) );
        putConnection = (HttpURLConnection) url.openConnection();

        addHeaders( putConnection );

        putConnection.setRequestMethod( "PUT" );
        putConnection.setDoOutput( true );
        outputData.setOutputStream( putConnection.getOutputStream() );
    }
    catch ( IOException e )
    {
        throw new TransferFailedException( "Error transferring file: " + e.getMessage(), e );
    }
}

protected void finishPutTransfer( Resource resource, InputStream input, OutputStream output )
    throws TransferFailedException, AuthorizationException, ResourceDoesNotExistException
{
    try
    {
        int statusCode = putConnection.getResponseCode();

        switch ( statusCode )
        {
            // Success Codes
            case HttpURLConnection.HTTP_OK: // 200
            case HttpURLConnection.HTTP_CREATED: // 201
            case HttpURLConnection.HTTP_ACCEPTED: // 202
            case HttpURLConnection.HTTP_NO_CONTENT: // 204
                break;

            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new AuthorizationException( "Access denied to: " + buildUrl( resource.getName() ) );

            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new ResourceDoesNotExistException( "File: " + buildUrl( resource.getName() )
                    + " does not exist" );

                // add more entries here
            default:
                throw new TransferFailedException( "Failed to transfer file: " + buildUrl( resource.getName() )
                    + ". Return code is: " + statusCode );
        }
    }
    catch ( IOException e )
    {
        fireTransferError( resource, e, TransferEvent.REQUEST_PUT );

        throw new TransferFailedException( "Error transferring file: " + e.getMessage(), e );
    }
}

protected void openConnectionInternal()
    throws ConnectionException, AuthenticationException
{
    previousHttpProxyHost = System.getProperty( "http.proxyHost" );
    previousHttpProxyPort = System.getProperty( "http.proxyPort" );
    previousProxyExclusions = System.getProperty( "http.nonProxyHosts" );

    final ProxyInfo proxyInfo = getProxyInfo( "http", getRepository().getHost() );
    if ( proxyInfo != null )
    {
        setSystemProperty( "http.proxyHost", proxyInfo.getHost() );
        setSystemProperty( "http.proxyPort", String.valueOf( proxyInfo.getPort() ) );
        setSystemProperty( "http.nonProxyHosts", proxyInfo.getNonProxyHosts() );
    }
    else
    {
        setSystemProperty( "http.proxyHost", null );
        setSystemProperty( "http.proxyPort", null );
    }

    final boolean hasProxy = ( proxyInfo != null && proxyInfo.getUserName() != null );
    final boolean hasAuthentication = ( authenticationInfo != null && authenticationInfo.getUserName() != null );
    if ( hasProxy || hasAuthentication )
    {
        Authenticator.setDefault( new Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                // TODO: ideally use getRequestorType() from JDK1.5 here...
                if ( hasProxy && getRequestingHost().equals( proxyInfo.getHost() )
                    && getRequestingPort() == proxyInfo.getPort() )
                {
                    String password = "";
                    if ( proxyInfo.getPassword() != null )
                    {
                        password = proxyInfo.getPassword();
                    }
                    return new PasswordAuthentication( proxyInfo.getUserName(), password.toCharArray() );
                }

                if ( hasAuthentication )
                {
                    String password = "";
                    if ( authenticationInfo.getPassword() != null )
                    {
                        password = authenticationInfo.getPassword();
                    }
                    return new PasswordAuthentication( authenticationInfo.getUserName(), password.toCharArray() );
                }

                return super.getPasswordAuthentication();
            }
        } );
    }
    else
    {
        Authenticator.setDefault( null );
    }
}

public void closeConnection()
    throws ConnectionException
{
    if ( putConnection != null )
    {
        putConnection.disconnect();
    }

    setSystemProperty( "http.proxyHost", previousHttpProxyHost );
    setSystemProperty( "http.proxyPort", previousHttpProxyPort );
    setSystemProperty( "http.nonProxyHosts", previousProxyExclusions );
}

public List getFileList( String destinationDirectory )
    throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
{
    InputData inputData = new InputData();

    if ( destinationDirectory.length() > 0 && !destinationDirectory.endsWith( "/" ) )
    {
        destinationDirectory += "/";
    }

    String url = buildUrl( destinationDirectory );

    Resource resource = new Resource( destinationDirectory );

    inputData.setResource( resource );

    fillInputData( inputData );

    InputStream is = inputData.getInputStream();

    if ( is == null )
    {
        throw new TransferFailedException( url + " - Could not open input stream for resource: '" + resource
                                           + "'" );
    }

    return HtmlFileListParser.parseFileList( url, is );
}

public boolean resourceExists( String resourceName )
    throws TransferFailedException, AuthorizationException
{
    HttpURLConnection headConnection;

    try
    {
        URL url = new URL( buildUrl( new Resource( resourceName ).getName() ) );
        headConnection = (HttpURLConnection) url.openConnection();

        addHeaders( headConnection );

        headConnection.setRequestMethod( "HEAD" );
        headConnection.setDoOutput( true );

        int statusCode = headConnection.getResponseCode();

        switch ( statusCode )
        {
            case HttpURLConnection.HTTP_OK:
                return true;

            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new AuthorizationException( "Access denied to: " + url );

            case HttpURLConnection.HTTP_NOT_FOUND:
                return false;

            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new AuthorizationException( "Access denied to: " + url );

            default:
                throw new TransferFailedException( "Failed to look for file: " + buildUrl( resourceName )
                    + ". Return code is: " + statusCode );
        }
    }
    catch ( IOException e )
    {
        throw new TransferFailedException( "Error transferring file: " + e.getMessage(), e );
    }
}

public boolean isUseCache()
{
    return useCache;
}

public void setUseCache( boolean useCache )
{
    this.useCache = useCache;
}

public Properties getHttpHeaders()
{
    return httpHeaders;
}

public void setHttpHeaders( Properties httpHeaders )
{
    this.httpHeaders = httpHeaders;
}

void setSystemProperty( String key, String value )
{
    if ( value != null )
    {
        System.setProperty( key, value );
    }
    else
    {
        System.getProperties().remove( key );
    }
}

}