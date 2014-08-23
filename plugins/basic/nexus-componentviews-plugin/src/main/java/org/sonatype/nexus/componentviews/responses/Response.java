package org.sonatype.nexus.componentviews.responses;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * @since 3.0
 */
public interface Response
{
  public void send(HttpServletResponse response) throws IOException;
}
