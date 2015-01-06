package org.sonatype.nexus.repository.raw;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;

import com.google.common.io.ByteStreams;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class RawContentImpl
    implements RawContent
{  private final String type;

  private final byte[] bytes;

  public RawContentImpl(final Payload payload) throws IOException {
    checkNotNull(payload);
    this.type = payload.getContentType();
    try (InputStream input = checkNotNull(payload.openInputStream())) {
      this.bytes = ByteStreams.toByteArray(input);
    }
  }

  @Override
  public String getContentType() {
    return type;
  }

  @Override
  public long getSize() {
    return bytes.length;
  }

  @Override
  public InputStream openInputStream() {
    return null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "type='" + type + '\'' +
        ", size=" + bytes.length +
        '}';
  }
}
