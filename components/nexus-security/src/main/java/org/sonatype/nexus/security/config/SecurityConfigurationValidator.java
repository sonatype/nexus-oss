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
package org.sonatype.nexus.security.config;

import java.util.Set;

import org.sonatype.nexus.validation.ValidationResponse;

public interface SecurityConfigurationValidator
{
  ValidationResponse validateModel(SecurityConfiguration model);

  ValidationResponse validatePrivilege(SecurityConfigurationValidationContext ctx,
                                       CPrivilege privilege,
                                       boolean update);

  ValidationResponse validateRoleContainment(SecurityConfigurationValidationContext ctx);

  ValidationResponse validateRole(SecurityConfigurationValidationContext ctx, CRole role, boolean update);

  ValidationResponse validateUser(SecurityConfigurationValidationContext ctx,
                                  CUser user,
                                  Set<String> roles,
                                  boolean update);

  ValidationResponse validateUserRoleMapping(SecurityConfigurationValidationContext ctx,
                                             CUserRoleMapping userRoleMapping,
                                             boolean update);
}
