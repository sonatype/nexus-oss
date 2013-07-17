/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mime.detectors;

import java.io.File;

import org.sonatype.nexus.mime.DefaultMimeSupport;

import com.google.common.base.Strings;

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.detector.OpendesktopMimeDetector;

/**
 * Nexus specific {@link OpendesktopMimeDetector}. It detects file override and will use the override or the default
 * accordingly.
 * 
 * @author cstamas
 * @since 2.6.1
 */
public class NexusOpendesktopMimeDetector
    extends OpendesktopMimeDetector
{
    /**
     * This method ensures that either override file path or "default" file path exists on system, as if they don't
     * exists, {@link OpendesktopMimeDetector} will try (for some unknown reason) the
     * {@code src/main/resources/mime.cache} path, and will die with confusing reasoning
     * "File src/main/resources/mime.cache not found!", that will confuse anyone trying to override the location of MIME
     * file, as that path was never set by the user (and the path set, and maybe mistyped, will never actually be shown
     * in exception).
     * 
     * @see <a
     *      href="http://sourceforge.net/p/mime-util/code/123/tree/trunk/MimeUtil/src/main/java/eu/medsea/mimeutil/detector/OpendesktopMimeDetector.java#l97">OpendesktopMimeDetector#init
     *      method</a>
     * @return a file path that exists for sure, to avoid fallback (and possible exception) to the
     *         {@code src/main/resources/mime.cache} path.
     */
    private static String getFilePathToUseIfExistsOrDie()
    {
        // chooses override path or default path. Default path copied from OpendesktopMimeDetector source.
        final String path =
            Strings.isNullOrEmpty( DefaultMimeSupport.MIME_MAGIC_FILE ) ? "/usr/share/mime/mime.cache"
                : DefaultMimeSupport.MIME_MAGIC_FILE;
        if ( !new File( path ).isFile() )
        {
            throw new MimeException( "MIME database file not found on path " + path );
        }
        return path;
    }

    public NexusOpendesktopMimeDetector()
    {
        super( getFilePathToUseIfExistsOrDie() );
    }
}
