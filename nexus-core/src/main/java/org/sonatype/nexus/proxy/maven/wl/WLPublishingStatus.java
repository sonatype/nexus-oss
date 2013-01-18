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

import static com.google.common.base.Preconditions.checkNotNull;

public class WLPublishingStatus
{
    public static enum PStatus
    {
        PUBLISHED, NOT_PUBLISHED;
    }

    private final PStatus status;

    private final long lastPublishedTimestamp;

    private final String lastPublishedFilePath;

    public WLPublishingStatus( final PStatus status, final long lastPublishedTimestamp,
                               final String lastPublishedFilePath )
    {
        this.status = checkNotNull( status );
        this.lastPublishedTimestamp = lastPublishedTimestamp;
        this.lastPublishedFilePath = lastPublishedFilePath;
    }

    public PStatus getStatus()
    {
        return status;
    }

    public long getLastPublishedTimestamp()
    {
        if ( getStatus() == PStatus.PUBLISHED )
        {
            return lastPublishedTimestamp;
        }
        else
        {
            return -1;
        }
    }

    public String getLastPublishedFilePath()
    {
        if ( getStatus() == PStatus.PUBLISHED )
        {
            return lastPublishedFilePath;
        }
        else
        {
            return null;
        }
    }
}
