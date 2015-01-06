package org.sonatype.nexus.repository.stateguard;

/**
 * ???
 *
 * @since 3.0
 */
public interface Guard
{
  void run(Action action) throws Exception;
}
