/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.commands.internal;

import java.util.Dictionary;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.commands.BeanEntryCommand;
import org.sonatype.nexus.commands.CommandHelper;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.felix.service.command.Function;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;
import org.osgi.framework.BundleContext;

/**
 * Manages registration of Karaf {@link Action} instances.
 *
 * @since 3.0
 */
@Named
public class ActionMediator
    extends ComponentSupport
    implements Mediator<Named, Action, BundleContext>
{
  /**
   * Returns command annotation for given bean-entry if one exists.
   */
  @Nullable
  private Command command(final BeanEntry<?,Action> beanEntry) {
    return beanEntry.getImplementationClass().getAnnotation(Command.class);
  }

  @Override
  public void add(final BeanEntry<Named, Action> beanEntry, final BundleContext bundleContext) throws Exception {
    Command command = command(beanEntry);
    if (command != null) {
      // TODO: warn if @Singleton is present, this is probably not desired due to @Option/@Argument processing ?

      Dictionary<String, ?> config = CommandHelper.config(command);
      log.debug("Adding action: {}, config: {}", beanEntry, config);
      bundleContext.registerService(
          Function.class,
          new BeanEntryCommand(beanEntry),
          config
      );
    }
    else {
      log.warn("Missing @Command annotation on action: {}", beanEntry);
    }
  }

  @Override
  public void remove(final BeanEntry<Named, Action> beanEntry, final BundleContext bundleContext) throws Exception {
    // TODO: implement remove
  }
}
