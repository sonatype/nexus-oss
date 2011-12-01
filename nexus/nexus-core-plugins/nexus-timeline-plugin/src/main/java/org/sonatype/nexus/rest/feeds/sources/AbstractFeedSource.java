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
package org.sonatype.nexus.rest.feeds.sources;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

/**
 * And abstract class for NexusArtifactEvent based feeds. This class implements all needed to create a feed,
 * implementors needs only to implement 3 abtract classes.
 * 
 * @author cstamas
 */
public abstract class AbstractFeedSource
    extends AbstractLoggingComponent
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
