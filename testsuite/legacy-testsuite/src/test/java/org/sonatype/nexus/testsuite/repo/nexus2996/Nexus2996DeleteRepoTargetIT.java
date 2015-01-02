/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.repo.nexus2996;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractSecurityTest;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItem;

public class Nexus2996DeleteRepoTargetIT
    extends AbstractSecurityTest
{

  private static final String TARGET_ID = "1c1fd83a2fd9";

  @Test
  public void deleteRepoTarget()
      throws Exception
  {
    RepositoryTargetResource target = TargetMessageUtil.get(TARGET_ID);
    MatcherAssert.assertThat(target.getPatterns(), hasItem(".*"));

    List<PrivilegeStatusResource> privileges = createPrivileges("nexus2996-priv", TARGET_ID);

    for (PrivilegeStatusResource privilege : privileges) {
      privUtil.assertExists(privilege.getId());
    }

    TargetMessageUtil.delete(TARGET_ID);

    for (PrivilegeStatusResource privilege : privileges) {
      privUtil.assertNotExists(privilege.getId());
    }
  }
}
