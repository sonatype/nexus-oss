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
package org.sonatype.security.realms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegePermissionPropertyDescriptor;
import org.sonatype.security.usermanagement.MockUserManager;
import org.sonatype.security.usermanagement.UserManager;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import junit.framework.Assert;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

public class ExternalRoleMappedTest
    extends AbstractSecurityTestCase
{
  @Override
  public void configure(final Binder binder) {
    super.configure(binder);

    binder.bind(UserManager.class)
        .annotatedWith(Names.named("Mock"))
        .to(MockUserManager.class)
        .in(Singleton.class);

    binder.bind(Realm.class)
        .annotatedWith(Names.named("Mock"))
        .to(MockRealm.class)
        .in(Singleton.class);
  }

  public void testUserHasPermissionFromExternalRole() throws Exception {
    SecuritySystem securitySystem = this.lookup(SecuritySystem.class);

    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ApplicationPrivilegeMethodPropertyDescriptor.ID, "read");
    properties.put(ApplicationPrivilegePermissionPropertyDescriptor.ID, "permissionOne");

    securitySystem.getAuthorizationManager("default").addPrivilege(new Privilege(
        "randomId",
        "permissionOne",
        "permissionOne",
        ApplicationPrivilegeDescriptor.TYPE,
        properties, false));

    securitySystem.getAuthorizationManager("default").addRole(new Role("mockrole1", "mockrole1", "mockrole1",
        "default", false, null,
        Collections.singleton("randomId")));

    // add MockRealm to config
    List<String> realms = new ArrayList<String>();
    realms.add("Mock");
    realms.add(AuthorizingRealmImpl.ROLE);
    securitySystem.setRealms(realms);

    // jcohen has the role mockrole1, there is also test role with the same ID, which means jcohen automaticly has
    // this test role

    PrincipalCollection jcohen = new SimplePrincipalCollection("jcohen", MockRealm.NAME);

    try {
      securitySystem.checkPermission(jcohen, "permissionOne:invalid");
      Assert.fail("Expected AuthorizationException");
    }
    catch (AuthorizationException e) {
      // expected
    }

    securitySystem.checkPermission(jcohen, "permissionOne:read"); // throws on error, so this is all we need to do
  }
}
