/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.feeds.sources;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

/**
 * And abstract class for NexusArtifactEvent based feeds. This class implements all needed to create a feed,
 * implementors needs only to implement 3 abtract classes.
 * 
 * @author cstamas
 */
public abstract class AbstractFeedSource
    extends AbstractLogEnabled
    implements FeedSource
{
    @Requirement
    private Nexus nexus;
    
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    protected Nexus getNexus()
    {
        return nexus;
    }
    
    protected RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    public abstract String getTitle();

    public abstract String getDescription();
}
