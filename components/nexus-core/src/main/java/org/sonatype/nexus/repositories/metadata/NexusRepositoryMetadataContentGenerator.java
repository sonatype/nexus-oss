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

package org.sonatype.nexus.repositories.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;

@Component(role = ContentGenerator.class, hint = NexusRepositoryMetadataContentGenerator.ID)
public class NexusRepositoryMetadataContentGenerator
    implements ContentGenerator
{
  public static final String ID = "NexusRepositoryMetadataContentGenerator";

  @Override
  public String getGeneratorId() {
    return ID;
  }

  @Override
  public ContentLocator generateContent(Repository repository, String path, StorageFileItem item)
      throws IllegalOperationException, ItemNotFoundException, StorageException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    InputStream is = null;
    try {
      is = item.getInputStream();

      IOUtil.copy(is, bos);
      String body = new String(bos.toByteArray(), "UTF-8");
      StringContentLocator result = null;
      if (item.getItemContext().getRequestAppRootUrl() != null) {
        String appRootUrl = item.getItemContext().getRequestAppRootUrl();
        // trim last slash NEXUS-1736
        if (appRootUrl.endsWith("/")) {
          appRootUrl = appRootUrl.substring(0, appRootUrl.length() - 1);
        }
        result = new StringContentLocator(body.replace("@rootUrl@", appRootUrl));
      }
      else {
        result = new StringContentLocator(body.replace("@rootUrl@", ""));
      }
      return result;
    }
    catch (IOException e) {
      throw new LocalStorageException(e);
    }
    finally {
      IOUtil.close(is);
    }
  }
}
