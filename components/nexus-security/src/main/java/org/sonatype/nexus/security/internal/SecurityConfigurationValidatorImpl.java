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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.security.config.CPrivilege;
import org.sonatype.nexus.security.config.CRole;
import org.sonatype.nexus.security.config.CUser;
import org.sonatype.nexus.security.config.CUserRoleMapping;
import org.sonatype.nexus.security.config.ConfigurationIdGenerator;
import org.sonatype.nexus.security.config.SecurityConfiguration;
import org.sonatype.nexus.security.config.SecurityConfigurationValidationContext;
import org.sonatype.nexus.security.config.SecurityConfigurationValidator;
import org.sonatype.nexus.security.privilege.PrivilegeDescriptor;
import org.sonatype.nexus.validation.ValidationMessage;
import org.sonatype.nexus.validation.ValidationResponse;
import org.sonatype.sisu.goodies.common.ComponentSupport;

@Named
@Singleton
public class SecurityConfigurationValidatorImpl
    extends ComponentSupport
    implements SecurityConfigurationValidator
{
  private static String DEFAULT_SOURCE = "default";

  private final ConfigurationIdGenerator idGenerator;

  private final List<PrivilegeDescriptor> privilegeDescriptors;

  @Inject
  public SecurityConfigurationValidatorImpl(final List<PrivilegeDescriptor> privilegeDescriptors,
                                            final ConfigurationIdGenerator idGenerator)
  {
    this.privilegeDescriptors = privilegeDescriptors;
    this.idGenerator = idGenerator;
  }

  public ValidationResponse validateModel(final SecurityConfiguration model) {
    ValidationResponse response = new ValidationResponse();
    response.setContext(new SecurityConfigurationValidationContext());
    SecurityConfigurationValidationContext context = (SecurityConfigurationValidationContext) response.getContext();

    List<CPrivilege> privs = model.getPrivileges();
    if (privs != null) {
      for (CPrivilege priv : privs) {
        response.append(validatePrivilege(context, priv, false));
      }
    }

    List<CRole> roles = model.getRoles();
    if (roles != null) {
      for (CRole role : roles) {
        response.append(validateRole(context, role, false));
      }
    }

    response.append(validateRoleContainment(context));

    List<CUser> users = model.getUsers();
    if (users != null) {
      for (CUser user : users) {
        Set<String> roleIds = new HashSet<>();
        for (CUserRoleMapping userRoleMapping : model.getUserRoleMappings()) {
          if (userRoleMapping.getUserId() != null && userRoleMapping.getUserId().equals(user.getId())
              && (DEFAULT_SOURCE.equals(userRoleMapping.getSource()))) {
            roleIds.addAll(userRoleMapping.getRoles());
          }
        }

        response.append(validateUser(context, user, roleIds, false));
      }
    }

    List<CUserRoleMapping> userRoleMappings = model.getUserRoleMappings();
    if (userRoleMappings != null) {
      for (CUserRoleMapping userRoleMapping : userRoleMappings) {
        response.append(this.validateUserRoleMapping(context, userRoleMapping, false));
      }
    }

    if (!response.isEmpty()) {
      log.error("* * * * * * * * * * * * * * * * * * * * * * * * * *");
      log.error("Security configuration has validation errors/warnings");
      log.error("* * * * * * * * * * * * * * * * * * * * * * * * * *");
      log.error(response.toString());
      log.error("* * * * * * * * * * * * * * * * * * * * *");
    }
    else {
      log.info("Security configuration validated successfully");
    }

    return response;
  }

  public ValidationResponse validatePrivilege(SecurityConfigurationValidationContext ctx, CPrivilege privilege, boolean update) {
    ValidationResponse response = new ValidationResponse();
    if (ctx != null) {
      response.setContext(ctx);
    }

    for (PrivilegeDescriptor descriptor : privilegeDescriptors) {
      ValidationResponse resp = descriptor.validatePrivilege(privilege, ctx, update);

      if (resp != null) {
        response.append(resp);
      }
    }

    ctx.getExistingPrivilegeIds().add(privilege.getId());

    return response;
  }

  public ValidationResponse validateRoleContainment(SecurityConfigurationValidationContext ctx) {
    ValidationResponse response = new ValidationResponse();
    if (ctx != null) {
      response.setContext(ctx);
    }
    SecurityConfigurationValidationContext context = (SecurityConfigurationValidationContext) response.getContext();

    if (context.getExistingRoleIds() != null) {
      for (String roleId : context.getExistingRoleIds()) {
        response.append(isRecursive(roleId, roleId, ctx));
      }
    }

    return response;
  }

  private boolean isRoleNameAlreadyInUse(Map<String, String> existingRoleNameMap, CRole role) {
    for (String roleId : existingRoleNameMap.keySet()) {
      if (roleId.equals(role.getId())) {
        continue;
      }
      if (existingRoleNameMap.get(roleId).equals(role.getName())) {
        return true;
      }
    }
    return false;
  }

  private String getRoleTextForDisplay(String roleId, SecurityConfigurationValidationContext ctx) {
    String name = ctx.getExistingRoleNameMap().get(roleId);
    if (Strings2.isEmpty(name)) {
      return roleId;
    }
    return name;
  }

  private ValidationResponse isRecursive(String baseRoleId, String roleId, SecurityConfigurationValidationContext ctx) {
    ValidationResponse response = new ValidationResponse();

    List<String> containedRoles = ctx.getRoleContainmentMap().get(roleId);

    for (String containedRoleId : containedRoles) {
      // Only need to do this on the first level
      if (baseRoleId.equals(roleId)) {
        if (!ctx.getExistingRoleIds().contains(roleId)) {
          ValidationMessage message =
              new ValidationMessage("roles", "Role '" + getRoleTextForDisplay(baseRoleId, ctx) + "' contains an invalid role");

          response.addWarning(message);
        }
      }

      if (containedRoleId.equals(baseRoleId)) {
        ValidationMessage message =
            new ValidationMessage("roles", "Role '" + getRoleTextForDisplay(baseRoleId, ctx)
                + "' contains itself through Role '" + getRoleTextForDisplay(roleId, ctx)
                + "'.  This is not valid.");

        response.addError(message);

        break;
      }

      if (ctx.getExistingRoleIds().contains(containedRoleId)) {
        response.append(isRecursive(baseRoleId, containedRoleId, ctx));
      }
      // Only need to do this on the first level
      else if (baseRoleId.equals(roleId)) {
        ValidationMessage message =
            new ValidationMessage("roles", "Role '" + getRoleTextForDisplay(roleId, ctx)
                + "' contains an invalid role '" + getRoleTextForDisplay(containedRoleId, ctx) + "'.");

        response.addWarning(message);
      }
    }

    return response;
  }

  public ValidationResponse validateRole(SecurityConfigurationValidationContext ctx, CRole role, boolean update) {
    ValidationResponse response = new ValidationResponse();
    if (ctx != null) {
      response.setContext(ctx);
    }
    SecurityConfigurationValidationContext context = (SecurityConfigurationValidationContext) response.getContext();

    List<String> existingIds = context.getExistingRoleIds();

    if (existingIds == null) {
      context.addExistingRoleIds();
      existingIds = context.getExistingRoleIds();
    }

    if (!update && existingIds.contains(role.getId())) {
      ValidationMessage message = new ValidationMessage("id", "Role ID must be unique.");
      response.addError(message);
    }

    if (update && !existingIds.contains(role.getId())) {
      ValidationMessage message = new ValidationMessage("id", "Role ID cannot be changed.");
      response.addError(message);
    }

    if (!update && (Strings2.isEmpty(role.getId()) || "0".equals(role.getId()))) {
      String newId = idGenerator.generateId();
      response.addWarning("Fixed wrong role ID from '" + role.getId() + "' to '" + newId + "'");
      role.setId(newId);
      response.setModified(true);
    }

    Map<String, String> existingRoleNameMap = context.getExistingRoleNameMap();

    if (Strings2.isEmpty(role.getName())) {
      ValidationMessage message = new ValidationMessage("name", "Role ID '" + role.getId() + "' requires a name.");
      response.addError(message);
    }
    else if (isRoleNameAlreadyInUse(existingRoleNameMap, role)) {
      ValidationMessage message = new ValidationMessage("name", "Role ID '" + role.getId() + "' can't use the name '" + role.getName() + "'.");
      response.addError(message);
    }
    else {
      existingRoleNameMap.put(role.getId(), role.getName());
    }

    if (context.getExistingPrivilegeIds() != null) {
      for (String privId : role.getPrivileges()) {
        if (!context.getExistingPrivilegeIds().contains(privId)) {
          ValidationMessage message = new ValidationMessage("privileges", "Role ID '" + role.getId() + "' Invalid privilege id '" + privId + "' found.");
          response.addWarning(message);
        }
      }
    }

    List<String> containedRoles = context.getRoleContainmentMap().get(role.getId());

    if (containedRoles == null) {
      containedRoles = new ArrayList<>();
      context.getRoleContainmentMap().put(role.getId(), containedRoles);
    }

    for (String roleId : role.getRoles()) {
      if (roleId.equals(role.getId())) {
        ValidationMessage message = new ValidationMessage("roles", "Role ID '" + role.getId() + "' cannot contain itself.");
        response.addError(message);
      }
      else if (context.getRoleContainmentMap() != null) {
        containedRoles.add(roleId);
      }
    }

    // It is expected that a full context is built upon update
    if (update) {
      response.append(isRecursive(role.getId(), role.getId(), context));
    }

    existingIds.add(role.getId());

    return response;
  }

  public ValidationResponse validateUser(SecurityConfigurationValidationContext ctx, CUser user, Set<String> roles, boolean update) {
    ValidationResponse response = new ValidationResponse();
    if (ctx != null) {
      response.setContext(ctx);
    }
    SecurityConfigurationValidationContext context = (SecurityConfigurationValidationContext) response.getContext();

    List<String> existingIds = context.getExistingUserIds();

    if (existingIds == null) {
      context.addExistingUserIds();

      existingIds = context.getExistingUserIds();
    }

    if (!update && Strings2.isEmpty(user.getId())) {
      ValidationMessage message = new ValidationMessage("userId", "User ID is required.");

      response.addError(message);
    }

    if (!update && Strings2.isNotEmpty(user.getId()) && existingIds.contains(user.getId())) {
      ValidationMessage message = new ValidationMessage("userId", "User ID '" + user.getId() + "' is already in use.");
      response.addError(message);
    }

    if (Strings2.isNotEmpty(user.getId()) && user.getId().contains(" ")) {
      ValidationMessage message = new ValidationMessage("userId", "User ID '" + user.getId() + "' cannot contain spaces.");
      response.addError(message);
    }

    if (Strings2.isNotEmpty(user.getFirstName())) {
      user.setFirstName(user.getFirstName());
    }

    if (Strings2.isNotEmpty(user.getLastName())) {
      user.setLastName(user.getLastName());
    }

    if (Strings2.isEmpty(user.getPassword())) {
      ValidationMessage message = new ValidationMessage("password", "User ID '" + user.getId() + "' has no password.  This is a required field.");
      response.addError(message);
    }

    if (Strings2.isEmpty(user.getEmail())) {
      ValidationMessage message = new ValidationMessage("email", "User ID '" + user.getId() + "' has no email address");
      response.addError(message);
    }
    else {
      try {
        if (!user.getEmail().matches(".+@.+")) {
          ValidationMessage message = new ValidationMessage("email", "User ID '" + user.getId() + "' has an invalid email address.");
          response.addError(message);
        }
      }
      catch (PatternSyntaxException e) {
        throw new IllegalStateException("Regex did not compile: " + e.getMessage(), e);
      }
    }

    if (!CUser.STATUS_ACTIVE.equals(user.getStatus()) && !CUser.STATUS_DISABLED.equals(user.getStatus())) {
      ValidationMessage message = new ValidationMessage("status", "User ID '" + user.getId() + "' has invalid status '" + user.getStatus() + "'.  (Allowed values are: " + CUser.STATUS_ACTIVE + " and " + CUser.STATUS_DISABLED + ")");
      response.addError(message);
    }

    if (context.getExistingRoleIds() != null) {

      if (roles != null && roles.size() > 0) {
        for (String roleId : roles) {
          if (!context.getExistingRoleIds().contains(roleId)) {
            ValidationMessage message = new ValidationMessage("roles", "User ID '" + user.getId() + "' Invalid role id '" + roleId + "' found.");
            response.addError(message);
          }
        }
      }
    }

    if (!Strings2.isEmpty(user.getId())) {
      existingIds.add(user.getId());
    }

    return response;
  }

  public ValidationResponse validateUserRoleMapping(SecurityConfigurationValidationContext context,
                                                    CUserRoleMapping userRoleMapping,
                                                    boolean update)
  {
    ValidationResponse response = new ValidationResponse();

    // ID must be not empty
    if (Strings2.isEmpty(userRoleMapping.getUserId())) {
      ValidationMessage message = new ValidationMessage("userId", "UserRoleMapping has no userId.  This is a required field.");
      response.addError(message);
    }

    // source must be not empty
    if (Strings2.isEmpty(userRoleMapping.getSource())) {
      ValidationMessage message = new ValidationMessage("source", "User Role Mapping for user '" + userRoleMapping.getUserId() + "' has no source.  This is a required field.");
      response.addError(message);
    }

    Set<String> roles = userRoleMapping.getRoles();
    // all roles must be real
    if (context.getExistingRoleIds() != null) {

      if (roles != null && roles.size() > 0) {
        for (String roleId : roles) {
          if (!context.getExistingRoleIds().contains(roleId)) {
            ValidationMessage message = new ValidationMessage("roles", "User Role Mapping for user '" + userRoleMapping.getUserId() + "' Invalid role id '" + roleId + "' found.");
            response.addError(message);
          }
        }
      }
    }

    return response;
  }
}
