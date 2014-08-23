package org.sonatype.nexus.componentviews.responses;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * @since 3.0
 */
public class RedirectResponse
    extends StatusResponse
{
  private final String location;

  public RedirectResponse(final int statusCode, final String location) {
    super(statusCode, null);
    this.location = location;
  }

  @Override
  public void send(final HttpServletResponse response) throws IOException {
    response.setHeader("Location", location);
    super.send(response);
  }

  public static RedirectResponse movedPermanently(String location) {
    return new RedirectResponse(HttpServletResponse.SC_MOVED_PERMANENTLY, location);
  }

  public static RedirectResponse movedTemporarily(String location) {
    return new RedirectResponse(HttpServletResponse.SC_MOVED_TEMPORARILY, location);
  }
}
