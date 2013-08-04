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

package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.wastebasket.AbstractRepositoryFolderCleaner;
import org.sonatype.nexus.proxy.wastebasket.RepositoryFolderCleaner;

import org.codehaus.plexus.component.annotations.Component;

/**
 * TO BE REMOVED once we switch from FS based attribute storage to LS based attribute storage!
 *
 * @author cstamas
 */
@Component(role = RepositoryFolderCleaner.class, hint = "core-proxy-attributes")
public class AttributesRepositoryFolderCleaner
    extends AbstractRepositoryFolderCleaner
{

  @Override
  public void cleanRepositoryFolders(Repository repository, boolean deleteForever)
      throws IOException
  {
    File defaultProxyAttributesFolder =
        new File(new File(getApplicationConfiguration().getWorkingDirectory(), "proxy/attributes"),
            repository.getId());

    if (defaultProxyAttributesFolder.isDirectory()) {
      // attributes are not preserved
      delete(defaultProxyAttributesFolder, true);
    }
  }

}
