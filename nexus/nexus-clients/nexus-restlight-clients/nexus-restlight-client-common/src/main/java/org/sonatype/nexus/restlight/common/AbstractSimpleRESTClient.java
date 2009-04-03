package org.sonatype.nexus.restlight.common;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.LogManager;
import org.codehaus.plexus.util.IOUtil;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public abstract class AbstractSimpleRESTClient
{

    public static final String SVC_BASE = "/service/local";

    protected static final String STATUS_PATH = SVC_BASE + "/status";

    protected static final String GROUP_ID_PARAM = "g";

    protected static final String ARTIFACT_ID_PARAM = "a";

    protected static final String VERSION_PARAM = "v";

    protected static final String VOCAB_MANIFEST = "vocabulary.lst";

    private final String baseUrl;

    private final String user;

    private final String password;

    private HttpClient client;

    private Properties vocabulary;

    protected AbstractSimpleRESTClient( String baseUrl, String user, String password )
        throws SimpleRESTClientException
    {
        this.baseUrl = baseUrl;
        this.user = user;
        this.password = password;

        connect();
    }
    
    protected final String getBaseURL()
    {
        return baseUrl;
    }

    protected final String getUser()
    {
        return user;
    }

    private void connect()
        throws SimpleRESTClientException
    {
        client = new HttpClient();

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials( user, password );

        List<String> policies = new ArrayList<String>();
        policies.add( "NxBASIC" );

        AuthPolicy.registerAuthScheme( "NxBASIC", NxBasicScheme.class );

        client.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, policies );

        client.getState().setCredentials( AuthScope.ANY, creds );

        loadVocabulary();
    }

    protected final Properties getVocabulary()
    {
        return vocabulary;
    }

    private void loadVocabulary()
        throws SimpleRESTClientException
    {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        InputStream stream = cloader.getResourceAsStream( VOCAB_MANIFEST );

        if ( stream == null )
        {
            throw new SimpleRESTClientException( "Cannot locate REST vocabulary variants manifest on classpath: "
                + VOCAB_MANIFEST );
        }

        Map<String, List<String>> vocabManifest = new HashMap<String, List<String>>();
        try
        {
            Properties vProps = new Properties();
            vProps.load( stream );
            for ( Map.Entry<Object, Object> lstEntry : vProps.entrySet() )
            {
                List<String> lst = new ArrayList<String>();

                if ( lstEntry.getValue() == null )
                {
                    continue;
                }

                StringTokenizer tokens = new StringTokenizer( (String) lstEntry.getValue() );
                while ( tokens.hasMoreTokens() )
                {
                    lst.add( tokens.nextToken().trim() );
                }

                vocabManifest.put( (String) lstEntry.getKey(), lst );
            }
        }
        catch ( IOException e )
        {
            throw new SimpleRESTClientException( "Failed to load REST vocabulary manifest from classpath: "
                + VOCAB_MANIFEST, e );
        }
        finally
        {
            IOUtil.close( stream );
        }

        String version = getApiVersion();
        if ( version.endsWith( "-SNAPSHOT" ) )
        {
            version = version.substring( 0, version.length() - "-SNAPSHOT".length() );
        }

        List<String> vocabs = vocabManifest.get( version );
        if ( vocabs == null )
        {
            vocabs = vocabManifest.get( "default" );
        }

        Properties loadedVocabulary = new Properties();
        for ( String vocab : vocabs )
        {
            Properties props = new Properties();

            String vocabResource = vocab + ".vocabulary.properties";

            stream = cloader.getResourceAsStream( vocabResource );
            try
            {
                props.load( stream );
            }
            catch ( IOException e )
            {
                throw new SimpleRESTClientException( "Failed to load REST vocabulary from classpath: " + vocabResource,
                                                     e );
            }

            loadedVocabulary.putAll( props );
        }

        this.vocabulary = loadedVocabulary;
    }

    public String getApiVersion()
        throws SimpleRESTClientException
    {
        Document doc = get( STATUS_PATH );
        String xpath = "/status/data/apiVersion/text()";
        XPath xp;
        try
        {
            xp = XPath.newInstance( xpath );
        }
        catch ( JDOMException e )
        {
            throw new SimpleRESTClientException( "Failed to build xpath: '" + xpath + "'.", e );
        }

        Text result;
        try
        {
            result = (Text) xp.selectSingleNode( doc.getRootElement() );
        }
        catch ( JDOMException e )
        {
            throw new SimpleRESTClientException( "XPath selection failed: '" + xpath + "' (Root node: "
                + doc.getRootElement().getName() + ").", e );
        }

        return result.getText();
    }

    protected void mapCoord( String groupId, String artifactId, String version, Map<String, String> params )
    {
        params.put( GROUP_ID_PARAM, groupId );
        params.put( ARTIFACT_ID_PARAM, artifactId );
        params.put( VERSION_PARAM, version );
        params.put( "t", "maven2" );
    }

    protected Document getAbsolute( String url )
        throws SimpleRESTClientException
    {
        return get( url, null, true );
    }

    protected Document getAbsolute( String url, Map<String, ? extends Object> requestParams )
        throws SimpleRESTClientException
    {
        return get( url, requestParams, true );
    }

    protected Document get( String path )
        throws SimpleRESTClientException
    {
        return get( path, null, false );
    }

    protected Document get( String path, Map<String, ? extends Object> requestParams )
        throws SimpleRESTClientException
    {
        return get( path, requestParams, false );
    }

    @SuppressWarnings( "unchecked" )
    protected Document get( String url, Map<String, ? extends Object> requestParams, boolean urlIsAbsolute )
        throws SimpleRESTClientException
    {
        GetMethod method = urlIsAbsolute ? new GetMethod( url ) : new GetMethod( baseUrl + url );

        if ( requestParams != null )
        {
            for ( Map.Entry<String, ? extends Object> entry : requestParams.entrySet() )
            {
                if ( entry.getValue() instanceof Collection )
                {
                    Collection<Object> values = (Collection<Object>) entry.getValue();
                    if ( values != null && !values.isEmpty() )
                    {
                        for ( Object val : values )
                        {
                            if ( val != null )
                            {
                                method.addRequestHeader( entry.getKey(), String.valueOf( val ) );
                            }
                        }
                    }
                }
                else
                {
                    method.addRequestHeader( entry.getKey(), String.valueOf( entry.getValue() ) );
                }
            }
        }

        try
        {
            client.executeMethod( method );
        }
        catch ( HttpException e )
        {
            throw new SimpleRESTClientException( "GET request execution failed. Reason: " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new SimpleRESTClientException( "GET request execution failed. Reason: " + e.getMessage(), e );
        }

        int status = method.getStatusCode();
        String statusText = method.getStatusText();

        if ( status != 200 )
        {
            throw new SimpleRESTClientException( "GET request failed; HTTP status: " + status + ": " + statusText );
        }

        try
        {
            return new SAXBuilder().build( method.getResponseBodyAsStream() );
        }
        catch ( JDOMException e )
        {
            throw new SimpleRESTClientException( "Failed to parse response body as XML for GET request.", e );
        }
        catch ( IOException e )
        {
            throw new SimpleRESTClientException( "Could not retrieve body as a String from GET request.", e );
        }
        finally
        {
            method.releaseConnection();
        }
    }

    protected void post( String path, Map<String, ? extends Object> requestParams, Document body )
        throws SimpleRESTClientException
    {
        doPost( path, requestParams, body, false );
    }

    protected Document postWithResponse( String path, Map<String, ? extends Object> requestParams, Document body )
        throws SimpleRESTClientException
    {
        return doPost( path, requestParams, body, true );
    }

    /**
     * @param expectResponseBody
     * @return null if expectResponseBody == false, the result {@link Document} otherwise.
     * @throws SimpleRESTClientException
     */
    @SuppressWarnings( "unchecked" )
    protected Document doPost( String path, Map<String, ? extends Object> requestParams, Document body,
                               boolean expectResponseBody )
        throws SimpleRESTClientException
    {
        LogManager.getLogger( getClass().getName() ).debug( "Posting to: '" + path + "'" );

        PostMethod method = new PostMethod( baseUrl + path );
        method.addRequestHeader( "Content-Type", "application/xml" );

        if ( body != null && body.getRootElement() != null )
        {
            try
            {
                method.setRequestEntity( new StringRequestEntity(
                                                                  new XMLOutputter( Format.getCompactFormat() ).outputString( body ),
                                                                  "text/plain", "UTF-8" ) );
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new SimpleRESTClientException( "Failed to construct POST request. Reason: " + e.getMessage(), e );
            }
        }

        if ( requestParams != null )
        {
            for ( Map.Entry<String, ? extends Object> entry : requestParams.entrySet() )
            {
                if ( entry.getValue() instanceof Collection )
                {
                    Collection<Object> values = (Collection<Object>) entry.getValue();
                    if ( values != null && !values.isEmpty() )
                    {
                        for ( Object val : values )
                        {
                            if ( val != null )
                            {
                                method.addRequestHeader( entry.getKey(), String.valueOf( val ) );
                            }
                        }
                    }
                }
                else
                {
                    method.addRequestHeader( entry.getKey(), String.valueOf( entry.getValue() ) );
                }
            }
        }

        try
        {
            client.executeMethod( method );
        }
        catch ( HttpException e )
        {
            throw new SimpleRESTClientException( "POST request execution failed. Reason: " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new SimpleRESTClientException( "POST request execution failed. Reason: " + e.getMessage(), e );
        }

        int status = method.getStatusCode();
        String statusText = method.getStatusText();

        if ( status < 200 || status > 299 )
        {
            throw new SimpleRESTClientException( "POST request failed; HTTP status: " + status + ": " + statusText );
        }

        if ( expectResponseBody )
        {
            try
            {
                return new SAXBuilder().build( method.getResponseBodyAsStream() );
            }
            catch ( JDOMException e )
            {
                throw new SimpleRESTClientException( "Failed to parse response body as XML for POST request.", e );
            }
            catch ( IOException e )
            {
                throw new SimpleRESTClientException( "Could not retrieve body as a String from POST request.", e );
            }
            finally
            {
                method.releaseConnection();
            }
        }
        else
        {
            method.releaseConnection();

            return null;
        }
    }

}
