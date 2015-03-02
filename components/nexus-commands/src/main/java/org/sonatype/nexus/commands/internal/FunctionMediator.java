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
import java.util.Map;

import javax.inject.Named;

import org.sonatype.nexus.commands.CommandHelper;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;
import org.apache.felix.service.command.Function;
import org.apache.karaf.shell.commands.Command;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.Mediator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

// TODO: Probably only need to support Action, but for now for testing registration of Functions too

/**
 * Manages registration of Karaf {@link Function} instances.
 *
 * @since 3.0
 */
@Named
public class FunctionMediator
  extends ComponentSupport
  implements Mediator<Named, Function, BundleContext>
{
  private final Map<BeanEntry,ServiceRegistration<Function>> registrations = Maps.newConcurrentMap();

  @Override
  public void add(final BeanEntry<Named, Function> beanEntry, final BundleContext bundleContext) throws Exception {
    Command command = beanEntry.getImplementationClass().getAnnotation(Command.class);
    if (command != null) {
      Dictionary<String,?> config = CommandHelper.config(command);
      // TODO info -> debug
      log.info("Adding function: {}, config: {}", beanEntry, config);
      ServiceRegistration<Function> registration = bundleContext.registerService(
          Function.class,
          beanEntry.getValue(),
          config
      );

      registrations.put(beanEntry, registration);
    }
    else {
      log.warn("Missing @Command annotation on function: {}", beanEntry);
    }
  }

  // TODO: Use scope:name as key

  @Override
  public void remove(final BeanEntry<Named, Function> beanEntry, final BundleContext bundleContext) throws Exception {
    // FIXME: No idea if this is sane or proper
    ServiceRegistration<Function> registration = registrations.remove(beanEntry);
    if (registration != null) {
      // TODO info -> debug
      log.info("Removing function: {}", beanEntry);
      registration.unregister();
    }
    else {
      log.warn("Missing registration: {}", beanEntry);
    }
  }
}
