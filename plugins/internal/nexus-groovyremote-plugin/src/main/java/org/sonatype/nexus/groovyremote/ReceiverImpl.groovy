package org.sonatype.nexus.groovyremote

import groovyx.remote.Command
import groovyx.remote.CommandChain
import groovyx.remote.Result
import groovyx.remote.server.CommandChainInvoker
import groovyx.remote.server.CommandInvoker
import groovyx.remote.server.Receiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonatype.sisu.goodies.common.Throwables2

/**
 * Custom {@link Receiver} to customize groovy-remote execution behavior.
 *
 * @since 3.0
 */
class ReceiverImpl
    extends Receiver
{
  ReceiverImpl(final ClassLoader classLoader, final Map contextStorageSeed) {
    super(classLoader, contextStorageSeed)
  }

  @Override
  protected Object createInvoker(final ClassLoader classLoader, final CommandChain commandChain) {
    return new CommandChainInvoker(classLoader, commandChain) {
      @Override
      protected Object createInvoker(final ClassLoader loader, final Command command) {
        return new CommandInvoker(loader, command) {
          private final Logger log = LoggerFactory.getLogger(ReceiverImpl.class)

          @Override
          protected Result resultForThrown(final Throwable thrown) {
            if (log.debugEnabled) {
              log.warn("Request failed", thrown)
            }
            else {
              log.warn("Request failed: {}", Throwables2.explain(thrown))
            }

            return super.resultForThrown(thrown)
          }
        };
      }
    };
  }
}
