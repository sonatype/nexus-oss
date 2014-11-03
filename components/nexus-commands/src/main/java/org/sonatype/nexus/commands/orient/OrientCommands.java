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
package org.sonatype.nexus.commands.orient;

import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.orientechnologies.common.console.annotation.ConsoleCommand;
import com.orientechnologies.orient.console.OConsoleDatabaseApp;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Function;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.CommandException;
import org.apache.karaf.shell.commands.basic.AbstractCommand;
import org.apache.karaf.shell.console.SubShellAction;
import org.eclipse.sisu.EagerSingleton;
import org.osgi.framework.BundleContext;

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
  private static final String NL = System.getProperty("line.separator");

  private static final String SCOPE = "orient";

  @Inject
  public OrientCommands(final BundleContext ctx) {
    super(new String[0]);

    // define our own Karaf sub-shell: type 'orient' to enter, and 'exit' to leave
    ctx.registerService(Function.class, createShellCommand(), config("*", SCOPE));

    for (Method method : getConsoleMethods().keySet()) {
      if (method.isAnnotationPresent(ConsoleCommand.class)) {
        ctx.registerService(Function.class, createOrientCommand(method), config(SCOPE, method.getName()));
      }
    }

    onBefore(); // set OrientDB command defaults
  }

  @Override
  protected void printApplicationInfo() {
    // hide banner as it doesn't apply here
  }

  private AbstractCommand createOrientCommand(final Method method) {
    return new AbstractCommand()
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

  private static Dictionary<String, ?> config(final String scope, final String function) {
    Hashtable<String, String> props = new Hashtable<String, String>();
    props.put(CommandProcessor.COMMAND_SCOPE, scope);
    props.put(CommandProcessor.COMMAND_FUNCTION, function);
    return props;
  }

  public Object execute(final Method method, final String command, final List<Object> params) {
    if (params.isEmpty() || !"--help".equals(params.get(0))) {

      // rebuild expression so OrientDB can re-parse it to handle optional params, etc.
      List<?> expression = ImmutableList.builder().add(command).addAll(params).build();
      OrientCommands.this.execute(Joiner.on(' ').useForNull("null").join(expression));

      return NL;
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
