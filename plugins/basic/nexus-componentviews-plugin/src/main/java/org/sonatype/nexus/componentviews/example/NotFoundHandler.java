package org.sonatype.nexus.componentviews.example;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewResponse;
import org.sonatype.nexus.componentviews.responses.Responses;

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
  public ViewResponse handle(final ViewRequest req) {
    return Responses.notFound(null);
  }
}
