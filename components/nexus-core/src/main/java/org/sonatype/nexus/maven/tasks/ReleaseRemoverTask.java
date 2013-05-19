/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.maven.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.maven.tasks.descriptors.ReleaseRemovalTaskDescriptor;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

/**
 * @since 2.5
 */
@Component( role = SchedulerTask.class, hint = ReleaseRemovalTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class ReleaseRemoverTask extends AbstractNexusRepositoriesTask<ReleaseRemovalResult>
{

    @Requirement
    private ReleaseRemover releaseRemover;

    @Override
    protected String getRepositoryFieldId()
    {
        return ReleaseRemovalTaskDescriptor.REPOSITORY_FIELD_ID;
    }

    @Override
    protected ReleaseRemovalResult doRun()
        throws Exception
    {
        int numberOfVersionsToKeep = Integer.parseInt(
            getParameter( ReleaseRemovalTaskDescriptor.NUMBER_OF_VERSIONS_TO_KEEP_FIELD_ID ) );
        String targetId = getParameter( ReleaseRemovalTaskDescriptor.REPOSITORY_TARGET_FIELD_ID );
        return releaseRemover.removeReleases(
            new ReleaseRemovalRequest( getRepositoryId(), numberOfVersionsToKeep, targetId ) );
    }

    @Override
    protected String getAction()
    {
        return getClass().getSimpleName();
    }

    @Override
    protected String getMessage()
    {
        return "Removing old releases from repository " + getRepositoryName();
    }
}
