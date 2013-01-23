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
package org.sonatype.nexus.proxy.maven.wl.internal;

import java.io.IOException;

import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus;

/**
 * Utility to persist discovery results.
 * 
 * @author cstamas
 */
public interface DiscoveryStatusSource
{
    /**
     * Returns {@code true} if "last" results exists, or {@code false} if never run discovery yet.
     * 
     * @return {@code true} if "last" results exists, or {@code false} if never run discovery yet.
     */
    boolean exists();

    /**
     * Reads up the last discovery status.
     * 
     * @return last discovery status.
     * @throws IOException
     */
    WLDiscoveryStatus read()
        throws IOException;

    /**
     * Persists last discovery status.
     * 
     * @param discoveryStatus
     * @throws IOException
     */
    void write( WLDiscoveryStatus discoveryStatus )
        throws IOException;

    /**
     * Deletes last discovery status.
     * 
     * @throws IOException
     */
    void delete()
        throws IOException;
}
