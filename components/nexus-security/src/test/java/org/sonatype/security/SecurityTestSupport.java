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
package org.sonatype.security;

import org.sonatype.security.configuration.source.PreconfiguredSecurityConfigurationSource;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.guice.SecurityModule;
import org.sonatype.sisu.litmus.testsupport.TestUtil;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import org.eclipse.sisu.launch.InjectedTestCase;

public abstract class SecurityTestSupport
    extends InjectedTestCase
{
  // FIXME: Convert to junit4

  protected final TestUtil util = new TestUtil(this);

  @Override
  public void configure(final Binder binder) {
    binder.install(new SecurityModule());
    binder.bind(SecurityConfigurationSource.class)
        .annotatedWith(Names.named("default"))
        .toInstance(new PreconfiguredSecurityConfigurationSource(SecurityTestSupportSecurity.security()));
  }

}
