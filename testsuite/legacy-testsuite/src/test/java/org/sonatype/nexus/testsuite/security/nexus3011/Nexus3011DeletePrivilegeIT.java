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
package org.sonatype.nexus.testsuite.security.nexus3011;

import org.sonatype.nexus.integrationtests.AbstractSecurityTest;
import org.sonatype.nexus.integrationtests.ITGroups.SECURITY;
import org.sonatype.security.rest.model.RoleResource;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

public class Nexus3011DeletePrivilegeIT
    extends AbstractSecurityTest
{

  private static final String ROLE_ID = "nexus3011-role";

  @Test
  @Category(SECURITY.class)
  public void deletePriv()
      throws Exception
  {
    String readPrivId = createPrivileges("nexus3011-priv", "1", asList("read")).get(0).getId();
    String createPrivId = createPrivileges("nexus3011-priv", "1", asList("create")).get(0).getId();
    String updatePrivId = createPrivileges("nexus3011-priv", "1", asList("update")).get(0).getId();
    String deletePrivId = createPrivileges("nexus3011-priv", "1", asList("delete")).get(0).getId();
    String[] privs = new String[]{readPrivId, createPrivId, updatePrivId, deletePrivId};

    RoleResource role = createRole(ROLE_ID, asList(privs));
    Assert.assertNotNull(role);
    MatcherAssert.assertThat(role.getPrivileges(), hasItems(privs));
    privUtil.assertExists(privs);

    // remove read
    Assert.assertTrue(privUtil.delete(readPrivId).getStatus().isSuccess());
    role = roleUtil.getRole(ROLE_ID);
    MatcherAssert.assertThat(role.getPrivileges(), not(hasItems(readPrivId)));
    MatcherAssert.assertThat(role.getPrivileges(), hasItems(createPrivId, updatePrivId, deletePrivId));

    // remove create
    Assert.assertTrue(privUtil.delete(createPrivId).getStatus().isSuccess());
    role = roleUtil.getRole(ROLE_ID);
    MatcherAssert.assertThat(role.getPrivileges(), not(hasItems(readPrivId, createPrivId)));
    MatcherAssert.assertThat(role.getPrivileges(), hasItems(updatePrivId, deletePrivId));

    // remove update
    Assert.assertTrue(privUtil.delete(updatePrivId).getStatus().isSuccess());
    role = roleUtil.getRole(ROLE_ID);
    MatcherAssert.assertThat(role.getPrivileges(), not(hasItems(readPrivId, createPrivId, updatePrivId)));
    MatcherAssert.assertThat(role.getPrivileges(), hasItems(deletePrivId));

    // remove delete
    Assert.assertTrue(privUtil.delete(deletePrivId).getStatus().isSuccess());
    role = roleUtil.getRole(ROLE_ID);
    MatcherAssert.assertThat(role.getPrivileges(),
        not(hasItems(readPrivId, createPrivId, updatePrivId, deletePrivId)));
    Assert.assertTrue(role.getPrivileges().isEmpty());

    privUtil.assertNotExists(privs);
  }
}
