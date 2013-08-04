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

package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * A default factory for UIDs.
 *
 * @author cstamas
 */
@Component(role = RepositoryItemUidFactory.class)
public class DefaultRepositoryItemUidFactory
    extends AbstractRepositoryItemUidFactory
{
  /**
   * The registry.
   */
  @Requirement
  private RepositoryRegistry repositoryRegistry;

  @Override
  public DefaultRepositoryItemUid createUid(final String uidStr)
      throws IllegalArgumentException, NoSuchRepositoryException
  {
    if (uidStr.indexOf(":") > -1) {
      String[] parts = uidStr.split(":");

      if (parts.length == 2) {
        Repository repository = repositoryRegistry.getRepository(parts[0]);

        return createUid(repository, parts[1]);
      }
      else {
        throw new IllegalArgumentException(uidStr
            + " is malformed RepositoryItemUid! The proper format is '<repoId>:/path/to/something'.");
      }
    }
    else {
      throw new IllegalArgumentException(uidStr
          + " is malformed RepositoryItemUid! The proper format is '<repoId>:/path/to/something'.");
    }
  }
}
