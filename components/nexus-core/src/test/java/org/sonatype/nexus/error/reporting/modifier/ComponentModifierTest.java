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

package org.sonatype.nexus.error.reporting.modifier;

import org.sonatype.nexus.error.report.ErrorReportComponent;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link ComponentModifier}
 *
 * @since 2.1
 */
public class ComponentModifierTest
    extends TestSupport
{

  @Mock
  private ErrorReportComponent cmp;

  @Mock
  private IssueSubmissionRequest request;

  private ComponentModifier underTest;

  @Before
  public void init() {
    when(cmp.getComponent()).thenReturn("cmp");

    underTest = new ComponentModifier(cmp);
  }

  @Test
  public void testModify()
      throws Exception
  {
    underTest.modify(request);
    verify(request).setComponent("cmp");
  }
}
