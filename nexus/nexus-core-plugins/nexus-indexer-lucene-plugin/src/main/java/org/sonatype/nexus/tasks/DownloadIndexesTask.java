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
package org.sonatype.nexus.tasks;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.nexus.tasks.descriptors.DownloadIndexesTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Publish indexes task.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = DownloadIndexesTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class DownloadIndexesTask
    extends AbstractNexusRepositoriesTask<Object>
{
    /**
     * System event action: download indexes
     */
    public static final String ACTION = "DOWNLOADINDEX";

    @Requirement
    private IndexerManager indexerManager;

    @Override
    protected String getRepositoryFieldId()
    {
        return DownloadIndexesTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        try
        {
            if ( getRepositoryId() != null )
            {
                indexerManager.downloadRepositoryIndex( getRepositoryId() );
            }
            else
            {
                indexerManager.downloadAllIndex();
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot download indexes!", e );
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return ACTION;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryId() != null )
        {
            return "Downloading indexes for repository " + getRepositoryName();
        }
        else
        {
            return "Downloading indexes for all registered repositories";
        }
    }

}
