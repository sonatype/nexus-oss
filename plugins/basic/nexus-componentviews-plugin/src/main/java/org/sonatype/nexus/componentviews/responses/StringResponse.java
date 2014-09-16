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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import com.google.common.base.Throwables;

/**
 * A response whose body is defined by a String.
 *
 * @since 3.0
 */
public class StringResponse
    extends ContentResponse
{
  private final String content;

  private ContentType parsedContentType;

  /**
   * If the contentType includes a charset, the content will be encoded using that character set (presuming it's
   * available on the platform).
   */
  public StringResponse(final String content, final String contentType) {
    super(contentType, null);
    this.content = content;
    try {
      if (contentType != null) {
        parsedContentType = new ContentType(contentType);
      }
    }
    catch (ParseException e) {
      Throwables.propagate(e);
    }
  }

  @Override
  protected InputStream getInputStream() {
    String charset = determineCharset();
    return createStream(charset);
  }

  protected ByteArrayInputStream createStream(
      final String charset)
  {
    return new ByteArrayInputStream(content.getBytes(Charset.forName(charset)));
  }

  private String determineCharset() {
    if (parsedContentType != null) {
      final String charset = parsedContentType.getParameter("charset");
      if (charset != null) {
        return charset;
      }
    }

    return "UTF-8";
  }
}
