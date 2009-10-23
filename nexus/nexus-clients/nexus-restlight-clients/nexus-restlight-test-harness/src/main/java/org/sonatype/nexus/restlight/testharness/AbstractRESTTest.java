package org.sonatype.nexus.restlight.testharness;

import static junit.framework.Assert.fail;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.sonatype.nexus.restlight.common.NxBasicScheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract test class that provides convenience methods for reading XML request/response documents from various
 * locations, managing the test-harness server, and initializing the test fixture used when a RESTLight client connects
 * and loads its vocabulary information.
 */
public abstract class AbstractRESTTest
{

    protected static final String TEST_NX_API_VERSION_SYSPROP = "test.nexus.api.version";

    private static final String DEFAULT_TEST_NX_API_VERSION = "1.3.2";

    protected static final String DEFAULT_EXPECTED_USER = "testuser";

    protected static final String DEFAULT_EXPECTED_PASSWORD = "password";

    /**
     * <p>
     * The test fixture MUST NOT change during the test.
     * </p>
     * <p>
     * Return the {@link RESTTestFixture} instance that sets expectations for HTTP exchanges between the RESTLight
     * client under test and the mockup Nexus instance, the test-harness server used here.
     * </p>
     */
    protected abstract RESTTestFixture getTestFixture();

    protected void setupAuthentication( final HttpClient client )
    {
        setupAuthentication( client, getExpectedUser(), getExpectedPassword() );
    }

    protected void setupAuthentication( final HttpClient client, final String user, final String password )
    {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials( user, password );

        List<String> policies = new ArrayList<String>();
        policies.add( NxBasicScheme.POLICY_NAME );

        AuthPolicy.registerAuthScheme( NxBasicScheme.POLICY_NAME, NxBasicScheme.class );

        client.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, policies );

        client.getState().setCredentials( AuthScope.ANY, creds );
    }

    protected String getExpectedUser()
    {
        return DEFAULT_EXPECTED_USER;
    }

    protected String getExpectedPassword()
    {
        return DEFAULT_EXPECTED_PASSWORD;
    }

    /**
     * Retrieve the base URL used by the test-harness server. This will serve as the base Nexus URL for tests.
     */
    protected final String getBaseUrl()
    {
        return "http://127.0.0.1:" + getTestFixture().getPort();
    }

    /**
     * Load a {@link Document} instance from a classpath resource with the given path. This document normally is used as
     * either a request or response body in a {@link RESTTestFixture} instance.
     */
    protected final Document readTestDocumentResource( final String resourcePath )
    throws JDOMException, IOException
    {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( resourcePath );
        if ( stream == null )
        {
            fail( "Cannot find test resource: '" + resourcePath + "'" );
        }

        try
        {
            return new SAXBuilder().build( stream );
        }
        finally
        {
            if ( stream != null )
            {
                try
                {
                    stream.close();
                }
                catch ( IOException e )
                {
                    System.out.println( "Failed to close stream to classpath resource: " + resourcePath );
                }
            }
        }
    }

    /**
     * Load a {@link Document} instance from the given {@link File}. This document normally is used as either a request
     * or response body in a {@link RESTTestFixture} instance.
     */
    protected final Document readTestDocumentFile( final File file )
    throws JDOMException, IOException
    {
        FileInputStream stream = null;
        try
        {
            stream = new FileInputStream( file );

            return new SAXBuilder().build( stream );
        }
        finally
        {
            if ( stream != null )
            {
                try
                {
                    stream.close();
                }
                catch ( IOException e )
                {
                    System.out.println( "Failed to close stream to file: " + file );
                }
            }
        }
    }

    /**
     * Construct a new {@link GETFixture} that represents the expectations the Nexus server should have for retrieving
     * the Nexus API version (to allow the client to load the proper REST vocabulary variable set).
     */
    protected final GETFixture getVersionCheckFixture()
    {
        GETFixture fixture = new GETFixture( getExpectedUser(), getExpectedPassword() );

        Document doc = new Document().setRootElement( new Element( "status" ) );
        Element data = new Element( "data" );

        data.addContent( new Element( "apiVersion" ).setText( getTestNexusAPIVersion() ) );
        doc.getRootElement().addContent( data );

        fixture.setExactURI( "/service/local/status" );
        fixture.setResponseDocument( doc );

        return fixture;
    }

    /**
     * Retrieve the Nexus API version we're testing, either using the System property <code>'test.nexus.api.version'</code>, or else using the default
     * value of <code>'1.3.1'</code> if no System property is supplied.
     */
    protected String getTestNexusAPIVersion()
    {
        return System.getProperty( TEST_NX_API_VERSION_SYSPROP, DEFAULT_TEST_NX_API_VERSION );
    }

    /**
     * Start the test-harness server. This is a Nexus mock instance, driven by expectations set in the
     * {@link RESTTestFixture} supplied by the {@link AbstractRESTTest#getTestFixture()} method.
     */
    @Before
    public void startServer()
    throws Exception
    {
        getTestFixture().startServer();
    }

    /**
     * Stop the test-harness server that acts as the Nexus mock instance.
     */
    @After
    public void stopServer()
    throws Exception
    {
        getTestFixture().stopServer();
    }

}
