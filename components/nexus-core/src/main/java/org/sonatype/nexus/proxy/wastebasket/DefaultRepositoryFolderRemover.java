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

package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;
import java.util.Map;

import org.sonatype.nexus.proxy.repository.Repository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = RepositoryFolderRemover.class)
public class DefaultRepositoryFolderRemover
    implements RepositoryFolderRemover
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Requirement(role = RepositoryFolderCleaner.class)
  private Map<String, RepositoryFolderCleaner> cleaners;

  public void deleteRepositoryFolders(final Repository repository, final boolean deleteForever)
      throws IOException
  {
    logger.debug("Removing folders of repository \"{}\" (ID={})", repository.getName(), repository.getId());

    for (RepositoryFolderCleaner cleaner : cleaners.values()) {
      try {
        cleaner.cleanRepositoryFolders(repository, deleteForever);
      }
      catch (Exception e) {
        logger.warn("Got exception during execution of RepositoryFolderCleaner {}, continuing.",
            cleaner.getClass().getName(), e);
      }
    }
  }
}
