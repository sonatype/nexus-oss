package org.sonatype.nexus.componentviews.responses;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.componentviews.ViewResponse;

/**
 * A response primarily built around an HTTP return code.
 *
 * @since 3.0
 */
public class StatusResponse
    implements ViewResponse
{
  private final int statusCode;

  private final String errorMessage;

  public StatusResponse(final int statusCode, final String errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public StatusResponse(final int statusCode) {
    this(statusCode, null);
  }

  @Override
  public void send(final HttpServletResponse response) throws IOException {
    if (errorMessage != null) {
      response.sendError(statusCode, errorMessage);
    }
    else {
      response.sendError(statusCode);
    }
  }
}
