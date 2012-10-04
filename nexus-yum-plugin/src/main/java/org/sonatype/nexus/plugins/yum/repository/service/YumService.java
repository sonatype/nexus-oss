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
package org.sonatype.nexus.plugins.yum.repository.service;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.scheduling.ScheduledTask;

public interface YumService
{
    ScheduledTask<YumRepository> createYumRepository( Repository repository );

    ScheduledTask<YumRepository> createYumRepository( Repository repository, String version, File yumRepoDir,
                                                      URL yumRepoUrl );

    YumRepository getRepository( Repository repository, String version, URL repoBaseUrl )
        throws Exception;

    void markDirty( Repository repository, String itemVersion );

    ScheduledTask<YumRepository> createYumRepository( File rpmBaseDir, String rpmBaseUrl, File yumRepoBaseDir,
                                                      URL yumRepoUrl, String id, boolean singleRpmPerDirectory );

    ScheduledTask<YumRepository> addToYumRepository( Repository repository, String path );

    void recreateRepository( Repository repository );

    ScheduledTask<YumRepository> createGroupRepository( GroupRepository groupRepository );
}
