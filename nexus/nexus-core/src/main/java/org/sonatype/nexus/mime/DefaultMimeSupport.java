/**
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
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.item.ContentLocator;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

/**
 * Default implementation of {@link MimeSupport} component using MimeUtil2 library.
 * 
 * @author cstamas
 * @since 2.0
 */
@Component( role = MimeSupport.class )
public class DefaultMimeSupport
    extends AbstractLoggingComponent
    implements MimeSupport
{
    private final MimeUtil2 nonTouchingMimeUtil;

    private final MimeUtil2 touchingMimeUtil;

    public DefaultMimeSupport()
    {
        // MimeUtil2 by design will start (try to) read the file/stream if some "eager" detector is registered
        // so we follow the "private instance" pattern, and we handle two instances for now

        // uses Extension only for now (speed, no IO, but less accuracy)
        // See src/main/resources/mime-types.properties for customizations
        nonTouchingMimeUtil = new MimeUtil2();
        nonTouchingMimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );

        // uses magic-mime (IO and lower speed but more accuracy)
        // See src/main/resources/magic.mime for customizations
        touchingMimeUtil = new MimeUtil2();
        touchingMimeUtil.registerMimeDetector( MagicMimeMimeDetector.class.getName() );
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
        return MimeUtil2.getMostSpecificMimeType( getNonTouchingMimeUtil2().getMimeTypes( path ) ).toString();
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
