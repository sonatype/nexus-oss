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
package org.sonatype.nexus.security.internal;

import java.util.List;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link PrivilegeInheritanceManagerImpl}.
 */
public class PrivilegeInheritanceManagerImplTest
    extends TestSupport
{
  private PrivilegeInheritanceManagerImpl manager;

  @Before
  public void setUp() throws Exception {
    manager = new PrivilegeInheritanceManagerImpl();
  }

  @Test
  public void testCreateInherit() throws Exception {
    List<String> methods = manager.getInheritedMethods("create");

    assertTrue(methods.size() == 2);
    assertTrue(methods.contains("read"));
    assertTrue(methods.contains("create"));
  }

  @Test
  public void testReadInherit() throws Exception {
    List<String> methods = manager.getInheritedMethods("read");

    assertTrue(methods.size() == 1);
    assertTrue(methods.contains("read"));
  }

  @Test
  public void testUpdateInherit() throws Exception {
    List<String> methods = manager.getInheritedMethods("update");

    assertTrue(methods.size() == 2);
    assertTrue(methods.contains("read"));
    assertTrue(methods.contains("update"));
  }

  @Test
  public void testDeleteInherit() throws Exception {
    List<String> methods = manager.getInheritedMethods("delete");

    assertTrue(methods.size() == 2);
    assertTrue(methods.contains("read"));
    assertTrue(methods.contains("delete"));
  }
}
