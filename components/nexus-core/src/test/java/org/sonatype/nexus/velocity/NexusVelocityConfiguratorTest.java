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

package org.sonatype.nexus.velocity;

import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.sisu.velocity.Velocity;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test ensuring production config on Velocity is set. The dev setting is not checked
 *
 * @author cstamas
 */
public class NexusVelocityConfiguratorTest
    extends NexusTestSupport
{
  @Test
  public void checkForProduction()
      throws Exception
  {
    final Velocity velocity = lookup(Velocity.class);

    assertThat((String) velocity.getEngine().getProperty("class.resource.loader.cache"), equalTo("true"));
    assertThat((String) velocity.getEngine().getProperty("class.resource.loader.modificationCheckInterval"),
        equalTo("0"));
    assertThat((String) velocity.getEngine().getProperty("runtime.references.strict"), equalTo("false"));
  }
}
