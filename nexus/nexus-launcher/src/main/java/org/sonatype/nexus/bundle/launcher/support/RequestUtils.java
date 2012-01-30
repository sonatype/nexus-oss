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
package org.sonatype.nexus.bundle.launcher.support;

import com.google.common.base.Preconditions;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @since 2.0
 */
public class RequestUtils
{

    private static final Logger LOG = LoggerFactory.getLogger( RequestUtils.class );

    /**
     * Execute HttpMethod with default Nexus admin credentials
     *
     * @param method
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public static HttpMethod executeHTTPClientMethodAsAdmin( final HttpMethod method )
        throws HttpException, IOException
    {
        HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout( 5000 );
        httpClient.getHttpConnectionManager().getParams().setSoTimeout( 5000 );

        httpClient.getState().setCredentials( AuthScope.ANY,
                                              new UsernamePasswordCredentials( "admin", "admin123" ) );
        List<String> authPrefs = new ArrayList<String>( 1 );
        authPrefs.add( AuthPolicy.BASIC );
        httpClient.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
        httpClient.getParams().setAuthenticationPreemptive( true );

        try
        {
            httpClient.executeMethod( method );
            method.getResponseBodyAsString(); // forced consumption of response I guess
            return method;
        }
        finally
        {
            method.releaseConnection();

            // force socket cleanup
            HttpConnectionManager mgr = httpClient.getHttpConnectionManager();

            if ( mgr instanceof SimpleHttpConnectionManager )
            {
                ( (SimpleHttpConnectionManager) mgr ).shutdown();

            }
        }
    }

    public static boolean isNexusRESTStarted( final String nexusBaseURI )
        throws IOException, HttpException
    {
        Preconditions.checkNotNull( nexusBaseURI );
        final String serviceStatusURI = nexusBaseURI.endsWith( "/" )
            ? nexusBaseURI + "service/local/status"
            : nexusBaseURI + "/service/local/status";
        org.apache.commons.httpclient.HttpMethod method = null;
        try
        {
            method = new GetMethod( serviceStatusURI );
            // only try once makes sense by default
            DefaultHttpMethodRetryHandler oneRetry = new DefaultHttpMethodRetryHandler( 1, true );
            method.getParams().setParameter( HttpMethodParams.RETRY_HANDLER, oneRetry );

            method = executeHTTPClientMethodAsAdmin( method );
            final int statusCode = method.getStatusCode();
            if ( statusCode != 200 )
            {
                LOG.debug( "Status check returned status " + statusCode );
                return false;
            }

            final String entityText = method.getResponseBodyAsString();
            if ( entityText == null || !entityText.contains( "<state>STARTED</state>" ) )
            {
                LOG.debug( "Status check returned invalid system state. Status: " + entityText );
                return false;
            }

            return true;
        }
        finally
        {
            if ( method != null )
            {
                method.releaseConnection(); // request facade does this but just making sure
            }
        }
    }

    public static boolean waitForNexusToStart( final String nexusBaseURI )
    {
        return waitFor( new Condition()
        {
            @Override
            public boolean isTrue()
            {
                try
                {
                    return isNexusRESTStarted( nexusBaseURI );
                }
                catch ( IOException ex )
                {
                    LOG.trace( "Problem testing REST start", ex );
                    return false;
                }
            }
        }, 30000, 700 ); // 30 seconds
    }

    /**
     * Used by {@link #waitFor(RequestUtils.Condition, int, int) } as a Condition to wait for
     */
    public abstract static class Condition
    {

        public abstract boolean isTrue();
    }

    public static boolean waitFor( final Condition condition, final int pollTimeoutMs, final int pollIntervalMs )
    {
        Preconditions.checkNotNull( condition );

        if ( !( pollTimeoutMs >= pollIntervalMs ) )
        {
            throw new IllegalArgumentException(
                "Poll timeout should be greater than or equal to the interval at which to poll" );
        }

        boolean completed = false;
        int count = 0;
        final int attempts = (int) pollTimeoutMs / pollIntervalMs;
        LOG.info( "Waiting - checking every {}ms, for up to approx. {}ms", pollIntervalMs, pollTimeoutMs );
        try
        {
            while ( count < attempts )
            {
                count++;

                completed = condition.isTrue();
                if ( completed )
                {
                    break;
                }

                sleep( pollIntervalMs );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
        LOG.info( "Wait - condition:{} , checked {} times, took approx. {}ms",
                  new Object[]{ completed, count, pollIntervalMs * count } );
        return completed;
    }

    /**
     * Sleep the current thread for the specified milliseconds.
     *
     * @param millis the amount of milliseconds to sleep
     */
    public static void sleep( final long millis )
    {
        try
        {
            Thread.sleep( millis );
        }
        catch ( InterruptedException e )
        {
            System.err.println( "Sleep of " + millis + "ms interrupted" );
        }
    }
}
