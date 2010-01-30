/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.util.Date;

public class IndexUpdateResult
{
    private Date timestamp;

    private boolean fullUpdate;

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp( Date timestamp )
    {
        this.timestamp = timestamp;
    }

    public void setFullUpdate( boolean fullUpdate )
    {
        this.fullUpdate = fullUpdate;
    }

    public boolean isFullUpdate()
    {
        return this.fullUpdate;
    }
}
