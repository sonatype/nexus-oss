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
package org.sonatype.nexus.extender.modules;

import java.lang.annotation.ElementType;

import javax.inject.Singleton;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

import org.sonatype.nexus.validation.Validate;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.validator.HibernateValidator;

/**
 * Provides validation of methods annotated with {@link Validate}.
 * 
 * @since 3.0
 */
public class ValidationModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    final MethodInterceptor interceptor = new ValidationInterceptor();
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Validate.class), interceptor);
    requestInjection(interceptor);
  }

  @Provides
  @Singleton
  ValidatorFactory validatorFactory() {
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(HibernateValidator.class.getClassLoader());

      return Validation.byDefaultProvider().configure() // start with default configuration
          .traversableResolver(new AlwaysTraversableResolver()) // disable JPA reachability
          .buildValidatorFactory();
    }
    finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
  }

  @Provides
  @Singleton
  Validator validator(final ValidatorFactory validatorFactory) {
    return validatorFactory.getValidator();
  }

  @Provides
  @Singleton
  ExecutableValidator executableValidator(final Validator validator) {
    return validator.forExecutables();
  }

  static class AlwaysTraversableResolver
      implements TraversableResolver
  {
    public boolean isCascadable(final Object traversableObject, final Node traversableProperty,
        final Class<?> rootBeanType, final Path pathToTraversableObject, final ElementType elementType)
    {
      return true;
    }

    public boolean isReachable(final Object traversableObject, final Node traversableProperty,
        final Class<?> rootBeanType, final Path pathToTraversableObject, final ElementType elementType)
    {
      return true;
    }
  }
}
