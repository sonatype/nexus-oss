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
package org.sonatype.nexus.plugins.siesta.internal;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.validation.ValidationMessage;
import org.sonatype.nexus.validation.ValidationResponse;
import org.sonatype.nexus.validation.ValidationResponseException;
import org.sonatype.siesta.ValidationErrorXO;
import org.sonatype.siesta.server.validation.ValidationExceptionMapperSupport;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Maps {@link ValidationResponseException} to 400 with a list of {@link ValidationErrorXO} as body.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ValidationResponseExceptionMapper
    extends ValidationExceptionMapperSupport<ValidationResponseException>
{
  @Override
  protected List<ValidationErrorXO> getValidationErrors(final ValidationResponseException exception) {
    ValidationResponse response = exception.getResponse();
    List<ValidationMessage> errors = response.getErrors();
    if (!errors.isEmpty()) {
      return Lists.transform(errors, new Function<ValidationMessage, ValidationErrorXO>()
      {
        // FIXME: While guava api allows for nulls, the data here should never be null so can simplify
        @Nullable
        @Override
        public ValidationErrorXO apply(@Nullable final ValidationMessage message) {
          if (message != null) {
            return new ValidationErrorXO(message.getKey(), message.getMessage());
          }
          return null;
        }
      });
    }

    return Lists.newArrayList(new ValidationErrorXO(exception.getMessage()));
  }
}
