package org.sonatype.nexus.componentviews;

/**
 * A piece of code that processes a {@link ViewRequest} and returns an appropriate {@link ViewResponse}.
 *
 * @since 3.0
 */
public interface Handler
{
  ViewResponse handle(ViewRequest req);
}
