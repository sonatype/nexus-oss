package org.sonatype.nexus.repository.raw.internal;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;

/**
 * Converts between {@link Payload} and {@link RawContent}
 *
 * @since 3.0
 */
public class RawContentPayloadMarshaller
{
  private RawContentPayloadMarshaller() { }

  public static RawContent toContent(final Payload payload) {
    return new RawContent()
    {
      @Override
      public String getContentType() {
        return payload.getContentType();
      }

      @Override
      public long getSize() {
        return payload.getSize();
      }

      @Override
      public InputStream openInputStream() throws IOException {
        return payload.openInputStream();
      }
    };
  }

  public static Payload toPayload(RawContent content) throws IOException {
    return new StreamPayload(content.openInputStream(), content.getSize(), content.getContentType());
  }
}
