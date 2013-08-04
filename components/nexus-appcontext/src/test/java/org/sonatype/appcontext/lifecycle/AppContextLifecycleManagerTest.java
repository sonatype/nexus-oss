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

package org.sonatype.appcontext.lifecycle;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;

import junit.framework.Assert;
import junit.framework.TestCase;

public class AppContextLifecycleManagerTest
    extends TestCase
{
  private int stoppedInvoked = 0;

  private int lifecycleHandlerInvoked = 0;

  public void testSimple() {
    final AppContextRequest request = Factory.getDefaultRequest("c01");
    final AppContext context = Factory.create(request);

    context.getLifecycleManager().registerManaged(new Stoppable()
    {
      public void handle() {
        stoppedInvoked++;
      }
    });
    context.getLifecycleManager().registerManaged(new LifecycleHandler()
    {
      public void handle() {
        lifecycleHandlerInvoked++;
      }
    });

    // call Stopped 1st
    context.getLifecycleManager().invokeHandler(Stoppable.class);
    // call LifecycleHandler (effectively calling all)
    context.getLifecycleManager().invokeHandler(LifecycleHandler.class);

    Assert.assertEquals(2, stoppedInvoked);
    Assert.assertEquals(1, lifecycleHandlerInvoked);
  }
}
