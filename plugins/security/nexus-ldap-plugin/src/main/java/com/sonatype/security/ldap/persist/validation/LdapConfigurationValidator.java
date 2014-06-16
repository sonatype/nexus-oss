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
package com.sonatype.security.ldap.persist.validation;

import java.util.List;

import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import com.sonatype.security.ldap.realms.persist.model.CLdapConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.configuration.validation.ValidationContext;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;

public interface LdapConfigurationValidator
{
  ValidationResponse validateLdapServerConfiguration(CLdapServerConfiguration ldapServerConfiguration, boolean update);

  ValidationResponse validateConnectionInfo(ValidationContext ctx, CConnectionInfo connectionInfo);

  ValidationResponse validateModel(ValidationRequest<CLdapConfiguration> validationRequest);

  ValidationResponse validateLdapServerOrder(List<CLdapServerConfiguration> ldapServers, List<String> proposedOrder);
}
