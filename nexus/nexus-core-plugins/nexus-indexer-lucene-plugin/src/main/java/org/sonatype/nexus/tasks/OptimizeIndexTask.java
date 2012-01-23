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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.nexus.tasks.descriptors.OptimizeIndexTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * OptimizeIndex task.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = OptimizeIndexTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class OptimizeIndexTask
    extends AbstractNexusRepositoriesTask<Object>
{
    /**
     * System event action: optimize index
     */
    public static final String ACTION = "OPTIMIZE_INDEX";

    @Requirement
    private IndexerManager indexManager;

    @Override
    protected String getRepositoryFieldId()
    {
        return OptimizeIndexTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    public Object doRun()
        throws Exception
    {
        if ( getRepositoryId() != null )
        {
            indexManager.optimizeRepositoryIndex( getRepositoryId() );
        }
        else
        {
            indexManager.optimizeAllRepositoriesIndex();
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
            return "Optimizing repository " + getRepositoryName() + " index.";
        }
        else
        {
            return "Optimizing all maven repositories indexes";
        }
    }

}
