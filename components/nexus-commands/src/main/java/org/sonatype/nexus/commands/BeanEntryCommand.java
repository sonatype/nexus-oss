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

import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.CommandWithAction;
import org.eclipse.sisu.BeanEntry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts Sisu {@link BeanEntry} (carrying {@link Action}) to a Karaf {@link CommandWithAction}.
 *
 * @since 3.0
 */
public class BeanEntryCommand
    extends AbstractCompletableCommand
{
  private final BeanEntry<?, Action> beanEntry;

  public BeanEntryCommand(final BeanEntry<?, Action> beanEntry) {
    this.beanEntry = checkNotNull(beanEntry);
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
}
