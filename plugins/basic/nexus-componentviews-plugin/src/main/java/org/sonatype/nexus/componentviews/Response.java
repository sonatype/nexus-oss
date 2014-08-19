package org.sonatype.nexus.componentviews;

import java.io.InputStream;

/**
 * @since 3.0
 */
public class Response
{
  private final int statusCode;

  private final String statusCodeReason;

  private final InputStream contentStream;

  private final String mimeType;

  private Response(final int statusCode, final String statusCodeReason, final InputStream contentStream,
                   final String mimeType)
  {
    this.statusCode = statusCode;
    this.statusCodeReason = statusCodeReason;
    this.contentStream = contentStream;
    this.mimeType = mimeType;
  }

  public static ResponseBuilder status(int status) {
    return new ResponseBuilder().status(status);
  }

  public static ResponseBuilder content(String mimeType, InputStream contentStream) {
    return new ResponseBuilder().content(mimeType, contentStream);
  }

  public static class ResponseBuilder
  {
    private int status;

    private String statusCodeReason;

    private InputStream contentStream;

    private String mimeType;

    public ResponseBuilder status(int status) {
      this.status = status;
      return this;
    }

    public ResponseBuilder statusCodeReason(String reason) {
      this.statusCodeReason = reason;
      return this;
    }

    public ResponseBuilder content(String mimeType, InputStream contentStream) {
      this.mimeType = mimeType;
      this.contentStream = contentStream;
      return this;
    }

    public Response build() {
      return new Response(status, statusCodeReason, contentStream, mimeType) {};
    }
  }
}
