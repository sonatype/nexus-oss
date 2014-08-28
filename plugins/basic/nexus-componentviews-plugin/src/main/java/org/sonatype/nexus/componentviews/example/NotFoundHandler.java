package org.sonatype.nexus.componentviews.example;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.Request;
import org.sonatype.nexus.componentviews.responses.Response;
import org.sonatype.nexus.componentviews.responses.Responses;
import org.sonatype.nexus.componentviews.responses.StatusResponse;

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
    return Responses.notFound(null);
  }
}
