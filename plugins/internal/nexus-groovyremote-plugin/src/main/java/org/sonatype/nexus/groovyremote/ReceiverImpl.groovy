/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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

  /**
   * Wrap command-invoker so we can provide exception details in the server log stream.
   */
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
