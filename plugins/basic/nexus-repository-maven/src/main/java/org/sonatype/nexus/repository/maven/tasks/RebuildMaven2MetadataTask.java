/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.maven.tasks;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.maven.MavenHostedFacet;
import org.sonatype.nexus.scheduling.TaskSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.maven.tasks.RebuildMaven2MetadataTaskDescriptor.ARTIFACTID_FIELD_ID;
import static org.sonatype.nexus.repository.maven.tasks.RebuildMaven2MetadataTaskDescriptor.BASEVERSION_FIELD_ID;
import static org.sonatype.nexus.repository.maven.tasks.RebuildMaven2MetadataTaskDescriptor.GROUPID_FIELD_ID;
import static org.sonatype.nexus.repository.maven.tasks.RebuildMaven2MetadataTaskDescriptor.REPOSITORY_NAME_FIELD_ID;

/**
 * Maven 2 metadata rebuild task.
 *
 * @since 3.0
 */
@Named
public class RebuildMaven2MetadataTask
    extends TaskSupport
{
  private final RepositoryManager repositoryManager;

  @Inject
  public RebuildMaven2MetadataTask(final RepositoryManager repositoryManager) {
    this.repositoryManager = checkNotNull(repositoryManager);
  }

  @Override
  protected Object execute() throws Exception {
    Repository repository = repositoryManager.get(repositoryName());
    checkNotNull(repository);
    MavenHostedFacet mavenHostedFacet = repository.facet(MavenHostedFacet.class);
    mavenHostedFacet.rebuildMetadata(
        getConfiguration().getString(GROUPID_FIELD_ID),
        getConfiguration().getString(ARTIFACTID_FIELD_ID),
        getConfiguration().getString(BASEVERSION_FIELD_ID)
    );
    return null;
  }

  @Override
  public String getMessage() {
    return "Rebuilding Maven Metadata of " + repositoryName();
  }

  private String repositoryName() {
    return getConfiguration().getString(REPOSITORY_NAME_FIELD_ID);
  }
}
