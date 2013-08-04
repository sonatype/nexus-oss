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
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.sonatype.nexus.proxy.attributes.AbstractStorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Class ZipFileInspector inspects ZIP files and collects directory listings from it. The findings are stored as
 * attributes. Turned OFF, since nexus-indexer is doing this too.
 *
 * @author cstamas #plexus.component role-hint="zip"
 */
public class ZipFileInspector
    extends AbstractStorageFileItemInspector
{

  /**
   * The ZIP_FILES.
   */
  public static String ZIP_FILES = "zip.files";

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.attributes.StorageItemInspector#isHandled(org.sonatype.nexus.item.StorageItem)
   */
  public boolean isHandled(StorageItem item) {
    return StorageFileItem.class.isAssignableFrom(item.getClass())
        && item.getName().toLowerCase().endsWith("zip");
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.attributes.StorageItemInspector#getIndexableKeywords()
   */
  public Set<String> getIndexableKeywords() {
    Set<String> result = new HashSet<String>(1);
    result.add(ZIP_FILES);
    return result;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.sonatype.nexus.attributes.StorageFileItemInspector#processStorageFileItem(org.sonatype.nexus.item.StorageFileItem
   * , java.io.File)
   */
  @SuppressWarnings("unchecked")
  public void processStorageFileItem(StorageFileItem item, File file)
      throws IOException
  {
    ZipFile zFile = new ZipFile(file);
    try {
      StringBuilder files = new StringBuilder(zFile.size());

      for (Enumeration e = zFile.entries(); e.hasMoreElements(); ) {
        ZipEntry entry = (ZipEntry) e.nextElement();
        if (!entry.isDirectory()) {
          files.append(entry.getName());
          files.append("\n");
        }
      }

      item.getRepositoryItemAttributes().put(ZIP_FILES, files.toString());
      // result.setBoolean( LocalStorageFileItem.LOCAL_FILE_IS_CONTAINER_KEY, true );
    }
    finally {
      zFile.close();
    }
  }

}
