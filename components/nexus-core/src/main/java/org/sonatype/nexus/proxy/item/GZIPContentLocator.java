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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link ContentLocator} that wraps another content locator that contains GZ compressed content.
 *
 * @author cstamas
 * @since 2.4
 */
public class GZIPContentLocator
    extends AbstractWrappingContentLocator
{
  private final String mimeType;

  /**
   * Constructor.
   *
   * @param gzippedContent the wrapped {@link ContentLocator}.
   * @param mimeType       the MIME type of the uncompressed content (NOT the gzipped stream, but the uncompressed
   *                       content!)
   */
  public GZIPContentLocator(final ContentLocator gzippedContent, final String mimeType) {
    super(gzippedContent);
    this.mimeType = checkNotNull(mimeType);
  }

  @Override
  public InputStream getContent()
      throws IOException
  {
    return new GZIPInputStream(super.getContent());
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }
}
