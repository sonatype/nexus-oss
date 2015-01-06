/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.events;

import java.util.Date;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The event that is occurred within a Repository, such as content changes or other maintenance stuff.
 *
 * @author cstamas
 */
public abstract class RepositoryEvent
{
  private final Repository repository;

  private final Date date;

  public RepositoryEvent(final Repository repository) {
    this.repository = repository;
    this.date = new Date();
  }

  /**
   * Gets the repository.
   *
   * @return the repository
   */
  public Repository getRepository() {
    return repository;
  }

  public Date getEventDate() {
    return date;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "repositoryId=" + getRepository().getId() +
        '}';
  }
}
