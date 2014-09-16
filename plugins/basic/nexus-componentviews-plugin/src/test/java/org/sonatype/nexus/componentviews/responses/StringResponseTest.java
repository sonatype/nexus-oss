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
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StringResponseTest
{
  @Test
  public void noCharacterEncoding() throws Exception {
    // Default encoding
    checkEncoding("text/plain", "text/plain", "UTF-8");
  }

  @Test
  public void nonDefaultCharacterEncoding() throws Exception {
    // Specifying UTF-8. Does lowercase work okay?
    checkEncoding("text/plain;charset=utf-8", "text/plain;charset=utf-8", "utf-8");

    // Trying a non-default encoding, utf-16
    checkEncoding("text/plain;charset=utf-16", "text/plain;charset=utf-16", "utf-16");
  }

  private void checkEncoding(final String specifiedType, final String expectedHeader, final String expectedByteEncoding)
      throws IOException
  {
    final TestableStringResponse stringResponse = new TestableStringResponse("hi mom", specifiedType);

    final HttpServletResponse mock = mock(HttpServletResponse.class);
    when(mock.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

    stringResponse.send(mock);
    verify(mock).setContentType(expectedHeader);

    assertThat(stringResponse.streamedCharset, is(equalTo(expectedByteEncoding)));
  }

  private class TestableStringResponse
      extends StringResponse
  {
    String streamedCharset = null;

    private TestableStringResponse(final String content, final String contentType) {
      super(content, contentType);
    }

    @Override
    protected ByteArrayInputStream createStream(final String charset) {
      streamedCharset = charset;
      return super.createStream(charset);
    }
  }
}
