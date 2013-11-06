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

package org.sonatype.nexus.proxy.utils;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.repository.Repository;

import org.codehaus.plexus.util.StringUtils;

/**
 * Simple utils regarding stores.
 *
 * @author cstamas
 */
public class ResourceStoreUtils
{

  /**
   * A simple utility to "format" a list of stores for logging or other output.
   */
  public static String getResourceStoreListAsString(List<? extends ResourceStore> stores) {
    if (stores == null) {
      return "[]";
    }

    ArrayList<String> repoIdList = new ArrayList<String>(stores.size());

    for (ResourceStore store : stores) {
      if (store instanceof Repository) {
        repoIdList.add(((Repository) store).getId());
      }
      else {
        repoIdList.add(store.getClass().getName());
      }
    }

    return StringUtils.join(repoIdList.iterator(), ", ");
  }

}
