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
package org.sonatype.nexus.integrationtests;

import org.sonatype.security.rest.model.UserResource;

import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractPrivilegeTest
    extends AbstractSecurityTest
{

  public AbstractPrivilegeTest(String testRepositoryId) {
    super(testRepositoryId);
  }

  public AbstractPrivilegeTest() {
  }

  @BeforeClass
  public static void enableSecurity() {
    // turn on security for the test
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Before
  public void resetTestUserPrivs()
      throws Exception
  {
    TestContainer.getInstance().getTestContext().useAdminForRequests();

    UserResource testUser = this.userUtil.getUser(TEST_USER_NAME);
    testUser.getRoles().clear();
    testUser.addRole("anonymous");
    this.userUtil.updateUser(testUser);
  }

}
