package org.sonatype.nexus.componentviews.responses;

import java.io.InputStream;
import java.util.Date;

/**
 * @since 3.0
 */
public class ContentStreamResponse
    extends ContentResponse
{
  private final InputStream inputStream;

  public ContentStreamResponse(final InputStream inputStream, final String mimeType, final Date modifiedDate) {
    super(mimeType, modifiedDate);
    this.inputStream = inputStream;
  }

  @Override
  protected InputStream getInputStream() {
    return inputStream;
  }


}
