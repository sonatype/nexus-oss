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
package org.sonatype.nexus.scheduling;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

import com.google.common.base.Strings;

public abstract class AbstractNexusRepositoriesPathAwareTask<T>
    extends AbstractNexusRepositoriesTask<T>
{
  private static final String ROOT = RepositoryItemUid.PATH_ROOT;

  protected abstract String getRepositoryPathFieldId();

  public String getResourceStorePath() {
    final String id = getRepositoryPathFieldId();
    if (Strings.isNullOrEmpty(id)) {
      return ROOT;
    }
    final String path = getParameters().get(id);
    if (Strings.isNullOrEmpty(path)) {
      return ROOT;
    } else {
      return path;
    }
  }

  public void setResourceStorePath(String resourceStorePath) {
    String id = getRepositoryPathFieldId();

    if (id != null) {
      getParameters().put(id, resourceStorePath);
    }
  }

}
