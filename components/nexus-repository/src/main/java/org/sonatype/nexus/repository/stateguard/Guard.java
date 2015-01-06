package org.sonatype.nexus.repository.stateguard;

import java.util.concurrent.Callable;

/**
 * ???
 *
 * @since 3.0
 */
public interface Guard
{
  void run(Action action) throws Exception;

  void runX(Action action);

  <V> V run(Callable<V> action) throws Exception;

  <V> V runX(Callable<V> action);
}
