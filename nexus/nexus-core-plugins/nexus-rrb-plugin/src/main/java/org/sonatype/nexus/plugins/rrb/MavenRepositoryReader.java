/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.rrb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
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

    private String remotePath;

    private String remoteUrl;

    private String localUrl;

    private ProxyRepository proxyRepository;

    private String id;

    /**
     * @param remotePath remote path added to the URL
     * @param localUrl url to the local resource service
     * @return a list containing the remote data
     */
    public List<RepositoryDirectory> extract( String remotePath, String localUrl, ProxyRepository proxyRepository,
                                              String id )
    {
        logger.debug( "remotePath={}", remotePath );
        this.remotePath = remotePath;
        this.localUrl = localUrl;
        this.proxyRepository = proxyRepository;

        this.id = id;

        String baseRemoteUrl = proxyRepository.getRemoteUrl();

        if ( !baseRemoteUrl.endsWith( "/" ) && !remotePath.startsWith( "/" ) )
        {
            this.remoteUrl = baseRemoteUrl + "/" + remotePath;
        }
        else
        {
            this.remoteUrl = baseRemoteUrl + remotePath;
        }

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
            // if title="Artifactory" then it is an Artifactory repo...
            if ( indata.indexOf( "title=\"Artifactory\"" ) != -1 )
            {
                logger.debug( "is Artifactory repository" );
                parser = new ArtifactoryRemoteRepositoryParser( remotePath, localUrl, id, baseUrl );
            }
            else
            {
                logger.debug( "is html repository" );
                parser = new HtmlRemoteRepositoryParser( remotePath, localUrl, id, baseUrl );
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
            }
            else if ( responseContainsError( indata ) && responseContainsAccessDenied( indata ) )
            {
                logger.debug( "response from S3 repository contains access denied response" );
                indata = new StringBuilder();
            }

            parser =
                new S3RemoteRepositoryParser( remotePath, localUrl, id, baseUrl.replace( findRootUrl( indata ), "" ) );
        }
        else
        {
            logger.debug( "Found no matching parser, using default html parser" );

            parser = new HtmlRemoteRepositoryParser( remotePath, localUrl, id, baseUrl );
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

        // allow a couple redirects
        client.getParams().setParameter( "http.protocol.max-redirects", new Integer( 3 ) );

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

        // here is the deal, For reasons I do not understand, S3 comes back with an empty response (and a 200),
        // stripping off the last '/'
        // returns the error we are looking for (so we can do a query)
        Header serverHeader = method.getResponseHeader( "Server" );
        if ( result.length() == 0 && serverHeader.getValue().equals( "AmazonS3" ) && this.remoteUrl.endsWith( "/" ) )
        {
            this.remoteUrl = this.remoteUrl.substring( 0, this.remoteUrl.length() - 1 );
            // now just call it again
            return this.getContent();
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
