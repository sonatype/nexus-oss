/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.tasks;

import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.nexus.tasks.descriptors.AbstractIndexTaskDescriptor;

/**
 * @author cstamas
 */
public abstract class AbstractIndexerTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{
    /**
     * System event action: reindex
     */
    public static final String ACTION = "REINDEX";

    @Requirement( role = ReindexTaskHandler.class )
    private List<ReindexTaskHandler> handlers;

    private String action;

    private boolean fullReindex;

    public AbstractIndexerTask( String action, boolean fullReindex )
    {
        super();
        this.action = action;
        this.fullReindex = fullReindex;
    }

    @Override
    protected String getRepositoryFieldId()
    {
        return AbstractIndexTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected String getRepositoryPathFieldId()
    {
        return AbstractIndexTaskDescriptor.RESOURCE_STORE_PATH_FIELD_ID;
    }

    @Override
    public Object doRun()
        throws Exception
    {
        for ( ReindexTaskHandler handler : handlers )
        {
            try
            {
                if ( getRepositoryId() != null )
                {
                    handler.reindexRepository( getRepositoryId(), getResourceStorePath(), fullReindex );
                }
                else
                {
                    handler.reindexAllRepositories( getResourceStorePath(), fullReindex );
                }
            }
            catch ( NoSuchRepositoryException nsre )
            {
                // TODO: When we get to implement NEXUS-3977/NEXUS-1002 we'll be able to stop the indexing task when the
                // repo is deleted, so this exception handling/warning won't be needed anymore.
                if ( getRepositoryId() != null )
                {
                    getLogger().warn(
                        "Repository with ID=\""
                            + getRepositoryId()
                            + "\" was not found. It's likely that the repository was deleted while either the repair or the update index task was running." );
                }

                throw nsre;
            }
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
            return action + " repository index \"" + getRepositoryName() + "\" from path " + getResourceStorePath()
                + " and below.";
        }
        else
        {
            return action + " all registered repositories index";
        }
    }

}