/*
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
package org.sonatype.nexus.mime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.item.ContentLocator;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Closeables;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

/**
 * Default implementation of {@link MimeSupport} component using MimeUtil2 library and the
 * {@link NexusExtensionMimeDetector}.
 * 
 * @since 2.0
 */
@Component( role = MimeSupport.class )
public class DefaultMimeSupport
    extends AbstractLoggingComponent
    implements MimeSupport
{
    private final MimeUtil2 nonTouchingMimeUtil;

    private final MimeUtil2 touchingMimeUtil;

    /**
     * A "cache" to be used with {@link #nonTouchingMimeUtil}. As that instance of MimeUtil2 uses only one mime detector
     * registered by us, the {@link NexusExtensionMimeDetector}. Hence, even if the
     * {@link #guessMimeTypeFromPath(String)} and other methods talk about paths, we know they actually deal with file
     * extensions only (deduces the MIME type from file extension). This map simply caches the responses from MimeUtil2,
     * as it's operation is a bit heavy weight (congestion happens on synchronized {@link Properties} instance deeply
     * buried in MimeUtil2 classes), and also, modifications to extension MIME type mapping is not possible without
     * restarting JVM where MimeUtil2. The cache is keyed with extensions, values are MIME types (represented as
     * strings).
     */
    private final LoadingCache<String, String> extensionToMimeTypeCache;

    /**
     * See {@link NexusMimeTypes} for customizations
     */
    public DefaultMimeSupport()
    {
        // MimeUtil2 by design will start (try to) read the file/stream if some "eager" detector is registered
        // so we follow the "private instance" pattern, and we handle two instances for now

        // uses Extension only for now (speed, no IO, but less accuracy)
        nonTouchingMimeUtil = new MimeUtil2();
        nonTouchingMimeUtil.registerMimeDetector( NexusExtensionMimeDetector.class.getName() );

        // uses magic-mime (IO and lower speed but more accuracy)
        // See src/main/resources/magic.mime for customizations
        touchingMimeUtil = new MimeUtil2();
        touchingMimeUtil.registerMimeDetector( MagicMimeMimeDetector.class.getName() );

        // create the cache
        extensionToMimeTypeCache =
            CacheBuilder.newBuilder().maximumSize( 500 ).build( new CacheLoader<String, String>()
            {
                @Override
                public String load( final String key )
                    throws Exception
                {
                    // FIXME (by replacing MimeUtil with something else?)
                    // MimeUtil2#getMostSpecificMimeType is broken in 2.1.2/2.1.3, it will (in contrast to it's javadoc)
                    // *usually*
                    // return the last
                    // mime type regardless of specificity. Which one is last depends on the impl of
                    // HashSet<String>.iterator()
                    // (which seems to have a fairly stable ordering on JVM: different order breaks unit tests.)
                    //
                    // TODO: Hack alert: as with introduction of cache, loading cache will be invoked with extension got from 
                    // the path only. As code reading showed, MimeUtil2 will do similarly, as only extension MimeDetector is registered
                    // Still, we make a "fake" filename, just to not bork any existing logic or expectancies in MimeUtil2. Still
                    // it is the extension that matters of this "dummy" file.
                    return MimeUtil2.getMostSpecificMimeType( getNonTouchingMimeUtil2().getMimeTypes( "dummyfile." + key ) ).toString();
                }
            } );
    }

    protected MimeUtil2 getNonTouchingMimeUtil2()
    {
        return nonTouchingMimeUtil;
    }

    protected MimeUtil2 getTouchingMimeUtil2()
    {
        return touchingMimeUtil;
    }

    @Override
    public String guessMimeTypeFromPath( final MimeRulesSource mimeRulesSource, final String path )
    {
        if ( mimeRulesSource != null )
        {
            final String hardRule = mimeRulesSource.getRuleForPath( path );

            if ( !Strings.isNullOrEmpty( hardRule ) )
            {
                return hardRule;
            }
        }

        return guessMimeTypeFromPath( path );
    }

    @Override
    public String guessMimeTypeFromPath( final String path )
    {
        // even if we got path as param, the "non touching" mimeutil2 uses extensions only
        // see constructor how it is configured
        // Note: using same method to get extension as MimeUtil2's MimeDetectors would
        final String pathExtension = MimeUtil2.getExtension( path );
        try
        {
            return extensionToMimeTypeCache.get( pathExtension );
        }
        catch ( ExecutionException e )
        {
            Throwables.propagate( e );
            // only to make compiler happy, execution will never get here
            return null;
        }
    }

    @Override
    public Set<String> guessMimeTypesFromPath( final String path )
    {
        return toStringSet( getNonTouchingMimeUtil2().getMimeTypes( path ) );
    }

    @Override
    public Set<String> detectMimeTypesFromContent( final ContentLocator content )
        throws IOException
    {
        Set<String> magicMimeTypes = new HashSet<String>();
        BufferedInputStream bis = null;
        try
        {
            magicMimeTypes.addAll( toStringSet( getTouchingMimeUtil2().getMimeTypes(
                bis = new BufferedInputStream( content.getContent() ) ) ) );
        }
        finally
        {
            Closeables.closeQuietly( bis );
        }
        return magicMimeTypes;
    }

    // ==

    @SuppressWarnings( "unchecked" )
    private Set<String> toStringSet( final Collection<?> mimeTypes )
    {
        Set<String> result = new HashSet<String>();
        for ( MimeType mimeType : (Collection<MimeType>) mimeTypes )
        {
            result.add( mimeType.toString() );
        }
        return result;
    }
}
