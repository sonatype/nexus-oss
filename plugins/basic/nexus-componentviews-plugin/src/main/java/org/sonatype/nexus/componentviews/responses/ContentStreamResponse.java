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
package org.sonatype.nexus.componentviews.responses;

import java.io.InputStream;
import java.util.Date;

/**
 * A response whose body is provided by an {@link InputStream}.
 *
 * @since 3.0
 */
public class ContentStreamResponse
    extends ContentResponse
{
  private final InputStream inputStream;

  public ContentStreamResponse(final InputStream inputStream, final String contentType, final Date modifiedDate) {
    super(contentType, modifiedDate);
    this.inputStream = inputStream;
  }

  @Override
  protected InputStream getInputStream() {
    return inputStream;
  }
}
