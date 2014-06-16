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

import java.io.UnsupportedEncodingException;

import org.sonatype.nexus.proxy.maven.routing.Config;
import org.sonatype.nexus.proxy.maven.routing.internal.ConfigImpl;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.security.guice.SecurityModule;

import com.google.common.base.Throwables;
import com.google.common.collect.ObjectArrays;
import com.google.inject.Binder;
import com.google.inject.Module;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.codec.Base64;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;

import static org.apache.shiro.codec.CodecSupport.PREFERRED_ENCODING;

public abstract class AbstractEnterpriseLdapTest
    extends NexusTestSupport
{
  private CacheManager cacheManager;

  @Override
  protected Module[] getTestCustomModules() {
    Module[] modules = super.getTestCustomModules();
    if (modules == null) {
      modules = new Module[0];
    }
    modules = ObjectArrays.concat(modules, new SecurityModule());
    modules = ObjectArrays.concat(modules, new Module()
    {
      @Override
      public void configure(final Binder binder) {
        binder.bind(Config.class).toInstance(new ConfigImpl(enableAutomaticRoutingFeature()));
      }
    });
    return modules;
  }

  protected boolean enableAutomaticRoutingFeature() {
    return false;
  }

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();
    cacheManager = lookup(CacheManager.class);
  }

  @Override
  protected void tearDown()
      throws Exception
  {
    try {
      cacheManager.shutdown();
    }
    finally {
      super.tearDown();
    }
  }

  @Override
  protected void customizeContainerConfiguration(ContainerConfiguration configuration) {
    configuration.setClassPathScanning(PlexusConstants.SCANNING_ON);
  }

  public static String encodeBase64(final String value) {
    try {
      return Base64.encodeToString(value.getBytes(PREFERRED_ENCODING));
    }
    catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

}
