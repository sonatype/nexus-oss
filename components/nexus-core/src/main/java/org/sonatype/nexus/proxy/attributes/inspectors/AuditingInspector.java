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

package org.sonatype.nexus.proxy.attributes.inspectors;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.AbstractStorageFileItemInspector;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

import org.codehaus.plexus.component.annotations.Component;

/**
 * The Class AuditingInspector simply records the auth stuff from Item Context to attributes..
 *
 * @author cstamas
 */
@Component(role = StorageFileItemInspector.class, hint = "AuditingInspector")
public class AuditingInspector
    extends AbstractStorageFileItemInspector
{
  public Set<String> getIndexableKeywords() {
    Set<String> result = new HashSet<String>(2);
    result.add(AccessManager.REQUEST_USER);
    result.add(AccessManager.REQUEST_REMOTE_ADDRESS);
    result.add(AccessManager.REQUEST_CONFIDENTIAL);
    return result;
  }

  public boolean isHandled(StorageItem item) {
    if (item instanceof StorageFileItem) {
      final StorageFileItem fitem = (StorageFileItem) item;

      addIfExistsButDontContains(fitem, AccessManager.REQUEST_USER);

      addIfExistsButDontContains(fitem, AccessManager.REQUEST_REMOTE_ADDRESS);

      addIfExistsButDontContains(fitem, AccessManager.REQUEST_CONFIDENTIAL);
    }

    // don't do File copy for us, we done our job already
    return false;
  }

  public void processStorageFileItem(StorageFileItem item, File file)
      throws Exception
  {
    // noop
  }

  /**
   * Save it only 1st time. Meaning, a newly proxied/cached item will have not set these attributes, but when it
   * comes
   * from cache, it will. By storing it only once, at first time, we have the record of who did it initally
   * requested.
   */
  private void addIfExistsButDontContains(StorageFileItem item, String contextKey) {
    if (item.getItemContext().containsKey(contextKey) && !item.getRepositoryItemAttributes().containsKey(contextKey)) {
      Object val = item.getItemContext().get(contextKey);

      if (val != null) {
        item.getRepositoryItemAttributes().put(contextKey, val.toString());
      }
    }
  }

}
