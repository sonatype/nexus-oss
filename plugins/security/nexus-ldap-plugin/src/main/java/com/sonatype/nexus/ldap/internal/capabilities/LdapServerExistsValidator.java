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
package com.sonatype.nexus.ldap.internal.capabilities;

import java.util.Map;

import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.persist.LdapServerNotFoundException;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.capability.ValidationResult;
import org.sonatype.nexus.capability.Validator;
import org.sonatype.nexus.capability.support.validator.DefaultValidationResult;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates capability configuration field refers to an existing LDAP server.
 *
 * @since 2.4
 */
public class LdapServerExistsValidator
    implements Validator
{

  private final LdapConfigurationManager ldapConfigurationManager;

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("LDAP server exists")
    String valid();

    @DefaultMessage("LDAP server does not exist")
    String invalid();

    @DefaultMessage("LDAP server '%s' does not exist")
    String invalidValue(String ldapServerId);
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final String key;

  public LdapServerExistsValidator(final LdapConfigurationManager ldapConfigurationManager,
                                   final String key)
  {
    this.ldapConfigurationManager = checkNotNull(ldapConfigurationManager);
    this.key = checkNotNull(key);
  }

  @Override
  public ValidationResult validate(final Map<String, String> properties) {
    final String serverId = properties.get(key);
    if (serverId != null) {
      try {
        ldapConfigurationManager.getLdapServerConfiguration(serverId);
      }
      catch (LdapServerNotFoundException e) {
        return new DefaultValidationResult().add(key, messages.invalidValue(serverId));
      }
      catch (InvalidConfigurationException ignore) {
        //ignore as we do not expect an invalid configuration on get
      }
    }
    return ValidationResult.VALID;
  }

  @Override
  public String explainValid() {
    return messages.valid();
  }

  @Override
  public String explainInvalid() {
    return messages.invalid();
  }

}
