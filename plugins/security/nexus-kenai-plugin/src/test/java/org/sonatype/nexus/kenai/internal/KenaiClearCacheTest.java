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
package org.sonatype.nexus.kenai.internal;

import org.sonatype.nexus.NxApplication;
import org.sonatype.nexus.kenai.AbstractKenaiRealmTest;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.realm.RealmConfiguration;
import org.sonatype.nexus.security.realm.RealmManager;

import com.google.common.collect.ImmutableList;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.junit.Test;

public class KenaiClearCacheTest
    extends AbstractKenaiRealmTest
{
  protected SecuritySystem securitySystem;

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    lookup(NxApplication.class).start();

    mockKenai();

    RealmManager realmManager = lookup(RealmManager.class);

    RealmConfiguration realmConfiguration = new RealmConfiguration();
    realmConfiguration.setRealmNames(ImmutableList.of(KenaiRealm.ROLE));
    realmManager.setConfiguration(realmConfiguration);

    securitySystem = lookup(SecuritySystem.class);
  }

  @Override
  protected void tearDown()
      throws Exception
  {
    lookup(NxApplication.class).stop();
    super.tearDown();
  }

  @Test
  public void testClearCache()
      throws Exception
  {
    // so here is the problem, we clear the authz cache when ever config changes happen

    // now log the user in
    Subject subject1 = securitySystem.login(new UsernamePasswordToken(username, password));
    // check authz
    subject1.checkRole(DEFAULT_ROLE);

    // clear the cache
    KenaiRealm realm = (KenaiRealm) this.lookup(Realm.class, "kenai");
    realm.getAuthorizationCache().clear();

    // user should still have the role
    subject1.checkRole(DEFAULT_ROLE);

    // the user should be able to login again as well
    Subject subject2 = securitySystem.login(new UsernamePasswordToken(username, password));
    subject2.checkRole(DEFAULT_ROLE);
  }
}
