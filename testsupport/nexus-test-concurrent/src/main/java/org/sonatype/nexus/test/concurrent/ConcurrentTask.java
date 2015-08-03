package org.sonatype.nexus.test.concurrent;

/**
 * A task of some sort to be executed concurrently with other tasks. Tasks must be stateless, thread-safe and
 * reusable.
 */
public interface ConcurrentTask
{
  /**
   * @throws Exception If the entire test should fail (e.g. in the case of a concurrency problem).
   */
  void run() throws Exception;
}
