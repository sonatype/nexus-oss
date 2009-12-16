package org.sonatype.appcontext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;

/**
 * A factory class, that executes request and returns the response.
 * 
 * @author cstamas
 */
public class AppContextFactory
{
    public Interpolator getInterpolator( Map<Object, Object> ctx )
    {
        // interpolate what we have
        Interpolator interpolator = new RegexBasedInterpolator();

        interpolator.addValueSource( new MapBasedValueSource( ctx ) );
        interpolator.addValueSource( new MapBasedValueSource( System.getProperties() ) );

        return interpolator;
    }

    public AppContextRequest getDefaultAppContextRequest()
    {
        return new DefaultAppContextRequest();
    }

    public AppContext getAppContext( AppContextRequest request )
        throws AppContextException
    {
        // environment is a map of properties that comes from "environment": env vars and JVM system properties.
        // Keys found in this map are collected in this order, and the latter added will always replace any pre-existing
        // key:
        //
        // - basedir is put initially
        // - env vars
        // - system properties (will "stomp" env vars)
        //
        // As next step, the plexus.properties file is searched. If found, it will be loaded and filtered out for any
        // key that exists in environment map, and finally interpolation will be made against the "union" of those two.
        // The interpolation sources used in interpolation are: plexusProperties, environment and
        // System.getProperties().
        // The final interpolated values are put into containerContext map and returned.
        
        File basedir = request.getBasedirDiscoverer().discoverBasedir();

        Map<Object, Object> rawContext = new HashMap<Object, Object>();

        for ( ContextFiller filler : request.getContextFillers() )
        {
            filler.fillContext( request, rawContext );
        }
        
        // interpolate what we have
        Interpolator interpolator = getInterpolator( rawContext );

        Map<Object, Object> context = new HashMap<Object, Object>();

        // interpolate
        try
        {
            for ( Object key : rawContext.keySet() )
            {
                context.put( key, interpolator.interpolate( (String) rawContext.get( key ) ) );
            }
        }
        catch ( InterpolationException e )
        {
            throw new AppContextException( "Cannot interpolate the raw context!", e );
        }

        AppContext result = new DefaultAppContext( this, request.getName(), basedir, context, rawContext );

        // Now that we have containerContext with proper values, set them back into System properties and
        // dump them to System.out for reference.
        for ( ContextPublisher publisher : request.getContextPublishers() )
        {
            publisher.publishContext( request, result );
        }

        return result;
    }
}
