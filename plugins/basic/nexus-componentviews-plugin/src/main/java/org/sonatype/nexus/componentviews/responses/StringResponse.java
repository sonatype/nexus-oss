package org.sonatype.nexus.componentviews.responses;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @since 3.0
 */
public class StringResponse
    extends ContentResponse
{
  private final String content;

  public StringResponse(final String content, final String mimeType) {
    super(mimeType, null);
    this.content = content;
  }

  @Override
  protected InputStream getInputStream() {
    return new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
  }

}
