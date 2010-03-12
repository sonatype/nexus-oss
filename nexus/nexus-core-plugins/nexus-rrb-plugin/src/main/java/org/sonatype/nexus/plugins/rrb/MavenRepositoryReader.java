package org.sonatype.nexus.plugins.rrb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.parsers.ArtifactoryRemoteRepositoryParser;
import org.sonatype.nexus.plugins.rrb.parsers.HtmlRemoteRepositoryParser;
import org.sonatype.nexus.plugins.rrb.parsers.RemoteRepositoryParser;
import org.sonatype.nexus.plugins.rrb.parsers.S3RemoteRepositoryParser;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.HttpClientProxyUtil;

/**
 * Class for retrieving directory data from remote repository. This class is not thread-safe!
 */
public class MavenRepositoryReader
{

    private final Logger logger = LoggerFactory.getLogger( MavenRepositoryReader.class );

    private String remoteUrl;

    private String localUrl;

    private ProxyRepository proxyRepository;

    private String id;

    /**
     * @param remoteUrl url to the remote repository
     * @param localUrl url to the local resource service
     * @return a list containing the remote data
     */
    public List<RepositoryDirectory> extract( String remoteUrl, String localUrl, ProxyRepository proxyRepository,
                                              String id )
    {
        logger.debug( "remoteUrl={}", remoteUrl );
        this.remoteUrl = remoteUrl;
        this.localUrl = localUrl;
        this.proxyRepository = proxyRepository;

        this.id = id;
        StringBuilder html = getContent();
        if ( logger.isDebugEnabled() )
        {
            logger.trace( html.toString() );
        }
        return parseResult( html );
    }

    private ArrayList<RepositoryDirectory> parseResult( StringBuilder indata )
    {
        RemoteRepositoryParser parser = null;
        String baseUrl = "";
        if ( proxyRepository != null )
        {
            baseUrl = proxyRepository.getRemoteUrl();
        }

        if ( indata.indexOf( "<html " ) != -1 )
        {
            //if title="Artifactory" then it is an Artifactory repo...
            if ( indata.indexOf( "title=\"Artifactory\"" ) != -1 ) {
            	logger.debug( "is Artifactory repository" );
            	parser = new ArtifactoryRemoteRepositoryParser( remoteUrl, localUrl, id, baseUrl );
            } else {
                logger.debug( "is html repository" );
            	parser = new HtmlRemoteRepositoryParser( remoteUrl, localUrl, id, baseUrl );
            }
        }
        else if ( indata.indexOf( "xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\"" ) != -1
            || ( indata.indexOf( "<?xml" ) != -1 && responseContainsError( indata ) ) )
        {
            logger.debug( "is S3 repository" );
            if ( responseContainsError( indata ) && !responseContainsAccessDenied( indata ) )
            {
                logger.debug( "response from S3 repository contains error, need to find rootUrl" );
                remoteUrl = findcreateNewUrl( indata );
                indata = getContent();
            } else if ( responseContainsError( indata ) && responseContainsAccessDenied( indata ) ) {
                logger.debug( "response from S3 repository contains access denied response" );
                indata = new StringBuilder();
            }
            
            parser =
                new S3RemoteRepositoryParser( remoteUrl, localUrl, id, baseUrl.replace( findRootUrl( indata ), "" ) );
        }
        else
        {
            logger.debug( "Found no matching parser, using default html parser" );

            parser = new HtmlRemoteRepositoryParser( remoteUrl, localUrl, id, baseUrl );
        }
        return parser.extractLinks( indata );
    }

    private String findcreateNewUrl( StringBuilder indata )
    {
        logger.debug( "indata={}", indata.toString() );
        String key = extracktKey( indata );
        String newUrl = "";
        if ( !key.equals( "" ) )
        {
            newUrl = findRootUrl( indata );
            newUrl += "?prefix=" + key;
        }
        if ( !newUrl.endsWith( "/" ) )
        {
            newUrl += "/";
        }
        logger.debug( "newUrl={}", newUrl );
        return newUrl;
    }

    private String findRootUrl( StringBuilder indata )
    {
        int end = remoteUrl.indexOf( extracktKey( indata ) );
        if ( end > 0 )
        {
            String newUrl = remoteUrl.substring( 0, end );
            if ( newUrl.indexOf( '?' ) != -1 )
            {
                newUrl = newUrl.substring( 0, newUrl.indexOf( '?' ) );
            }
            return newUrl;
        }
        return remoteUrl;
    }

    private String extracktKey( StringBuilder indata )
    {
        String key = "";
        int start = indata.indexOf( "<Key>" );
        int end = indata.indexOf( "</Key>" );
        if ( start > 0 && end > start )
        {
            key = indata.substring( start + 5, end );
        }
        return key;
    }

    /**
     * Used to detect error in S3 response.
     * 
     * @param indata
     * @return
     */
    private boolean responseContainsError( StringBuilder indata )
    {
        if ( indata.indexOf( "<Error>" ) != -1 || indata.indexOf( "<error>" ) != -1 )
        {
            return true;
        }
        return false;
    }

    /**
     * Used to detect access denied in S3 response.
     * 
     * @param indata
     * @return
     */
    private boolean responseContainsAccessDenied( StringBuilder indata )
    {
        if ( indata.indexOf( "<Code>AccessDenied</Code>" ) != -1 || indata.indexOf( "<code>AccessDenied</code>" ) != -1 )
        {
            return true;
        }
        return false;
    }

    private StringBuilder getContent()
    {
        GetMethod method = null;

        HttpClient client = new HttpClient();

        if ( proxyRepository != null )
        {
            RemoteStorageContext rctx = proxyRepository.getRemoteStorageContext();
            HttpClientProxyUtil.applyProxyToHttpClient( client, rctx, null ); // no logger to pass in
        }

        if ( remoteUrl.indexOf( "?prefix" ) != -1 )
        {
            method = new GetMethod( remoteUrl + "&delimiter=/" );
            method.setFollowRedirects( true );
        }
        else
        {
            method = new GetMethod( remoteUrl + "?delimiter=/" );
            method.setFollowRedirects( true );
        }

        method.getParams().setParameter( HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler( 3, false ) );
        client.getParams().setParameter( "http.protocol.max-redirects", new Integer( 1 ) );

        StringBuilder result = new StringBuilder();

        try
        {
            doCall( method, client, result );
        }
        catch ( HttpException e )
        {
            method.setPath( method.getPath() + "/" );

            try
            {
                doCall( method, client, result );
            }
            catch ( HttpException e1 )
            {
                logger.error( e.getMessage(), e );
            }
            catch ( IOException e1 )
            {
                logger.error( e.getMessage(), e );
            }
        }
        catch ( IOException e )
        {
            logger.error( e.getMessage(), e );
        }
        finally
        {
            // Release the connection.
            method.releaseConnection();
        }

        return result;
    }

    private void doCall( GetMethod method, HttpClient client, StringBuilder result )
        throws IOException, HttpException
    {
        int responseCode;
        responseCode = client.executeMethod( method );

        logger.debug( "responseCode={}", responseCode );
        BufferedReader reader = new BufferedReader( new InputStreamReader( method.getResponseBodyAsStream() ) );

        String line = null;
        while ( ( line = reader.readLine() ) != null )
        {
            result.append( line + "\n" );
        }
    }
}
