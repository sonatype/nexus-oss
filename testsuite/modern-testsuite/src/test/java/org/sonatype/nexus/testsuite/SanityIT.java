/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite;

import javax.inject.Inject;

import org.sonatype.nexus.common.app.SystemState;
import org.sonatype.nexus.common.app.SystemStatus;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Sanity test of the OSS base template.
 * 
 * @since 3.0
 */
public class SanityIT
    extends NexusCoreITSupport
{
  @Inject
  private SystemStatus status;

  @Test
  public void testNexusStarts() {
    assertThat(SystemState.STARTED, is(status.getState()));
  }
}
