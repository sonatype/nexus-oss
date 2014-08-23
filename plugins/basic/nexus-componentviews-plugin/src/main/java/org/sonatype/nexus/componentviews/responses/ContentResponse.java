package org.sonatype.nexus.componentviews.responses;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

/**
 * A response sending an expected bit of content.
 *
 * @since 3.0
 */
public abstract class ContentResponse
    implements Response
{
  private final String mimeType;

  private final Date modifiedDate;

  protected ContentResponse(final String mimeType, final Date modifiedDate) {
    this.mimeType = mimeType;
    this.modifiedDate = modifiedDate;
  }

  protected abstract InputStream getInputStream();

  @Override
  public void send(final HttpServletResponse response) throws IOException {
    response.setStatus(200);
    // TODO: Set last modified headers
    if (modifiedDate != null) {
      response.setDateHeader("Last-Modified", modifiedDate.getTime());
    }
    response.setContentType(mimeType);

    ByteStreams.copy(getInputStream(), response.getOutputStream());
  }
}
