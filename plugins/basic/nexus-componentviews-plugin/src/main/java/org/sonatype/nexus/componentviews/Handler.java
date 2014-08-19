package org.sonatype.nexus.componentviews;

/**
 * @since 3.0
 */
public interface Handler
{
  Response handle(Request req);
}
