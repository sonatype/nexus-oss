package org.sonatype.nexus.componentviews.responses;

import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.componentviews.ViewResponse;

/**
 * Factory methods for constructing various simple responses.
 *
 * @since 3.0
 */
public class Responses
{
  public static ViewResponse streamResponse(final InputStream inputStream, final String mimeType, final Date modifiedDate) {
    return new ContentStreamResponse(inputStream, mimeType, modifiedDate);
  }

  public static RedirectResponse movedPermanently(String location) {
    return new RedirectResponse(HttpServletResponse.SC_MOVED_PERMANENTLY, location);
  }

  public static RedirectResponse movedTemporarily(String location) {
    return new RedirectResponse(HttpServletResponse.SC_MOVED_TEMPORARILY, location);
  }

  public static final StatusResponse created() {
    return new StatusResponse(HttpServletResponse.SC_CREATED);
  }

  public static final StatusResponse notFound(String message) {
    return new StatusResponse(HttpServletResponse.SC_NOT_FOUND, message);
  }

  public static final StatusResponse unmodified() {
    return new StatusResponse(HttpServletResponse.SC_NOT_MODIFIED);
  }

  /**
   * A simple acknowledgement.
   */
  public static final StatusResponse noContent() {
    return new StatusResponse(HttpServletResponse.SC_NO_CONTENT, null);
  }

  public static final StatusResponse deleted() {return noContent();}

  public static final StatusResponse methodNotAllowed() {
    return new StatusResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  public static StringResponse html(String html) {
    return new StringResponse(html, "text/html");
  }

  public static StringResponse xml(String xml) {
    return new StringResponse(xml, "text/xml");
  }
}
