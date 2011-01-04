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
package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = RepositoryFolderRemover.class )
public class DefaultRepositoryFolderRemover
    implements RepositoryFolderRemover
{
    @Requirement
    private Logger logger;

    @Requirement( role = RepositoryFolderCleaner.class )
    private Map<String, RepositoryFolderCleaner> cleaners;

    protected Logger getLogger()
    {
        return logger;
    }

    public void deleteRepositoryFolders( final Repository repository, final boolean deleteForever )
        throws IOException
    {
        getLogger().debug(
            "Removing folders of repository \"" + repository.getName() + "\" (ID=" + repository.getId() + ")" );

        for ( RepositoryFolderCleaner cleaner : cleaners.values() )
        {
            try
            {
                cleaner.cleanRepositoryFolders( repository, deleteForever );
            }
            catch ( Exception e )
            {
                getLogger().warn(
                    "Got exception during execution of RepositoryFolderCleaner " + cleaner.getClass().getName()
                        + ", continuing.", e );
            }
        }
    }
}
