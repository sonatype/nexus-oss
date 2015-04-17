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
package org.sonatype.nexus.security.privilege;

import java.util.List;

import javax.inject.Inject;

import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.security.authz.WildcardPermission2;
import org.sonatype.nexus.security.config.CPrivilege;
import org.sonatype.nexus.security.config.ConfigurationIdGenerator;
import org.sonatype.nexus.security.config.SecurityConfigurationValidationContext;
import org.sonatype.nexus.validation.ValidationMessage;
import org.sonatype.nexus.validation.ValidationResponse;

import org.apache.shiro.authz.Permission;

/**
 * Abstract {@link PrivilegeDescriptor}.
 *
 * @deprecated Use {@link PrivilegeDescriptorSupport} instead.
 */
@Deprecated
public abstract class AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
  private ConfigurationIdGenerator idGenerator;

  @Inject
  public void installDependencies(final ConfigurationIdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  protected abstract String buildPermission(CPrivilege privilege);

  @Override
  public Permission createPermission(final CPrivilege privilege) {
    assert privilege != null;
    assert getType().equals(privilege.getType());
    return new WildcardPermission2(buildPermission(privilege));
  }

  public ValidationResponse validatePrivilege(CPrivilege privilege, SecurityConfigurationValidationContext ctx, boolean update) {
    ValidationResponse response = new ValidationResponse();

    if (ctx != null) {
      response.setContext(ctx);
    }

    SecurityConfigurationValidationContext context = (SecurityConfigurationValidationContext) response.getContext();
    List<String> existingIds = context.getExistingPrivilegeIds();

    if (existingIds == null) {
      context.addExistingPrivilegeIds();
      existingIds = context.getExistingPrivilegeIds();
    }

    if (!update && (Strings2.isEmpty(privilege.getId()) || "0".equals(privilege.getId()) || (existingIds.contains(privilege.getId())))) {
      String newId = idGenerator.generateId();

      ValidationMessage message = new ValidationMessage("id", "Fixed wrong privilege ID from '" + privilege.getId() + "' to '" + newId + "'");
      response.addWarning(message);
      privilege.setId(newId);
      response.setModified(true);
    }

    if (Strings2.isEmpty(privilege.getType())) {
      ValidationMessage message = new ValidationMessage("type", "Cannot have an empty type");

      response.addError(message);
    }

    if (Strings2.isEmpty(privilege.getName())) {
      ValidationMessage message = new ValidationMessage("name", "Privilege ID '" + privilege.getId() + "' requires a name.");
      response.addError(message);
    }

    return response;
  }
}
