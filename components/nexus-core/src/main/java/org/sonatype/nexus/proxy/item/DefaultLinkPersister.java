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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;

@Component(role = LinkPersister.class)
public class DefaultLinkPersister
    implements LinkPersister
{
  private static final String UTF8_CHARSET = "UTF-8";

  private static final String LINK_PREFIX = "LINK to ";

  private static final byte[] LINK_PREFIX_BYTES = LINK_PREFIX.getBytes(Charset.forName(UTF8_CHARSET));

  @Requirement
  private RepositoryItemUidFactory repositoryItemUidFactory;

  public boolean isLinkContent(final ContentLocator locator)
      throws IOException
  {
    if (locator != null) {
      final byte[] buf = ContentLocatorUtils.getFirstBytes(LINK_PREFIX_BYTES.length, locator);

      if (buf != null) {
        return Arrays.equals(buf, LINK_PREFIX_BYTES);
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  public RepositoryItemUid readLinkContent(final ContentLocator locator)
      throws NoSuchRepositoryException, IOException
  {
    if (locator != null) {
      InputStream fis = null;

      try {
        fis = locator.getContent();

        final String linkBody = IOUtil.toString(fis, UTF8_CHARSET);

        final String uidStr = linkBody.substring(LINK_PREFIX.length(), linkBody.length());

        return repositoryItemUidFactory.createUid(uidStr);
      }
      finally {
        IOUtil.close(fis);
      }
    }
    else {
      return null;
    }
  }

  public void writeLinkContent(final StorageLinkItem link, final OutputStream os)
      throws IOException
  {
    try {
      final String linkBody = LINK_PREFIX + link.getTarget().toString();

      IOUtil.copy(new ByteArrayInputStream(linkBody.getBytes(UTF8_CHARSET)), os);

      os.flush();
    }
    finally {
      IOUtil.close(os);
    }
  }
}
