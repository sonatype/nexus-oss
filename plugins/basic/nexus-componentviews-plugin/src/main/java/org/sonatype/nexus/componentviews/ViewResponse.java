package org.sonatype.nexus.componentviews;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * The {@link View}'s response to the {@link ViewRequest}.
 *
 * @since 3.0
 */
public interface ViewResponse
{
  void send(HttpServletResponse response) throws IOException;
}
