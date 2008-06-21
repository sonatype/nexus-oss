package org.sonatype.nexus.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.gwt.client.request.RESTRequestBuilder;
import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.gwt.client.nexus.DefaultNexusRestApi;

import com.google.gwt.core.client.GWT;

/**
 * The Nexus REST API root.
 * 
 * @author cstamas
 */
public class Nexus
    extends DefaultResource
{
    public static final String LOCAL_INSTANCE_NAME = "local";

    private Variant defaultVariant;

    private Map namedInstances = new HashMap();

    /**
     * Instantiate Nexus resource instance using <code>GWT.getHostPageBaseURL()</code>. This presumes that the GWT
     * UI is served from the Nexus web application root.
     */
    public Nexus()
    {
        super( GWT.getHostPageBaseURL() + "service" );
    }

    /**
     * Instantiate Nexus resource instance from custom URL.
     * 
     * @param url
     */
    public Nexus( String url )
    {
        super( url );
    }

    /**
     * Instantiate Nexus resource instance with existing RESTRequestBuilder and known path.
     * 
     * @param path
     * @param requestBuilder
     */
    public Nexus( String path, RESTRequestBuilder requestBuilder )
    {
        super( path, requestBuilder );
    }

    /**
     * Returns the Nexus local instance.
     * 
     * @return
     */
    public NexusRestApi getLocalInstance()
    {
        return getNamedInstance( LOCAL_INSTANCE_NAME );
    }

    /**
     * Returns a named Nexus instance.
     * 
     * @param name
     * @return
     */
    public NexusRestApi getNamedInstance( String name )
    {
        if ( !namedInstances.containsKey( name ) )
        {
            namedInstances.put( name, new DefaultNexusRestApi( this, name ) );
        }
        return (NexusRestApi) namedInstances.get( name );
    }

    /**
     * Returns the default Variant used to communicate to Nexus REST API.
     * 
     * @return
     */
    public Variant getDefaultVariant()
    {
        if ( defaultVariant == null )
        {
            defaultVariant = Variant.APPLICATION_JSON;
        }
        return defaultVariant;
    }

    /**
     * Sets the default Variant used to communicate to Nexus REST API.
     * 
     * @param defaultVariant
     */
    public void setDefaultVariant( Variant defaultVariant )
    {
        this.defaultVariant = defaultVariant;
    }

}
