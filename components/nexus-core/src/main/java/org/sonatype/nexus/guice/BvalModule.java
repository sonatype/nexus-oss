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
package org.sonatype.nexus.guice;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.BootstrapState;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.bval.guice.Validate;
import org.apache.bval.guice.ValidationModule;

/**
 * <a href="https://bval.apache.org">Apache BVal</a> Guice module.
 *
 * Allows {@link Validate} to be used to apply validation around a method invocation.
 *
 * Additional provides some workarounds for {@link ValidationModule} quirks.
 *
 * @since 3.0
 */
public class BvalModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    // BootstrapState should not be bounded as it is marked as optional in ConfigurationStateProvider
    bind(BootstrapState.class).toInstance(new BootstrapState()
    {
      @Override
      public ValidationProviderResolver getValidationProviderResolver() {
        return null;
      }

      @Override
      public ValidationProviderResolver getDefaultValidationProviderResolver() {
        return null;
      }
    });

    // TCCL workaround for Apache/BVal visibility issue
    binder().bindInterceptor(Matchers.any(), Matchers.annotatedWith(Validate.class), new MethodInterceptor()
    {
      public Object invoke(final MethodInvocation mi) throws Throwable {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
          return mi.proceed();
        }
        finally {
          Thread.currentThread().setContextClassLoader(cl);
        }
      }
    });

    install(new ValidationModule());
  }
}