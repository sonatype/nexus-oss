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
package org.sonatype.security.realms.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.events.AuthorizationConfigurationChanged;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.SecurityModelConfiguration;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;
import org.sonatype.security.realms.validator.SecurityConfigurationValidator;
import org.sonatype.security.realms.validator.SecurityValidationContext;
import org.sonatype.security.usermanagement.UserManagerImpl;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.authc.credential.PasswordService;
import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkState;

@Named
@Singleton
public class DefaultConfigurationManager
    extends ComponentSupport
    implements ConfigurationManager
{
  private final SecurityModelConfigurationSource configurationSource;

  private final SecurityConfigurationValidator validator;

  private final SecurityConfigurationCleaner configCleaner;

  private final List<SecurityConfigurationModifier> configurationModifiers;

  private final PasswordService passwordService;

  private final EventBus eventBus;

  private final List<StaticSecurityResource> staticResources;

  private final List<DynamicSecurityResource> dynamicResources;

  private volatile SecurityModelConfiguration defaultConfiguration;

  private volatile SecurityModelConfiguration mergedConfiguration;

  @Inject
  public DefaultConfigurationManager(final SecurityModelConfigurationSource configurationSource,
                                     final List<StaticSecurityResource> staticResources,
                                     final List<DynamicSecurityResource> dynamicResources,
                                     final List<SecurityConfigurationModifier> configurationModifiers,
                                     final SecurityConfigurationCleaner configCleaner,
                                     final SecurityConfigurationValidator validator,
                                     final PasswordService passwordService,
                                     final EventBus eventBus)
  {
    this.configurationSource = configurationSource;
    this.dynamicResources = dynamicResources;
    this.staticResources = staticResources;
    this.eventBus = eventBus;
    this.configurationModifiers = configurationModifiers;
    this.configCleaner = configCleaner;
    this.validator = validator;
    this.passwordService = passwordService;
  }

  @Override
  public List<CPrivilege> listPrivileges() {
    List<CPrivilege> privileges = Lists.newArrayList();
    privileges.addAll(getDefaultConfiguration().getPrivileges());
    privileges.addAll(getMergedConfiguration().getPrivileges());
    return Collections.unmodifiableList(privileges);
  }

  @Override
  public List<CRole> listRoles() {
    List<CRole> roles = Lists.newArrayList();
    roles.addAll(getDefaultConfiguration().getRoles());
    roles.addAll(getMergedConfiguration().getRoles());
    return Collections.unmodifiableList(roles);
  }

  @Override
  public List<CUser> listUsers() {
    return Collections.unmodifiableList(getDefaultConfiguration().getUsers());
  }

  @Override
  public List<CUserRoleMapping> listUserRoleMappings() {
    return Collections.unmodifiableList(getDefaultConfiguration().getUserRoleMappings());
  }

  @Override
  public void createPrivilege(CPrivilege privilege) throws InvalidConfigurationException {
    createPrivilege(privilege, initializeContext());
  }

  private void createPrivilege(CPrivilege privilege, SecurityValidationContext context)
      throws InvalidConfigurationException
  {
    if (context == null) {
      context = initializeContext();
    }

    ValidationResponse vr = validator.validatePrivilege(context, privilege, false);

    if (vr.isValid()) {
      getDefaultConfiguration().addPrivilege(privilege);
      logValidationWarnings(vr);
    }
    else {
      throw new InvalidConfigurationException(vr);
    }
  }

  @Override
  public void createRole(CRole role) throws InvalidConfigurationException {
    createRole(role, initializeContext());
  }

  private void createRole(CRole role, SecurityValidationContext context) throws InvalidConfigurationException {
    if (context == null) {
      context = initializeContext();
    }

    ValidationResponse vr = validator.validateRole(context, role, false);

    if (vr.isValid()) {
      getDefaultConfiguration().addRole(role);
      logValidationWarnings(vr);
    }
    else {
      throw new InvalidConfigurationException(vr);
    }
  }

  @Override
  public void createUser(CUser user, Set<String> roles) throws InvalidConfigurationException {
    createUser(user, null, roles, initializeContext());
  }

  @Override
  public void createUser(CUser user, String password, Set<String> roles) throws InvalidConfigurationException {
    createUser(user, password, roles, initializeContext());
  }

  private void createUser(CUser user, String password, Set<String> roles, SecurityValidationContext context)
      throws InvalidConfigurationException
  {
    if (context == null) {
      context = initializeContext();
    }

    // set the password if its not null
    if (password != null && password.trim().length() > 0) {
      user.setPassword(this.passwordService.encryptPassword(password));
    }

    ValidationResponse vr = validator.validateUser(context, user, roles, false);

    if (vr.isValid()) {
      getDefaultConfiguration().addUser(user, roles);
      logValidationWarnings(vr);
    }
    else {
      throw new InvalidConfigurationException(vr);
    }
  }

  @Override
  public void deletePrivilege(String id) throws NoSuchPrivilegeException {
    boolean found = getDefaultConfiguration().removePrivilege(id);
    if (!found) {
      throw new NoSuchPrivilegeException(id);
    }
    cleanRemovedPrivilege(id);
  }

  @Override
  public void deleteRole(String id) throws NoSuchRoleException {
    boolean found = getDefaultConfiguration().removeRole(id);
    if (!found) {
      throw new NoSuchRoleException(id);
    }
    cleanRemovedRole(id);
  }

  @Override
  public void deleteUser(String id) throws UserNotFoundException {
    boolean found = getDefaultConfiguration().removeUser(id);

    if (!found) {
      throw new UserNotFoundException(id);
    }
  }

  @Override
  public CPrivilege readPrivilege(String id) throws NoSuchPrivilegeException {
    CPrivilege privilege = getMergedConfiguration().getPrivilege(id);
    if (privilege != null) {
      return privilege;
    }

    privilege = getDefaultConfiguration().getPrivilege(id);
    if (privilege != null) {
      return privilege;
    }

    throw new NoSuchPrivilegeException(id);
  }

  @Override
  public CRole readRole(String id) throws NoSuchRoleException {
    CRole role = getMergedConfiguration().getRole(id);
    if (role != null) {
      return role;
    }

    role = getDefaultConfiguration().getRole(id);
    if (role != null) {
      return role;
    }

    throw new NoSuchRoleException(id);
  }

  @Override
  public CUser readUser(String id) throws UserNotFoundException {
    CUser user = getDefaultConfiguration().getUser(id);

    if (user != null) {
      return user;
    }
    throw new UserNotFoundException(id);
  }

  @Override
  public void updatePrivilege(CPrivilege privilege) throws InvalidConfigurationException, NoSuchPrivilegeException {
    updatePrivilege(privilege, initializeContext());
  }

  private void updatePrivilege(CPrivilege privilege, SecurityValidationContext context)
      throws InvalidConfigurationException, NoSuchPrivilegeException
  {
    if (context == null) {
      context = initializeContext();
    }

    ValidationResponse vr = validator.validatePrivilege(context, privilege, true);

    if (vr.isValid()) {
      getDefaultConfiguration().updatePrivilege(privilege);
      logValidationWarnings(vr);
    }
    else {
      throw new InvalidConfigurationException(vr);
    }
  }

  @Override
  public void updateRole(CRole role) throws InvalidConfigurationException, NoSuchRoleException {
    updateRole(role, initializeContext());
  }

  private void updateRole(CRole role, SecurityValidationContext context)
      throws InvalidConfigurationException, NoSuchRoleException
  {
    if (context == null) {
      context = initializeContext();
    }

    ValidationResponse vr = validator.validateRole(context, role, true);

    if (vr.isValid()) {
      getDefaultConfiguration().updateRole(role);
      logValidationWarnings(vr);
    }
    else {
      throw new InvalidConfigurationException(vr);
    }
  }

  @Override
  public void updateUser(CUser user) throws InvalidConfigurationException, UserNotFoundException {
    Set<String> roles = Collections.emptySet();
    try {
      roles = readUserRoleMapping(user.getId(), UserManagerImpl.SOURCE).getRoles();
    }
    catch (NoSuchRoleMappingException e) {
      log.debug("User: {} has no roles", user.getId());
    }
    updateUser(user, roles);
  }

  @Override
  public void updateUser(CUser user, Set<String> roles) throws InvalidConfigurationException, UserNotFoundException {
    updateUser(user, roles, initializeContext());
  }

  private void updateUser(CUser user, Set<String> roles, SecurityValidationContext context)
      throws InvalidConfigurationException, UserNotFoundException
  {
    if (context == null) {
      context = initializeContext();
    }

    ValidationResponse vr = validator.validateUser(context, user, roles, true);

    if (vr.isValid()) {
      getDefaultConfiguration().updateUser(user, roles);
      logValidationWarnings(vr);
    }
    else {
      throw new InvalidConfigurationException(vr);
    }
  }

  @Override
  public void createUserRoleMapping(CUserRoleMapping userRoleMapping) throws InvalidConfigurationException {
    createUserRoleMapping(userRoleMapping, initializeContext());
  }

  private void createUserRoleMapping(CUserRoleMapping userRoleMapping, SecurityValidationContext context)
      throws InvalidConfigurationException
  {
    if (context == null) {
      context = this.initializeContext();
    }

    try {
      // this will throw a NoSuchRoleMappingException, if there isn't one
      readUserRoleMapping(userRoleMapping.getUserId(), userRoleMapping.getSource());

      ValidationResponse vr = new ValidationResponse();
      vr.addValidationError(new ValidationMessage("*", "User Role Mapping for user '"
          + userRoleMapping.getUserId() + "' already exists."));

      throw new InvalidConfigurationException(vr);
    }
    catch (NoSuchRoleMappingException e) {
      // expected
    }

    ValidationResponse vr = validator.validateUserRoleMapping(context, userRoleMapping, false);

    if (vr.getValidationErrors().size() > 0) {
      throw new InvalidConfigurationException(vr);
    }

    getDefaultConfiguration().addUserRoleMapping(userRoleMapping);
    logValidationWarnings(vr);
  }

  private void logValidationWarnings(final ValidationResponse vr) {
    final List<ValidationMessage> validationWarnings = vr.getValidationWarnings();
    if (validationWarnings != null && validationWarnings.size() > 0) {
      final StringBuilder sb = new StringBuilder();
      for (ValidationMessage msg : validationWarnings) {
        if (sb.length() >= 0) {
          sb.append(",");
        }
        sb.append(" ").append(msg.toString());
      }
      log.warn("Security configuration has validation warnings:" + sb.toString());
    }
  }

  private CUserRoleMapping readCUserRoleMapping(String userId, String source) throws NoSuchRoleMappingException {
    CUserRoleMapping mapping = getDefaultConfiguration().getUserRoleMapping(userId, source);

    if (mapping != null) {
      return mapping;
    }
    else {
      throw new NoSuchRoleMappingException("No User Role Mapping for user: " + userId);
    }
  }

  @Override
  public CUserRoleMapping readUserRoleMapping(String userId, String source) throws NoSuchRoleMappingException {
    return readCUserRoleMapping(userId, source);
  }

  @Override
  public void updateUserRoleMapping(CUserRoleMapping userRoleMapping)
      throws InvalidConfigurationException, NoSuchRoleMappingException
  {
    updateUserRoleMapping(userRoleMapping, initializeContext());
  }

  private void updateUserRoleMapping(CUserRoleMapping userRoleMapping, SecurityValidationContext context)
      throws InvalidConfigurationException, NoSuchRoleMappingException
  {
    if (context == null) {
      context = initializeContext();
    }

    if (readUserRoleMapping(userRoleMapping.getUserId(), userRoleMapping.getSource()) == null) {
      ValidationResponse vr = new ValidationResponse();
      vr.addValidationError(new ValidationMessage("*", "No User Role Mapping found for user '"
          + userRoleMapping.getUserId() + "'."));

      throw new InvalidConfigurationException(vr);
    }

    ValidationResponse vr = validator.validateUserRoleMapping(context, userRoleMapping, true);

    if (vr.getValidationErrors().size() > 0) {
      throw new InvalidConfigurationException(vr);
    }

    getDefaultConfiguration().updateUserRoleMapping(userRoleMapping);
  }

  @Override
  public void deleteUserRoleMapping(String userId, String source) throws NoSuchRoleMappingException {
    boolean found = getDefaultConfiguration().removeUserRoleMapping(userId, source);

    if (!found) {
      throw new NoSuchRoleMappingException("No User Role Mapping for user: " + userId);
    }
  }

  private SecurityValidationContext initializeContext() {
    SecurityValidationContext context = new SecurityValidationContext();

    context.addExistingUserIds();
    context.addExistingRoleIds();
    context.addExistingPrivilegeIds();

    for (CUser user : listUsers()) {
      context.getExistingUserIds().add(user.getId());
    }

    for (CRole role : listRoles()) {
      context.getExistingRoleIds().add(role.getId());
      context.getRoleContainmentMap().put(role.getId(), Lists.newArrayList(role.getRoles()));
      context.getExistingRoleNameMap().put(role.getId(), role.getName());
    }

    for (CPrivilege privilege : listPrivileges()) {
      context.getExistingPrivilegeIds().add(privilege.getId());
    }

    return context;
  }

  @Override
  public void cleanRemovedPrivilege(String privilegeId) {
    configCleaner.privilegeRemoved(getDefaultConfiguration(), privilegeId);
  }

  @Override
  public void cleanRemovedRole(String roleId) {
    configCleaner.roleRemoved(getDefaultConfiguration(), roleId);
  }

  private SecurityModelConfiguration getDefaultConfiguration() {
    // Assign configuration to local variable first, as calls to clearCache can null it out at any time
    SecurityModelConfiguration configuration = this.defaultConfiguration;
    if (configuration == null) {
      synchronized (this) {
        // double-checked locking of volatile is apparently OK with java5+
        // http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
        configuration = this.defaultConfiguration;
        if (configuration == null) {
          this.defaultConfiguration = configuration = doGetDefaultConfiguration();
        }
      }
    }
    return configuration;
  }

  private SecurityModelConfiguration doGetDefaultConfiguration() {
    configurationSource.loadConfiguration();
    for (SecurityConfigurationModifier modifier : configurationModifiers) {
      modifier.apply(configurationSource.getConfiguration());
    }
    return configurationSource.getConfiguration();
  }

  private SecurityModelConfiguration getMergedConfiguration() {
    // Assign configuration to local variable first, as calls to clearCache can null it out at any time
    SecurityModelConfiguration configuration = this.mergedConfiguration;
    if (configuration == null || shouldRebuildMergedConfiguration()) {
      boolean rebuiltConfiguration = false;

      synchronized (this) {
        // double-checked locking of volatile is apparently OK with java5+
        // http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
        configuration = this.mergedConfiguration;
        if (configuration == null || shouldRebuildMergedConfiguration()) {
          rebuiltConfiguration = (configuration != null);
          this.mergedConfiguration = configuration = doGetMergedConfiguration();
        }
      }

      if (rebuiltConfiguration) {
        // signal rebuild (outside lock to avoid contention)
        eventBus.post(new AuthorizationConfigurationChanged());
      }
    }
    return configuration;
  }

  private boolean shouldRebuildMergedConfiguration() {
    for (DynamicSecurityResource resource : dynamicResources) {
      if (resource.isDirty()) {
        return true;
      }
    }
    return false;
  }

  private Configuration doGetMergedConfiguration() {
    final Configuration configuration = new Configuration();

    for (StaticSecurityResource resource : staticResources) {
      SecurityModelConfiguration resConfig = resource.getConfiguration();

      if (resConfig != null) {
        checkState(
            resConfig.getUsers() == null || resConfig.getUsers().isEmpty(),
            "Static resources cannot have users"
        );
        checkState(
            resConfig.getUserRoleMappings() == null || resConfig.getUserRoleMappings().isEmpty(),
            "Static resources cannot have user/role mappings"
        );
        appendConfig(configuration, resConfig);
      }
    }

    for (DynamicSecurityResource resource : dynamicResources) {
      SecurityModelConfiguration resConfig = resource.getConfiguration();

      if (resConfig != null) {
        checkState(
            resConfig.getUsers() == null || resConfig.getUsers().isEmpty(),
            "Dynamic resources cannot have users"
        );
        checkState(
            resConfig.getUserRoleMappings() == null || resConfig.getUserRoleMappings().isEmpty(),
            "Dynamic resources cannot have user/role mappings"
        );
        appendConfig(configuration, resConfig);
      }
    }

    return configuration;
  }

  private SecurityModelConfiguration appendConfig(final SecurityModelConfiguration to,
                                                  final SecurityModelConfiguration from)
  {
    for (CPrivilege privilege : from.getPrivileges()) {
      privilege.setReadOnly(true);
      to.addPrivilege(privilege);
    }

    // number of roles can be significant (>15K), so need to speedup lookup roles by roleId
    final Map<String, CRole> roles = new HashMap<String, CRole>();
    for (CRole role : to.getRoles()) {
      roles.put(role.getId(), role);
    }

    for (Iterator<CRole> iterator = from.getRoles().iterator(); iterator.hasNext(); ) {
      CRole role = iterator.next();

      // need to check if we need to merge the static config
      CRole eachRole = roles.get(role.getId());
      if (eachRole != null) {
        role = this.mergeRolesContents(role, eachRole);
        to.removeRole(role.getId());
      }

      role.setReadOnly(true);
      to.addRole(role);
      roles.put(role.getId(), role); // deduplicate config roles
    }

    return to;
  }

  private CRole mergeRolesContents(CRole roleA, CRole roleB) {
    // ROLES
    Set<String> roles = new HashSet<String>();
    // make sure they are not empty
    if (roleA.getRoles() != null) {
      roles.addAll(roleA.getRoles());
    }
    if (roleB.getRoles() != null) {
      roles.addAll(roleB.getRoles());
    }

    // PRIVS
    Set<String> privs = new HashSet<String>();
    // make sure they are not empty
    if (roleA.getPrivileges() != null) {
      privs.addAll(roleA.getPrivileges());
    }
    if (roleB.getPrivileges() != null) {
      privs.addAll(roleB.getPrivileges());
    }

    CRole newRole = new CRole();
    newRole.setId(roleA.getId());
    newRole.setRoles(Sets.newHashSet(roles));
    newRole.setPrivileges(Sets.newHashSet(privs));

    // now for the name and description
    if (StringUtils.isNotEmpty(roleA.getName())) {
      newRole.setName(roleA.getName());
    }
    else {
      newRole.setName(roleB.getName());
    }

    if (StringUtils.isNotEmpty(roleA.getDescription())) {
      newRole.setDescription(roleA.getDescription());
    }
    else {
      newRole.setDescription(roleB.getDescription());
    }

    return newRole;
  }
}
