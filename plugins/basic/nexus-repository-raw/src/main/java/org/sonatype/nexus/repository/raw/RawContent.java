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
public class RawContent
{  private final String type;

  private final byte[] bytes;

  public RawContent(final Payload payload) throws IOException {
    checkNotNull(payload);
    this.type = payload.getContentType();
    try (InputStream input = checkNotNull(payload.openInputStream())) {
      this.bytes = ByteStreams.toByteArray(input);
    }
  }

  public String getType() {
    return type;
  }

  public long getSize() {
    return bytes.length;
  }

  public Payload toPayload() {
    return new BytesPayload(bytes, type);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "type='" + type + '\'' +
        ", size=" + bytes.length +
        '}';
  }
}
