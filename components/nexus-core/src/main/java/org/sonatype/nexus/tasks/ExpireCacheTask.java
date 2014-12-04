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
package org.sonatype.nexus.tasks;

import javax.inject.Named;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.RepositoryTaskSupport;

/**
 * Clear caches task.
 */
@Named
public class ExpireCacheTask
    extends RepositoryTaskSupport<Void>
{
  @Override
  public Void execute()
      throws Exception
  {
    ResourceStoreRequest req = new ResourceStoreRequest(getConfiguration().getPath());

    if (getConfiguration().getRepositoryId() != null) {
      getRepositoryRegistry().getRepository(getConfiguration().getRepositoryId()).expireCaches(req);
    }
    else {
      for (Repository repository : getRepositoryRegistry().getRepositories()) {
        if (repository.getLocalStatus().shouldServiceRequest()) {
          repository.expireCaches(req);
        }
      }
    }

    return null;
  }

  @Override
  public String getMessage() {
    if (getConfiguration().getRepositoryId() != null) {
      return "Expiring caches for repository " + getConfiguration().getRepositoryId() + " from path " +
          getConfiguration().getPath()
          + " and below.";
    }
    else {
      return "Expiring caches for all registered repositories from path " + getConfiguration().getPath()
          + " and below.";
    }
  }
}
