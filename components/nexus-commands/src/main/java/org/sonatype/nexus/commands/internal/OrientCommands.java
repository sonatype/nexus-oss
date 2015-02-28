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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.commands.AbstractCompletableCommand;
import org.sonatype.nexus.commands.CommandHelper;
import org.sonatype.nexus.commands.CommandManager;
import org.sonatype.nexus.common.text.Strings2;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.orientechnologies.common.console.annotation.ConsoleCommand;
import com.orientechnologies.orient.console.OConsoleDatabaseApp;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.CommandException;
import org.apache.karaf.shell.commands.basic.AbstractCommand;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.SubShellAction;
import org.apache.karaf.shell.console.completer.NullCompleter;
import org.eclipse.sisu.EagerSingleton;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

/**
 * Registers OrientDB commands with Karaf's shell; handles execution requests and displays results.
 * 
 * @since 3.0
 */
@Named
@EagerSingleton
public class OrientCommands
    extends OConsoleDatabaseApp
{
  private static final String SCOPE = "orient";

  @Inject
  public OrientCommands(final CommandManager commandManager) {
    super(new String[0]);

    // define our own Karaf sub-shell: type 'orient' to enter, and 'exit' to leave
    commandManager.register(createShellCommand(), CommandHelper.config("*", SCOPE));

    for (Method method : getConsoleMethods().keySet()) {
      if (method.isAnnotationPresent(ConsoleCommand.class)) {
        commandManager.register(createOrientCommand(method), CommandHelper.config(SCOPE, method.getName()));
      }
    }

    onBefore(); // set OrientDB command defaults
  }

  @Override
  protected void printApplicationInfo() {
    // hide banner as it doesn't apply here
  }

  private AbstractCommand createOrientCommand(final Method method) {
    return new AbstractCompletableCommand()
    {
      // OrientDB expects the method name to be transformed from camel-case into lower-case with spaces
      private final String command = LOWER_CAMEL.to(LOWER_UNDERSCORE, method.getName()).replace('_', ' ');

      @Override
      public Object execute(final CommandSession session, final List<Object> params) throws Exception {
        try {
          return OrientCommands.this.execute(method, command, params);
        }
        catch (Throwable e) {
          throw new CommandException(e);
        }
      }

      public List<Completer> getCompleters() {
        return Collections.<Completer> singletonList(NullCompleter.INSTANCE);
      }

      public Map<String, Completer> getOptionalCompleters() {
        return Collections.emptyMap();
      }

      @Override
      public Class<? extends Action> getActionClass() {
        return Action.class;
      }

      @Override
      public Action createNewAction() {
        return null; // not used
      }
    };
  }

  private static AbstractCommand createShellCommand() {
    return new AbstractCommand()
    {
      @Override
      public Action createNewAction() {
        SubShellAction subShell = new SubShellAction();
        subShell.setSubShell(SCOPE);
        return subShell;
      }
    };
  }

  public Object execute(final Method method, final String command, final List<Object> params) {
    if (params.isEmpty() || !"--help".equals(params.get(0))) {

      // rebuild expression so OrientDB can re-parse it to handle optional params, etc.
      List<?> expression = ImmutableList.builder().add(command).addAll(params).build();
      OrientCommands.this.execute(Joiner.on(' ').useForNull("null").join(expression));

      return Strings2.NL;
    }

    syntaxError(command, method);
    return ""; // avoid spurious newline
  }

  @Override
  public void error(final String iMessage, final Object... iArgs) {
    // clean up error messages to remove redundant/irrelevant content
    if (!iMessage.contains("Unrecognized command")) {
      super.error(iMessage.replaceFirst("(?s)!Wrong syntax.*Expected", "Syntax"), iArgs);
    }
  }
}
