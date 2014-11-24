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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.RepositoryTaskSupport;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Evicts unused proxied items.
 */
@Named
public class EvictUnusedProxiedItemsTask
    extends RepositoryTaskSupport<Collection<String>>
{
  @Override
  protected Collection<String> execute()
      throws Exception
  {
    ResourceStoreRequest req = new ResourceStoreRequest("/");

    final int olderThanDays = getConfiguration()
        .getInteger(EvictUnusedProxiedItemsTaskDescriptor.OLDER_THAN_FIELD_ID, -1);
    checkArgument(olderThanDays > -1);

    long olderThan = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(olderThanDays);

    if (getConfiguration().getRepositoryId() != null) {
      return getRepositoryRegistry().getRepository(getConfiguration().getRepositoryId())
          .evictUnusedItems(req, olderThan);
    }
    else {
      ArrayList<String> result = Lists.newArrayList();

      for (Repository repository : getRepositoryRegistry().getRepositories()) {
        result.addAll(repository.evictUnusedItems(req, olderThan));
      }

      return result;
    }
  }

  @Override
  protected String getMessage() {
    if (getConfiguration().getRepositoryId() != null) {
      return "Evicting unused proxied items for repository " + getConfiguration().getRepositoryId() + ".";
    }
    else {
      return "Evicting unused proxied items for all registered proxy repositories.";
    }
  }

}
