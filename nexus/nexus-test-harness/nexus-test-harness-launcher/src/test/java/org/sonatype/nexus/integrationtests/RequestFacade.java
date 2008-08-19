package org.sonatype.nexus.integrationtests;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.sonatype.nexus.test.utils.TestProperties;

public class RequestFacade
{
    private static final Logger LOG = Logger.getLogger( RequestFacade.class );

    public static Response doGetRequest( String serviceURIpart )
        throws IOException
    {
        return sendMessage( serviceURIpart, Method.GET );
    }

    public static Response sendMessage( String serviceURIpart, Method method )
        throws IOException
    {
        return sendMessage( serviceURIpart, method, null );
    }

    public static Response sendMessage( String serviceURIpart, Method method, Representation representation )
        throws IOException
    {

        String serviceURI = TestProperties.getString( "nexus.base.url" ) + serviceURIpart;
        return sendMessage( new URL( serviceURI ), method, representation );
    }

    public static Response sendMessage( URL url, Method method, Representation representation )
        throws IOException
    {

        Request request = new Request();
        request.setResourceRef( url.toString() );
        request.setMethod( method );
        request.setEntity( representation );

        // check the text context to see if this is a secure test
        TestContext context = TestContainer.getInstance().getTestContext();
        if ( context.isSecureTest() )
        {
            ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
            ChallengeResponse authentication =
                new ChallengeResponse( scheme, context.getUsername(), context.getPassword() );
            request.setChallengeResponse( authentication );
        }

        Client client = new Client( Protocol.HTTP );

        LOG.debug( "sendMessage: " + method.getName() + " "+ url );
        return client.handle( request );
    }

    protected static File downloadFile( URL url, String targetFile )
        throws IOException
    {

        OutputStream out = null;
        InputStream in = null;
        File downloadedFile = new File( targetFile );

        try
        {
            Response response = sendMessage( url, Method.GET, null );

            if ( !response.getStatus().isSuccess() )
            {
                throw new FileNotFoundException( response.getStatus() + " - " + url );
            }

            // if this is null then someone was getting really creative with the tests, but hey, we will let them...
            if ( downloadedFile.getParentFile() != null )
            {
                downloadedFile.getParentFile().mkdirs();
            }

            in = response.getEntity().getStream();
            out = new BufferedOutputStream( new FileOutputStream( downloadedFile ) );

            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ( ( numRead = in.read( buffer ) ) != -1 )
            {
                out.write( buffer, 0, numRead );
                numWritten += numRead;
            }
        }
        finally
        {
            try
            {
                if ( out != null )
                {
                    out.close();
                }
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( IOException e )
            {
            }
        }
        return downloadedFile;
    }

    public static int executeHTTPClientMethod( URL url, HttpMethod method )
        throws HttpException, IOException
    {

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout( 5000 );

        // check the text context to see if this is a secure test
        TestContext context = TestContainer.getInstance().getTestContext();
        if ( context.isSecureTest() )
        {
            client.getState().setCredentials(
                                              AuthScope.ANY,
                                              new UsernamePasswordCredentials( context.getUsername(),
                                                                               context.getPassword() ) );

            List<String> authPrefs = new ArrayList<String>( 1 );
            authPrefs.add( AuthPolicy.BASIC );
            client.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
            client.getParams().setAuthenticationPreemptive( true );
            
        }
        try
        {
            return client.executeMethod( method );
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public static AuthenticationInfo getWagonAuthenticationInfo()
    {
        AuthenticationInfo authInfo = null;
        // check the text context to see if this is a secure test
        TestContext context = TestContainer.getInstance().getTestContext();
        if ( context.isSecureTest() )
        {
            authInfo = new AuthenticationInfo();
            authInfo.setUserName( context.getUsername() );
            authInfo.setPassword( context.getPassword() );
        }
        return authInfo;
    }

}
