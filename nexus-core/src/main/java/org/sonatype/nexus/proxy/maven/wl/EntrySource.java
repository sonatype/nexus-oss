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
package org.sonatype.nexus.proxy.maven.wl;

import java.io.IOException;
import java.util.List;

/**
 * Entry source.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface EntrySource
{
    /**
     * Returns {@code true} if this entry source exists, hence, is readable.
     * 
     * @return {@code true} if entry source exists, {@code false} otherwise.
     */
    boolean exists();

    /**
     * Reads entries for this source, of {@code null} if not exists ({@link #exists()} returns {@code false} in this
     * case).
     * 
     * @return list of entries contained in this source, or {@code null} in no entries could be read.
     * @throws IOException
     */
    List<String> readEntries()
        throws IOException;

    /**
     * Returns the timestamp of this entry source. Based on implementation, this might mean different things: if backed
     * by file, the file timestamp, or the timestamp when enties were collected in some way, or when the entries was
     * generated in some way, etc. Simply put, the "when" point in time that this instance reflects. If not exists,
     * result is -1.
     * 
     * @return timestamp in millis of this entry source, or -1 if not exists.
     */
    long getLostModifiedTimestamp();
}
