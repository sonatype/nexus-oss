package org.sonatype.nexus.repository.view;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

/**
 * ???
 *
 * @since 3.0
 */
public class StreamPayload
  implements Payload
{
  private final String contentType;

  private final long size;

  private final InputStream stream;

  public StreamPayload(final String contentType, final long size, final InputStream stream) {
    this.contentType = contentType;
    this.size = size;
    this.stream = stream;
  }

  @Nullable
  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public long getSize() {
    return size;
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return stream;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "contentType='" + contentType + '\'' +
        ", size=" + size +
        ", stream=" + stream +
        '}';
  }
}
