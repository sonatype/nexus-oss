/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.scheduling.RepositoryTaskSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 2.5
 */
@Named
public class ReleaseRemovalTask
    extends RepositoryTaskSupport<ReleaseRemovalResult>
{

  private final ReleaseRemover releaseRemover;

  @Inject
  public ReleaseRemovalTask(final ReleaseRemover releaseRemover)
  {
    this.releaseRemover = checkNotNull(releaseRemover);
  }

  @Override
  protected ReleaseRemovalResult execute()
      throws Exception
  {
    int numberOfVersionsToKeep = getConfiguration()
        .getInteger(ReleaseRemovalTaskDescriptor.NUMBER_OF_VERSIONS_TO_KEEP_FIELD_ID, 0);
    String targetId = getConfiguration().getString(ReleaseRemovalTaskDescriptor.REPOSITORY_TARGET_FIELD_ID, null);
    return releaseRemover.removeReleases(
        new ReleaseRemovalRequest(getConfiguration().getRepositoryId(), numberOfVersionsToKeep, targetId));
  }

  @Override
  public String getMessage() {
    return "Removing old releases from repository " + getConfiguration().getRepositoryId();
  }
}
