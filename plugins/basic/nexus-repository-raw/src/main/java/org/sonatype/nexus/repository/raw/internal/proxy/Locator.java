package org.sonatype.nexus.repository.raw.internal.proxy;

/**
 * @since 3.0
 */
public interface Locator
{
  /**
   * A description suitable for using as a 404 message.
   * Is this a different thing than {@link #uri()} ?
   */
  String describe();

  /**
   * Return the relative bit of the URI.
   * Is this a different thing than {@link #describe()} ?
   */
  String uri();
}
