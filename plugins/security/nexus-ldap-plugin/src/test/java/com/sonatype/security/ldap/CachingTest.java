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
package com.sonatype.security.ldap;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.ldap.realms.connector.LdapConnector;
import org.sonatype.security.ldap.realms.persist.LdapClearCacheEvent;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("This test is testing LDAP caching in LdapManager, that is removed, as we now use Shiro caching")
public class CachingTest
    extends AbstractMockLdapConnectorTest
{

  private MockLdapConnector mainConnector = null;

  protected List<LdapConnector> getLdapConnectors() {
    List<LdapConnector> connectors = new ArrayList<LdapConnector>();
    this.mainConnector = this.buildMainMockServer("default");
    connectors.add(mainConnector);

    return connectors;
  }

  @Test
  public void testGetUser()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    // all systems are good
    Assert.assertNotNull(ldapManager.getUser("rwalker"));

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the cache
    Assert.assertNotNull(ldapManager.getUser("rwalker"));

    // wait 2 sec cache should still be valid
    Thread.sleep(1000 * 2);
    Assert.assertNotNull(ldapManager.getUser("rwalker"));

    // now wait another 2 seconds and cache should be cleared
    Thread.sleep(1000 * 2);
    // server down and cache cleared
    try {
      ldapManager.getUser("rwalker");
      Assert.fail("expected LdapDAOException");
    }
    catch (LdapDAOException e) {
      // expected
    }
  }

  @Test
  public void testExpireCache()
      throws Exception
  {
    EnterpriseLdapManager ldapManager = (EnterpriseLdapManager) this.lookup(LdapManager.class);

    // all systems are good
    Assert.assertNotNull(ldapManager.getUser("rwalker"));

    // now clear the cache
    ldapManager.onEvent(new LdapClearCacheEvent(null));

    // we also need to reset the connectors
    List<LdapConnector> connectors = ldapManager.getLdapConnectors();
    connectors.clear();
    connectors.addAll(this.getLdapConnectors());
    this.mainConnector.stop();

    try {
      ldapManager.getUser("rwalker");
      Assert.fail("expected LdapDAOException");
    }
    catch (LdapDAOException e) {
      // expected
    }
  }

  @Test
  public void testUserAuthentication()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    // all systems are good
    Assert.assertNotNull(ldapManager.authenticateUser("rwalker", "rwalker123"));

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the cache
    Assert.assertNotNull(ldapManager.authenticateUser("rwalker", "rwalker123"));

    // wait 2 sec cache should still be valid
    Thread.sleep(1000 * 2);
    Assert.assertNotNull(ldapManager.authenticateUser("rwalker", "rwalker123"));

    // now wait another 2 seconds and cache should be cleared
    Thread.sleep(1000 * 2);
    // server down and cache cleared
    try {
      ldapManager.authenticateUser("rwalker", "rwalker123");
      Assert.fail("expected NoSuchLdapUserException");
    }
    catch (AuthenticationException e) {
      // expected
    }
  }

  // public void testCache()
  // throws Exception
  // {
  // CacheManager cacheManager = CacheManager.getInstance();
  //
  // int cacheTimeout = 1;
  //
  // Cache cache = new Cache(
  // "TEST_CACHE",
  // 100,
  // MemoryStoreEvictionPolicy.LRU,
  // false,
  // null,
  // false,
  // cacheTimeout,
  // cacheTimeout,
  // false,
  // 0,
  // null );
  //
  // // add the cache
  // cacheManager.addCache( cache );
  //
  // // add an object to the cache
  // cache.put( new Element( "key1", "value1" ) );
  //
  // // get it out
  // Assert.assertNotNull( cache.get( "key1" ) );
  // Thread.sleep( (cacheTimeout + 1) * 1000 );
  // Element element = cache.get( "key1" );
  // Assert.assertNull( element );
  //
  // element = new Element( "key2", "value2", false, 3, 3 );
  // cache.put( element );
  // // element.setTimeToIdle( 3 );
  // // element.setTimeToLive( 3 );
  //
  // Assert.assertNotNull( cache.get( "key2" ) );
  // Thread.sleep( 2 * 1000 );
  // element = cache.get( "key2" );
  // Assert.assertNotNull( element );
  // Assert.assertTrue( !element.isExpired() );
  //
  // }

}
