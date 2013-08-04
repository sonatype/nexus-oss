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
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.proxy.attributes.AbstractStorageFileItemInspector;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

import org.apache.commons.codec.binary.Hex;
import org.codehaus.plexus.component.annotations.Component;

/**
 * The Class DigestCalculatingInspector calculates MD5 and SHA1 digests of a file and stores them into extended
 * attributes.
 *
 * @author cstamas
 */
@Component(role = StorageFileItemInspector.class, hint = "digest")
public class DigestCalculatingInspector
    extends AbstractStorageFileItemInspector
{

  /**
   * The digest md5 key.
   */
  @Deprecated
  public static String DIGEST_MD5_KEY = StorageFileItem.DIGEST_MD5_KEY;

  /**
   * The digest sha1 key.
   */
  public static String DIGEST_SHA1_KEY = StorageFileItem.DIGEST_SHA1_KEY;

  public Set<String> getIndexableKeywords() {
    Set<String> result = new HashSet<String>(2);
    result.add(DIGEST_MD5_KEY);
    result.add(DIGEST_SHA1_KEY);
    return result;
  }

  public boolean isHandled(StorageItem item) {
    if (item instanceof StorageFileItem) {
      if (item.getItemContext().containsKey(StorageFileItem.DIGEST_SHA1_KEY)) {
        item.getRepositoryItemAttributes().put(DIGEST_SHA1_KEY,
            String.valueOf(item.getItemContext().get(StorageFileItem.DIGEST_SHA1_KEY)));

        // do this one "blindly"
        item.getRepositoryItemAttributes().put(DIGEST_MD5_KEY,
            String.valueOf(item.getItemContext().get(StorageFileItem.DIGEST_MD5_KEY)));

        // we did our job, we "lifted" the digest from context
        return false;
      }

    }

    // handling all files otherwise
    return true;
  }

  public void processStorageFileItem(StorageFileItem item, File file)
      throws Exception
  {
    InputStream fis = new FileInputStream(file);
    try {
      byte[] buffer = new byte[1024];
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      MessageDigest sha1 = MessageDigest.getInstance("SHA1");
      int numRead;
      do {
        numRead = fis.read(buffer);
        if (numRead > 0) {
          md5.update(buffer, 0, numRead);
          sha1.update(buffer, 0, numRead);
        }
      }
      while (numRead != -1);
      String md5digestStr = new String(Hex.encodeHex(md5.digest()));
      String sha1DigestStr = new String(Hex.encodeHex(sha1.digest()));
      item.getRepositoryItemAttributes().put(DIGEST_MD5_KEY, md5digestStr);
      item.getRepositoryItemAttributes().put(DIGEST_SHA1_KEY, sha1DigestStr);
    }
    finally {
      fis.close();
    }
  }

}
