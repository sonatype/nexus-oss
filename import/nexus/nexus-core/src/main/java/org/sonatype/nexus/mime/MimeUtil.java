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
package org.sonatype.nexus.mime;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * A simple component that hides MIME detection code. Singular methods returns the "best" applicable MIME type, while
 * plural methods returns all detected MIME types in ascending order.
 * 
 * @author cstamas
 * @deprecated Use {@link MimeSupport} instead.
 */
public interface MimeUtil
{
    /**
     * Returns the most applicable MIME type from "guessed" ones, based on path (usually extension).
     * 
     * @param fileName
     * @return
     * @deprecated Use {@link #guessMimeTypeFromPath(String)} instead.
     */
    String getMimeType( String fileName );

    /**
     * See {@link #getMimeType(String)}.
     * 
     * @param file
     * @return
     * @deprecated Use {@link #guessMimeTypeFromPath(String)} instead.
     */
    String getMimeType( File file );

    /**
     * Returns the most applicable MIME type from "guessed" ones, based on path portion of URL (usually extension).
     * 
     * @param url
     * @return
     * @deprecated Use {@link #guessMimeTypeFromPath(String)} instead.
     */
    String getMimeType( URL url );

    /**
     * Returns the most applicable MIME type from detected ones, based on stream content (using "magic" matching).
     * 
     * @param is
     * @return
     * @deprecated Use {@link #detectMimeTypesFromContent(StorageFileItem)} instead.
     */
    String getMimeType( InputStream is );

    /**
     * Returns all matched MIME types from "guessed" ones, based on path (usually extension).
     * 
     * @param fileName
     * @return
     * @deprecated No replacement.
     */
    Set<String> getMimeTypes( String fileName );

    /**
     * Returns all matched MIME types from "guessed" ones, based on path (usually extension).
     * 
     * @param fileName
     * @return
     * @deprecated No replacement.
     */
    Set<String> getMimeTypes( File file );

    /**
     * Returns all matched MIME types from "guessed" ones, based on path (usually extension).
     * 
     * @param fileName
     * @return
     * @deprecated No replacement.
     */
    Set<String> getMimeTypes( URL url );

    /**
     * Returns all the MIME types from detected ones, based on stream content (using "magic" matching).
     * 
     * @param fileName
     * @return
     * @deprecated No replacement.
     */
    Set<String> getMimeTypes( InputStream is );
}
