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

package org.sonatype.nexus.commands;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.CommandWithAction;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.Completer;
import org.eclipse.sisu.BeanEntry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Adapts Sisu {@link BeanEntry} (carrying {@link Action}) to a Karaf {@link CommandWithAction}.
 *
 * @since 3.0
 */
public class BeanEntryCommand
    extends AbstractCompletableCommand
{
  private final BeanEntry<?, Action> beanEntry;

  private List<Completer> argumentCompleters = Lists.newLinkedList();

  private Map<String,Completer> optionCompleters = Maps.newHashMap();

  public BeanEntryCommand(final BeanEntry<?, Action> beanEntry) {
    this.beanEntry = checkNotNull(beanEntry);

    discoverCompleters(beanEntry.getImplementationClass());
  }

  private void discoverCompleters(final Class<Action> type) {
    for (Field field : type.getDeclaredFields()) {
      Complete complete = field.getAnnotation(Complete.class);
      if (complete != null) {
        Argument argument = field.getAnnotation(Argument.class);
        Option option = field.getAnnotation(Option.class);
        checkState(argument != null || option != null);
        if (argument != null) {
          argumentCompleters.set(argument.index(), completer(complete));
        }
        else if (option != null) {
          optionCompleters.put(option.name(), completer(complete));
        }
      }
    }
  }

  private Completer completer(final Complete complete) {
    // TODO: Need bean-locator/container context here to resolve complete.value()
    return null;
  }

  @Override
  public Class<? extends org.apache.felix.gogo.commands.Action> getActionClass() {
    return beanEntry.getImplementationClass();
  }

  @Override
  public Action createNewAction() {
    return beanEntry.getProvider().get();
  }

  @Override
  public String toString() {
    return beanEntry.toString();
  }

  //
  // CompletableFunction
  //


  @Nullable
  @Override
  public List<Completer> getCompleters() {
    return argumentCompleters;
  }

  @Nullable
  @Override
  public Map<String, Completer> getOptionalCompleters() {
    return optionCompleters;
  }
}
