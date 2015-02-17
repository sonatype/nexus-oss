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
package org.sonatype.nexus.security.internal;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.security.settings.SecuritySettings;
import org.sonatype.nexus.security.settings.SecuritySettingsValidationContext;
import org.sonatype.nexus.security.settings.SecuritySettingsValidator;

@Named
@Singleton
public class SecuritySettingsValidatorImpl
    implements SecuritySettingsValidator
{
  public ValidationResponse validateModel(SecuritySettingsValidationContext context,
                                          ValidationRequest<SecuritySettings> request)
  {
    ValidationResponse validationResponse = new ValidationResponse();
    validationResponse.setContext(context);

    SecuritySettings configuration = request.getConfiguration();

    validationResponse.append(this.validateAnonymousUsername(context, configuration.getAnonymousUsername()));
    validationResponse.append(this.validateAnonymousPassword(context, configuration.getAnonymousPassword()));
    validationResponse.append(this.validateRealms(context, configuration.getRealms()));

    return validationResponse;
  }

  public ValidationResponse validateAnonymousPassword(SecuritySettingsValidationContext context, String anonymousPassword) {
    // we are not currently doing anything here
    ValidationResponse validationResponse = new ValidationResponse();
    validationResponse.setContext(context);
    return validationResponse;
  }

  public ValidationResponse validateAnonymousUsername(SecuritySettingsValidationContext context, String anonymousUsername) {
    // we are not currently doing anything here
    ValidationResponse validationResponse = new ValidationResponse();
    validationResponse.setContext(context);
    return validationResponse;
  }

  public ValidationResponse validateRealms(SecuritySettingsValidationContext context, List<String> realms) {
    ValidationResponse validationResponse = new ValidationResponse();
    validationResponse.setContext(context);

    if (context.getSecuritySettings() == null) {
      if (realms.size() < 1) {
        validationResponse.addValidationError("Security is enabled, You must have at least one realm enabled.");
      }
      // TODO: we should also try to load each one to see if it exists
    }

    return validationResponse;
  }
}
