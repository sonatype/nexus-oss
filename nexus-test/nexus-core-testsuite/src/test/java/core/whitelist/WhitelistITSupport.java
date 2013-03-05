/*
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
package core.whitelist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sonatype.nexus.client.core.exception.NexusClientBadRequestException;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status;
import org.sonatype.nexus.client.core.subsystem.whitelist.Whitelist;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status.Outcome;

import com.google.common.io.Closeables;

import core.NexusCoreITSupport;

/**
 * Support class for Whitelist Core feature (NEXUS-5472), aka "proxy404".
 * 
 * @author cstamas
 * @since 2.4
 */
public abstract class WhitelistITSupport
    extends NexusCoreITSupport
{
    protected WhitelistITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    /**
     * Returns {@link Whitelist} client subsystem.
     * 
     * @return client for whitelist.
     */
    public Whitelist whitelist()
    {
        return client().getSubsystem( Whitelist.class );
    }

    /**
     * Waits for a remote discovery outcomes. The passed in repository IDs must correspond to a Maven2 proxy repository,
     * otherwise {@link IllegalArgumentException} is thrown. They all will be waited for, in passed in order.
     * 
     * @param proxyRepositoryIds
     * @throws IllegalArgumentException if repository ID is not a maven2 proxy.
     * @throws InterruptedException
     */
    public void waitForWLDiscoveryOutcomes( final String... proxyRepositoryIds )
        throws IllegalArgumentException, InterruptedException
    {
        for ( String proxyRepositoryId : proxyRepositoryIds )
        {
            waitForWLDiscoveryOutcome( proxyRepositoryId );
        }
    }

    /**
     * Waits for a remote discovery outcome. The passed in repository ID must correspond to a Maven2 proxy repository,
     * otherwise {@link IllegalArgumentException} is thrown.
     * 
     * @param proxyRepositoryId
     * @throws IllegalArgumentException if repository ID is not a maven2 proxy.
     * @throws InterruptedException
     */
    public void waitForWLDiscoveryOutcome( final String proxyRepositoryId )
        throws IllegalArgumentException, InterruptedException
    {
        // status
        Status status = whitelist().getWhitelistStatus( proxyRepositoryId );
        if ( status.getDiscoveryStatus() == null )
        {
            throw new IllegalArgumentException( "Repository with ID=" + proxyRepositoryId
                + " is not a Maven2 proxy repository!" );
        }
        while ( status.getDiscoveryStatus().isDiscoveryEnabled()
            && status.getDiscoveryStatus().getDiscoveryLastStatus() == Outcome.UNDECIDED )
        {
            Thread.sleep( 1000 );
            status = whitelist().getWhitelistStatus( proxyRepositoryId );
        }
    }

    /**
     * Waits for a publishing outcomes. The passed in repository IDs must correspond to a Maven2 repository, otherwise
     * {@link IllegalArgumentException} is thrown. They all will be waited for, in passed in order.
     * 
     * @param repositoryIds
     * @throws IllegalArgumentException if repository ID is not a maven2 repo.
     * @throws InterruptedException
     */
    public void waitForWLPublishingOutcomes( final String... repositoryIds )
        throws IllegalArgumentException, InterruptedException
    {
        for ( String repositoryId : repositoryIds )
        {
            waitForWLPublishingOutcome( repositoryId );
        }
    }

    /**
     * Waits for a publishing outcome. The passed in repository ID must correspond to a Maven2 repository, otherwise
     * {@link NexusClientBadRequestException} is thrown.
     * 
     * @param repositoryId
     * @throws NexusClientBadRequestException if repository ID is not a maven2 repo.
     * @throws InterruptedException
     */
    public void waitForWLPublishingOutcome( final String repositoryId )
        throws NexusClientBadRequestException, InterruptedException
    {
        // status
        Status status = whitelist().getWhitelistStatus( repositoryId );
        while ( status.getPublishedStatus() == Outcome.UNDECIDED )
        {
            Thread.sleep( 1000 );
            status = whitelist().getWhitelistStatus( repositoryId );
        }
    }

    /**
     * Does HTTP GET against given URL.
     * 
     * @param url
     * @return
     * @throws IOException
     */
    protected HttpResponse executeGet( final String url )
        throws IOException
    {
        InputStream entityStream = null;
        try
        {
            final HttpClient httpClient = new DefaultHttpClient();
            final HttpGet get = new HttpGet( url );
            final HttpResponse httpResponse = httpClient.execute( get );
            return httpResponse;
        }
        catch ( IOException e )
        {
            Closeables.closeQuietly( entityStream );
            throw e;
        }
    }

    /**
     * Fetches file from given URL.
     * 
     * @param url
     * @return
     * @throws IOException
     */
    protected InputStream getPrefixFileFrom( final String url )
        throws IOException
    {
        InputStream entityStream = null;
        try
        {
            final HttpResponse httpResponse = executeGet( url );
            assertThat( httpResponse.getStatusLine().getStatusCode(), equalTo( 200 ) );
            assertThat( httpResponse.getEntity(), is( notNullValue() ) );
            entityStream = httpResponse.getEntity().getContent();
            return entityStream;
        }
        catch ( IOException e )
        {
            Closeables.closeQuietly( entityStream );
            throw e;
        }
    }

}
