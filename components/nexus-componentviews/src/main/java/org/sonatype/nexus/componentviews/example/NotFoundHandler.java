package org.sonatype.nexus.componentviews.example;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.Request;
import org.sonatype.nexus.componentviews.Response;

/**
 * Always returns a 404 'not found' response.
 *
 * @since 3.0
 */
@Named
@Singleton
public class NotFoundHandler
    implements Handler
{
  @Override
  public Response handle(final Request req) {
    return Response.status(404).statusCodeReason("Not Found").build();
  }
}
