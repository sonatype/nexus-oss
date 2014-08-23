package org.sonatype.nexus.componentviews;

import org.sonatype.nexus.componentviews.responses.Response;

/**
 * @since 3.0
 */
public interface Handler
{
  Response handle(Request req);
}
