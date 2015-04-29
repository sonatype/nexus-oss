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
package org.sonatype.nexus.rapture.internal.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.extdirect.DirectComponentSupport;
import org.sonatype.nexus.rapture.StateContributor;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.anonymous.AnonymousConfiguration;
import org.sonatype.nexus.security.anonymous.AnonymousManager;
import org.sonatype.nexus.security.privilege.Privilege;
import org.sonatype.nexus.validation.Validate;
import org.sonatype.nexus.wonderland.AuthTicketService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.softwarementors.extjs.djn.config.annotations.DirectAction;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.Subject;
import org.hibernate.validator.constraints.NotEmpty;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.rapture.internal.ui.StateComponent.shouldSend;

/**
 * Security Ext.Direct component.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = "rapture_Security")
public class SecurityComponent
    extends DirectComponentSupport
    implements StateContributor
{

  private static final int NONE = 0;

  private static final int READ = 1;

  private static final int UPDATE = 2;

  private static final int DELETE = 4;

  private static final int CREATE = 8;

  private final SecuritySystem securitySystem;

  private final AnonymousManager anonymousManager;

  private final AuthTicketService authTickets;

  @Inject
  public SecurityComponent(final SecuritySystem securitySystem,
                           final AnonymousManager anonymousManager,
                           final AuthTicketService authTickets)
  {
    this.securitySystem = checkNotNull(securitySystem);
    this.anonymousManager = checkNotNull(anonymousManager);
    this.authTickets = checkNotNull(authTickets);
  }

  // FIXME: Move authenticate to session servlet

  @DirectMethod
  @Validate
  public UserXO authenticate(final @NotEmpty(message = "[base64Username] may not be empty") String base64Username,
                             final @NotEmpty(message = "[base64Password] may not be empty") String base64Password)
      throws Exception
  {
    boolean rememberMe = false;
    Subject subject = securitySystem.getSubject();
    if (subject != null) {
      rememberMe = subject.isRemembered();
    }

    try {
      securitySystem.login(new UsernamePasswordToken(
          Strings2.decodeBase64(base64Username),
          Strings2.decodeBase64(base64Password),
          rememberMe
      ));
    }
    catch (Exception e) {
      throw new Exception("Authentication failed", e);
    }

    return getUser();
  }

  @DirectMethod
  @Validate
  public String authenticationToken(
      final @NotEmpty(message = "[base64Username] may not be empty") String base64Username,
      final @NotEmpty(message = "[base64Password] may not be empty") String base64Password)
      throws Exception
  {
    Subject subject = securitySystem.getSubject();
    if (subject == null || !subject.isAuthenticated()) {
      authenticate(base64Username, base64Password);
    }

    String username = Strings2.decodeBase64(base64Username);
    String password = Strings2.decodeBase64(base64Password);
    log.debug("Authenticate w/username: {}, password: {}", username, Strings2.mask(password));

    // Require current user to be the requested user to authenticate
    subject = securitySystem.getSubject();
    if (!subject.getPrincipal().toString().equals(username)) {
      throw new Exception("Username mismatch");
    }

    // Ask the sec-manager to authenticate, this won't alter the current subject
    try {
      securitySystem.getRealmSecurityManager().authenticate(new UsernamePasswordToken(username, password));
    }
    catch (AuthenticationException e) {
      throw new Exception("Authentication failed", e);
    }

    // At this point we should be authenticated, return a new ticket
    return authTickets.createTicket();
  }

  @DirectMethod
  public UserXO getUser() {
    UserXO userXO = null;

    Subject subject = securitySystem.getSubject();
    if (isLoggedIn(subject)) {
      userXO = new UserXO();
      userXO.setAuthenticated(subject.isAuthenticated());

      // HACK: roles for the current user are not exposed to the UI.
      // HACK: but we need to know if user is admin or not for some things (like outreach)
      if (subject.hasRole("nx-admin")) {
        userXO.setAdministrator(true);
      }

      Object principal = subject.getPrincipal();
      if (principal != null) {
        userXO.setId(principal.toString());
        AnonymousConfiguration anonymousConfiguration = anonymousManager.getConfiguration();
        if (anonymousConfiguration.isEnabled() && userXO.getId().equals(anonymousConfiguration.getUserId())) {
          userXO = null;
        }
      }
    }
    return userXO;
  }

  @DirectMethod
  public List<PermissionXO> getPermissions() {
    List<PermissionXO> permissions = calculatePermissions();
    // store hash so we do not send later on a command to fetch
    shouldSend("permissions", permissions);
    return permissions;
  }

  public List<PermissionXO> calculatePermissions() {
    List<PermissionXO> permissions = null;
    Subject subject = securitySystem.getSubject();
    if (isLoggedIn(subject)) {
      permissions = calculatePermissions(subject);
    }
    return permissions;
  }

  @Override
  public Map<String, Object> getState() {
    HashMap<String, Object> state = Maps.newHashMap();
    state.put("user", getUser());

    AnonymousConfiguration anonymousConfiguration = anonymousManager.getConfiguration();
    state.put("anonymousUsername", anonymousConfiguration.isEnabled() ? anonymousConfiguration.getUserId() : null);
    return state;
  }

  @Override
  public Map<String, Object> getCommands() {
    HashMap<String, Object> commands = Maps.newHashMap();

    List<PermissionXO> permissions = calculatePermissions();
    if (permissions != null && shouldSend("permissions", permissions)) {
      commands.put("fetchpermissions", null);
    }

    return commands;
  }

  private boolean isLoggedIn(final Subject subject) {
    return subject != null && (subject.isRemembered() || subject.isAuthenticated());
  }

  private List<PermissionXO> calculatePermissions(final Subject subject) {
    List<Permission> permissionList = Lists.newArrayList();
    List<String> permissionNameList = Lists.newArrayList();
    List<PermissionXO> permissions = Lists.newArrayList();

    for (Privilege priv : securitySystem.listPrivileges()) {
      if (priv.getPermission() instanceof WildcardPermission) {
        WildcardPermission permission = (WildcardPermission) priv.getPermission();
        List<Set<String>> parts = SecurityUtils.getParts(permission);
        processParts(0, parts, null, permissionList, permissionNameList);
      }
    }

    boolean[] boolResults = subject.isPermitted(permissionList);

    for (int i = 0; i < permissionList.size(); i++) {
      if (boolResults[i]) {
        PermissionXO permissionXO = new PermissionXO();
        permissionXO.setId(permissionNameList.get(i));
        permissions.add(permissionXO);
      }
    }

    Collections.sort(permissions, new Comparator<PermissionXO>()
    {
      @Override
      public int compare(final PermissionXO o1, final PermissionXO o2) {
        return o1.getId().compareTo(o2.getId());
      }
    });

    return permissions;
  }

  /**
   * Expand multiple parts as for example nexus:foo:create,read into nexus:foo:create and nexus:foo:read
   */
  private void processParts(final int partIndex,
                            final List<Set<String>> parts,
                            final String name,
                            final List<Permission> permissionList,
                            final List<String> permissionNameList)
  {
    for (String part : parts.get(partIndex)) {
      String newName = name == null ? part : name + ":" + part;
      if (partIndex < parts.size() - 1) {
        processParts(partIndex + 1, parts, newName, permissionList, permissionNameList);
      }
      else {
        permissionList.add(new WildcardPermission(newName));
        permissionNameList.add(newName);
      }
    }
  }

}
