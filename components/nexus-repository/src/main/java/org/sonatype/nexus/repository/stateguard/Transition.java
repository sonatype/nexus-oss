package org.sonatype.nexus.repository.stateguard;

/**
 * ???
 *
 * @since 3.0
 */
public interface Transition
{
  Transition from(String... allowed);

  void run(Action action) throws Exception;
}
