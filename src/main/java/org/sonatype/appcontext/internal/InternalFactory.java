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
import org.sonatype.appcontext.publisher.PrintStreamEntryPublisher;
import org.sonatype.appcontext.source.EntrySource;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.Sources;

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

        return interpolator;
    }

    public static AppContextRequest getDefaultAppContextRequest( final String id, final AppContext parent,
                                                                 final List<String> aliases )
    {
        Preconditions.checkNotNull( id );
        Preconditions.checkNotNull( aliases );

        List<EntrySource> sources = Sources.getDefaultSources( id, aliases );
        List<EntryPublisher> publishers = Arrays.asList( new EntryPublisher[] { new PrintStreamEntryPublisher() } );

        return new AppContextRequest( id, parent, sources, publishers, true );
    }

    public static AppContext create( final AppContextRequest request )
        throws AppContextException
    {
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

        if ( request.isUseSystemPropertiesFallback() )
        {
            // make Java System properties participate in interpolation but as last resort, kinda "fallback"
            interpolator.addValueSource( new MapBasedValueSource( System.getProperties() ) );
        }

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
