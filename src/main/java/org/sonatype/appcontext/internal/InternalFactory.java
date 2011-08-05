package org.sonatype.appcontext.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;
import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.publisher.EntryPublisher;
import org.sonatype.appcontext.publisher.Slf4jLoggerEntryPublisher;
import org.sonatype.appcontext.publisher.SystemPropertiesEntryPublisher;
import org.sonatype.appcontext.source.EntrySource;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.FilteredEntrySource;
import org.sonatype.appcontext.source.KeyPrefixEntryFilter;
import org.sonatype.appcontext.source.SystemEnvironmentEntrySource;
import org.sonatype.appcontext.source.SystemPropertiesEntrySource;

public class InternalFactory
{
    public static Interpolator getInterpolator( final Map<String, Object> context )
    {
        // interpolate what we have
        final Interpolator interpolator = new RegexBasedInterpolator();

        if ( context != null )
        {
            interpolator.addValueSource( new MapBasedValueSource( context ) );
        }
        // interpolator.addValueSource( new MapBasedValueSource( System.getProperties() ) );

        return interpolator;
    }

    public static Interpolator getInterpolator( final AppContext ctx )
    {
        Interpolator interpolator = new RegexBasedInterpolator();
        interpolator.addValueSource( new MapBasedValueSource( ctx ) );
        return interpolator;
    }

    public static AppContextRequest getDefaultAppContextRequest( final String id, final AppContext parent )
    {
        Preconditions.checkNotNull( id );

        List<EntrySource> sources =
            Arrays.asList( new EntrySource[] {
                new FilteredEntrySource( new SystemEnvironmentEntrySource(), new KeyPrefixEntryFilter( id + "." ) ),
                new FilteredEntrySource( new SystemPropertiesEntrySource(), new KeyPrefixEntryFilter( id + "." ) ) } );
        List<EntryPublisher> publishers =
            Arrays.asList( new EntryPublisher[] { new SystemPropertiesEntryPublisher( id + "." ),
                new Slf4jLoggerEntryPublisher() } );

        return new AppContextRequest( id, parent, sources, publishers );
    }

    public static AppContext create( final AppContextRequest request )
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

        final Map<String, Object> rawContext = new HashMap<String, Object>();
        final Map<String, EntrySourceMarker> rawContextSourceMarkers = new HashMap<String, EntrySourceMarker>();

        for ( EntrySource source : request.getSources() )
        {
            for ( Map.Entry<String, Object> entry : source.getEntries( request ).entrySet() )
            {
                rawContext.put( entry.getKey(), entry.getValue() );
                rawContextSourceMarkers.put( entry.getKey(), source.getEntrySourceMarker() );
            }
        }

        // interpolate what we have
        final Interpolator interpolator = new RegexBasedInterpolator();

        if ( request.getParent() != null )
        {
            interpolator.addValueSource( new RawAppContextValueSource( request.getParent() ) );
            interpolator.addValueSource( new MapBasedValueSource( request.getParent() ) );
        }
        
        interpolator.addValueSource( new MapBasedValueSource( rawContext ) );

        Map<String, AppContextEntry> context = new HashMap<String, AppContextEntry>();

        // interpolate
        try
        {
            for ( String key : rawContext.keySet() )
            {
                final Object rawValue = rawContext.get( key );

                final Object value;

                if ( rawValue == null )
                {
                    value = null;
                }
                else if ( rawValue instanceof String )
                {
                    value = interpolator.interpolate( (String) rawValue );
                }
                else
                {
                    value = rawValue;
                }

                final AppContextEntry entry =
                    new AppContextEntryImpl( key, rawValue, value, rawContextSourceMarkers.get( key ) );

                context.put( key, entry );
            }
        }
        catch ( InterpolationException e )
        {
            throw new AppContextException( "Cannot interpolate the raw context!", e );
        }

        AppContext result = new AppContextImpl( request.getId(), (AppContextImpl) request.getParent(), context );

        for ( EntryPublisher publisher : request.getPublishers() )
        {
            publisher.publishEntries( result );
        }

        return result;
    }
}
