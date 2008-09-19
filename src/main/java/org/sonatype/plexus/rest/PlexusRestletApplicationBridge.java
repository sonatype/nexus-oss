/**
 * Copyright (C) 2008 Sonatype Inc. 
 * Sonatype Inc, licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.sonatype.plexus.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;

/**
 * An abstract Restlet.org application, that should be extended for custom application needs. It will automatically pick
 * up existing PlexusResources, but is also able to take the "old way" for creating application root. Supports out of
 * the box JSON and XML representations powered by XStream, and also offers help in File Upload handling.
 * 
 * @author cstamas
 */
public class PlexusRestletApplicationBridge
    extends Application
{
    /** Key to store JSON driver driven XStream */
    public static final String JSON_XSTREAM = "plexus.xstream.json";

    /** Key to store XML driver driven XStream */
    public static final String XML_XSTREAM = "plexus.xstream.xml";

    /** Key to store used Commons Fileupload FileItemFactory */
    public static final String FILEITEM_FACTORY = "plexus.fileItemFactory";

    /** Key to store the flag should plexus discover resource or no */
    public static final String PLEXUS_DISCOVER_RESOURCES = "plexus.discoverResources";

    /** Date of creation of this application */
    private final Date createdOn;

    /** The root that is changeable as-needed basis */
    private RetargetableRestlet root;

    private Map<String, PlexusResource> plexusResources;

    /**
     * Constructor.
     * 
     * @param context
     */
    public PlexusRestletApplicationBridge( Context context )
    {
        super( context );

        this.createdOn = new Date();
    }

    /**
     * Returns the timestamp of instantaniation of this object. This is used as timestamp for transient objects when
     * they are still unchanged (not modified).
     * 
     * @return date
     */
    public Date getCreatedOn()
    {
        return createdOn;
    }

    /**
     * Gets the Plexus from context.
     * 
     * @return
     */
    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getContext().getAttributes().get( PlexusConstants.PLEXUS_KEY );
    }

    /**
     * Puts the Plexus to context.
     * 
     * @param plexusContainer
     */
    public void setPlexusContainer( PlexusContainer plexusContainer )
    {
        getContext().getAttributes().put( PlexusConstants.PLEXUS_KEY, plexusContainer );
    }

    /**
     * Invoked from restlet.org Application once, to create root.
     */
    public final Restlet createRoot()
    {
        if ( root == null )
        {
            root = new RetargetableRestlet( getContext() );
        }

        configure();

        recreateRoot( true );

        return root;
    }

    /**
     * Creating all sort of shared tools and putting them into context, to make them usable by per-request
     * instantaniated Resource implementors.
     */
    protected final void configure()
    {
        // we are putting XStream into this Application's Context, since XStream is threadsafe
        // and it is safe to share it across multiple threads. XStream is heavily used by our
        // custom Representation implementation to support XML and JSON.

        // create and configure XStream for JSON
        XStream xstream = createAndConfigureXstream( new JsonOrgHierarchicalStreamDriver() );

        // for JSON, we use a custom converter for Maps
        xstream.registerConverter( new PrimitiveKeyedMapConverter( xstream.getMapper() ) );

        // put it into context
        getContext().getAttributes().put( JSON_XSTREAM, xstream );

        // create and configure XStream for XML
        xstream = createAndConfigureXstream( new LookAheadXppDriver() );

        // put it into context
        getContext().getAttributes().put( XML_XSTREAM, xstream );

        // put fileItemFactory into context
        getContext().getAttributes().put( FILEITEM_FACTORY, new DiskFileItemFactory() );

        boolean shouldCollectPlexusResources = getContext().getAttributes().containsKey( PLEXUS_DISCOVER_RESOURCES )
            ? Boolean.parseBoolean( (String) getContext().getAttributes().get( PLEXUS_DISCOVER_RESOURCES ) )
            : true; // the default if not set

        if ( shouldCollectPlexusResources )
        {
            try
            {
                // discover the plexusResources
                plexusResources = (Map<String, PlexusResource>) getPlexusContainer().lookupMap( PlexusResource.class );

                getLogger().info( "Discovered " + plexusResources.size() + " PlexusResource components." );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalStateException( "Cannot collect PlexusResources!", e );
            }
        }
        else
        {
            // create an empty map
            plexusResources = new HashMap<String, PlexusResource>();
        }

        doConfigure();
    }

    protected final void recreateRoot( boolean isStarted )
    {
        // reboot?
        if ( root != null )
        {
            // create a new root router
            Router rootRouter = new Router( getContext() );

            // attach all PlexusResources
            if ( isStarted )
            {
                for ( PlexusResource resource : plexusResources.values() )
                {
                    rootRouter.attach( resource.getResourceUri(), new PlexusResourceFinder( getContext(), resource ) );
                }
            }

            // allow "manual" resource attachment too
            doCreateRoot( rootRouter, isStarted );

            // set it
            root.setRoot( rootRouter );
        }
    }

    protected final XStream createAndConfigureXstream( HierarchicalStreamDriver driver )
    {
        XStream xstream = new XStream( driver );

        return doConfigureXstream( xstream );
    }

    // methods to override

    /**
     * Method to be overridden by subclasses. It will be called only once in the lifetime of this Application. This is
     * the place when you need to create and add to context some stuff.
     */
    protected void doConfigure()
    {
        // empty implementation, left for subclasses to do something meaningful
    }

    /**
     * Method to be overridden by subclasses. It will be called multiple times with multiple instances of XStream.
     * Configure it by adding aliases for used DTOs, etc.
     * 
     * @param xstream
     * @return
     */
    protected XStream doConfigureXstream( XStream xstream )
    {
        // default implementation does nothing, override if needed
        return xstream;
    }

    /**
     * Called when the app root needs to be created. Override it if you need "old way" to attach resources, or need to
     * use the isStarted flag.
     * 
     * @param root
     * @param isStarted
     */
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        // empty implementation, left for subclasses to do something meaningful
    }

}
