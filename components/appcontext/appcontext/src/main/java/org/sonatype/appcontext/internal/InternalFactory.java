/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.appcontext.internal;

import java.util.ArrayList;
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
import org.sonatype.appcontext.publisher.Slf4jLoggerEntryPublisher;
import org.sonatype.appcontext.source.EntrySource;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.MapEntrySource;
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
                                                                 final List<String> aliases,
                                                                 final String... keyInclusions )
    {
        Preconditions.checkNotNull( id );
        Preconditions.checkNotNull( aliases );

        final List<EntrySource> sources = new ArrayList<EntrySource>();

        if ( keyInclusions.length > 0 )
        {
            sources.addAll( Sources.getDefaultSelectTargetedSources( keyInclusions ) );
        }
        sources.addAll( Sources.getDefaultSources( id, aliases ) );

        // be smart with publishers. go for System.out only as last resort
        List<EntryPublisher> publishers;
        if ( isSlf4jPresentOnClasspath() )
        {
            publishers = Arrays.asList( new EntryPublisher[] { new Slf4jLoggerEntryPublisher() } );
        }
        else
        {
            publishers = Arrays.asList( new EntryPublisher[] { new PrintStreamEntryPublisher() } );
        }

        return new AppContextRequest( id, parent, sources, publishers, true, Arrays.asList( keyInclusions ) );
    }

    public static AppContext create( final AppContextRequest request )
        throws AppContextException
    {
        final long created = System.currentTimeMillis();
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

        interpolator.addValueSource( new MapBasedValueSource( rawContext ) );

        if ( request.getParent() != null )
        {
            interpolator.addValueSource( new RawAppContextValueSource( request.getParent() ) );
            interpolator.addValueSource( new MapBasedValueSource( request.getParent() ) );
        }

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
                    new AppContextEntryImpl( created, key, rawValue, value, rawContextSourceMarkers.get( key ) );

                context.put( key, entry );
            }
        }
        catch ( InterpolationException e )
        {
            throw new AppContextException( "Cannot interpolate the raw context!", e );
        }

        AppContext result =
            new AppContextImpl( created, request.getId(), (AppContextImpl) request.getParent(), context );

        for ( EntryPublisher publisher : request.getPublishers() )
        {
            publisher.publishEntries( result );
        }

        return result;
    }

    public static AppContext create( final String id, final AppContext parent, final Map<String, Object> map )
        throws AppContextException
    {
        final AppContextRequest request = getDefaultAppContextRequest( id, parent, new ArrayList<String>() );
        request.getPublishers().clear();
        request.getSources().clear();
        request.getSources().add( new MapEntrySource( id, map ) );
        return create( request );
    }

    // ==

    public static boolean isSlf4jPresentOnClasspath()
    {
        try
        {
            return Class.forName( "org.slf4j.LoggerFactory", false, InternalFactory.class.getClassLoader() ) != null;
        }
        catch ( ClassNotFoundException e )
        {
            return false;
        }
    }
}
