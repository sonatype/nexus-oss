/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mime;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

@Component( role = MimeUtil.class )
public class DefaultMimeUtil
    implements MimeUtil
{
    private MimeUtil2 nonTouchingMimeUtil;

    private MimeUtil2 touchingMimeUtil;

    public DefaultMimeUtil()
    {
        // MimeUtil2 by design will start (try to) read the file/stream if some "eager" detector is registered
        // so we follow the "private instance" pattern, and we handle two instances for now
        
        // uses Extension only for now (speed, no IO, but less accuracy)
        nonTouchingMimeUtil = new MimeUtil2();
        nonTouchingMimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );

        // uses magic-mime (IO and lower speed but more accuracy)
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
    public String getMimeType( String fileName )
    {
        return MimeUtil2.getMostSpecificMimeType( getNonTouchingMimeUtil2().getMimeTypes( fileName ) ).toString();
    }

    @Override
    public String getMimeType( File file )
    {
        return MimeUtil2.getMostSpecificMimeType( getNonTouchingMimeUtil2().getMimeTypes( file ) ).toString();
    }

    @Override
    public String getMimeType( URL url )
    {
        return MimeUtil2.getMostSpecificMimeType( getNonTouchingMimeUtil2().getMimeTypes( url ) ).toString();
    }

    @Override
    public String getMimeType( InputStream is )
    {
        return MimeUtil2.getMostSpecificMimeType( getTouchingMimeUtil2().getMimeTypes( is ) ).toString();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( String fileName )
    {
        return this.toStringSet( getNonTouchingMimeUtil2().getMimeTypes( fileName ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( File file )
    {
        return this.toStringSet( getNonTouchingMimeUtil2().getMimeTypes( file ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( URL url )
    {
        return this.toStringSet( getNonTouchingMimeUtil2().getMimeTypes( url ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( InputStream is )
    {
        return toStringSet( getTouchingMimeUtil2().getMimeTypes( is ) );
    }

    // ==

    private Set<String> toStringSet( Collection<MimeType> mimeTypes )
    {
        Set<String> result = new HashSet<String>();
        for ( MimeType mimeType : mimeTypes )
        {
            result.add( mimeType.toString() );
        }
        return result;
    }
}
