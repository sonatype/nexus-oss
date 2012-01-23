/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.rrb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.parsers.ArtifactoryRemoteRepositoryParser;
import org.sonatype.nexus.plugins.rrb.parsers.HtmlRemoteRepositoryParser;
import org.sonatype.nexus.plugins.rrb.parsers.RemoteRepositoryParser;
import org.sonatype.nexus.plugins.rrb.parsers.S3RemoteRepositoryParser;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

/**
 * Class for retrieving directory data from remote repository. This class is not thread-safe!
 */
public class MavenRepositoryReader
{

    private final Logger logger = LoggerFactory.getLogger( MavenRepositoryReader.class );

    private final AsyncHttpClient client;

    private String remotePath;

    private String remoteUrl;

    private String localUrl;

    private ProxyRepository proxyRepository;

    private String id;

    public MavenRepositoryReader( final AsyncHttpClient client )
    {
        this.client = client;
    }

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
        RequestBuilder builder = new RequestBuilder();

        if ( remoteUrl.indexOf( "?prefix" ) != -1 )
        {
            builder.setUrl( remoteUrl + "&delimiter=/" );
        }
        else
        {
            builder.setUrl( remoteUrl + "?delimiter=/" );
        }

        Response response = null;
        StringBuilder result = new StringBuilder();

        try
        {
            response = doCall( builder.build(), result );
        }
        catch ( IOException e )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.warn( e.getMessage(), e );
            }
            else
            {
                logger.warn( e.getMessage() );
            }
        }

        // here is the deal, For reasons I do not understand, S3 comes back with an empty response (and a 200),
        // stripping off the last '/'
        // returns the error we are looking for (so we can do a query)

        String serverHeader = response != null ? response.getHeader( "Server" ) : null;
        if ( result.length() == 0 && serverHeader != null && serverHeader.equalsIgnoreCase( "AmazonS3" )
            && remoteUrl.endsWith( "/" ) )
        {
            remoteUrl = remoteUrl.substring( 0, remoteUrl.length() - 1 );
            // now just call it again
            return getContent();
        }

        return result;
    }

    private Response doCall( Request request, StringBuilder result )
        throws IOException
    {
        try
        {
            Response response = client.executeRequest( request ).get();
            final int responseCode = response.getStatusCode();

            logger.debug( "responseCode={}", responseCode );
            BufferedReader reader = new BufferedReader( new InputStreamReader( response.getResponseBodyAsStream() ) );

            String line = null;
            while ( ( line = reader.readLine() ) != null )
            {
                result.append( line + "\n" );
            }

            return response;
        }
        catch ( Exception e )
        {
            throw new IOException( e );
        }
    }
}
