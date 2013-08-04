/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.appcontext.internal;

import java.util.concurrent.CopyOnWriteArrayList;

import org.sonatype.appcontext.lifecycle.AppContextLifecycleManager;
import org.sonatype.appcontext.lifecycle.LifecycleHandler;

public class AppContextLifecycleManagerImpl
    implements AppContextLifecycleManager
{
  private final CopyOnWriteArrayList<LifecycleHandler> handlers;

  public AppContextLifecycleManagerImpl() {
    this.handlers = new CopyOnWriteArrayList<LifecycleHandler>();
  }

  public void registerManaged(final LifecycleHandler handler) {
    handlers.add(handler);
  }

  public void unregisterManaged(final LifecycleHandler handler) {
    handlers.remove(handler);
  }

  public void invokeHandler(final Class<? extends LifecycleHandler> clazz) {
    for (LifecycleHandler handler : handlers) {
      if (clazz.isAssignableFrom(handler.getClass())) {
        try {
          handler.handle();
        }
        catch (Exception e) {
          // nop
        }
      }
    }
  }
}
