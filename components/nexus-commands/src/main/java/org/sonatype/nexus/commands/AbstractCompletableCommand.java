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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.karaf.shell.commands.basic.AbstractCommand;
import org.apache.karaf.shell.console.CompletableFunction;
import org.apache.karaf.shell.console.Completer;

/**
 * Helper class that ties {@link AbstractCommand} and {@link CompletableFunction} together.
 * 
 * @since 3.0
 */
public abstract class AbstractCompletableCommand
    extends AbstractCommand
    implements CompletableFunction
{
  // NOTE: Both return values appear to be nullable, but not marked on interface

  @Override
  @Nullable
  public List<Completer> getCompleters() {
    return null;
  }

  @Override
  @Nullable
  public Map<String, Completer> getOptionalCompleters() {
    return null;
  }
}
