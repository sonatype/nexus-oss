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

package org.sonatype.nexus.rapture.internal.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

import org.sonatype.nexus.extdirect.DirectComponentSupport;
import org.sonatype.nexus.rapture.StateContributor;
import org.sonatype.nexus.util.Tokens;
import org.sonatype.nexus.validation.Validate;
import org.sonatype.nexus.wonderland.AuthTicketService;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.Privilege;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.softwarementors.extjs.djn.config.annotations.DirectAction;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;
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

  private final AuthTicketService authTickets;

  @Inject
  public SecurityComponent(final SecuritySystem securitySystem,
                           final AuthTicketService authTickets)
  {
    this.securitySystem = checkNotNull(securitySystem);
    this.authTickets = checkNotNull(authTickets);
  }

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
    return login(base64Username, base64Password, rememberMe);
  }

  @DirectMethod
  @Validate
  public UserXO login(final @NotEmpty(message = "[base64Username] may not be empty") String base64Username,
                      final @NotEmpty(message = "[base64Password] may not be empty") String base64Password,
                      final boolean rememberMe) throws Exception
  {
    try {
      securitySystem.login(new UsernamePasswordToken(
          Tokens.decodeBase64String(base64Username), Tokens.decodeBase64String(base64Password), rememberMe
      ));
    }
    catch (Exception e) {
      throw new Exception("Authentication failed", e);
    }
    return getUser();
  }

  @DirectMethod
  public void logout() {
    Subject subject = securitySystem.getSubject();

    if (subject != null) {
      subject.logout();
    }

    if (WebContextManager.isWebContextAttachedToCurrentThread()) {
      HttpSession session = WebContextManager.get().getRequest().getSession(false);
      if (session != null) {
        session.invalidate();
      }
    }
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

    String username = Tokens.decodeBase64String(base64Username);
    String password = Tokens.decodeBase64String(base64Password);
    log.debug("Authenticate w/username: {}, password: {}", username, Tokens.mask(password));

    // Require current user to be the requested user to authenticate
    subject = securitySystem.getSubject();
    if (!subject.getPrincipal().toString().equals(username)) {
      throw new Exception("Username mismatch");
    }

    // Ask the sec-manager to authenticate, this won't alter the current subject
    try {
      securitySystem.getSecurityManager().authenticate(new UsernamePasswordToken(username, password));
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
      Object principal = subject.getPrincipal();
      if (principal != null) {
        userXO.setId(principal.toString());
        if (securitySystem.isAnonymousAccessEnabled() && userXO.getId().equals(securitySystem.getAnonymousUsername())) {
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
      permissions = asPermissions(calculatePrivileges(subject));
    }
    return permissions;
  }

  @Override
  public Map<String, Object> getState() {
    HashMap<String, Object> state = Maps.newHashMap();
    state.put("user", getUser());
    state.put(
        "anonymousUsername", securitySystem.isAnonymousAccessEnabled() ? securitySystem.getAnonymousUsername() : null
    );
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

  private Map<String, Integer> calculatePrivileges(final Subject subject) {
    Map<String, Integer> privilegeMap = Maps.newHashMap();

    for (Privilege priv : securitySystem.listPrivileges()) {
      if (priv.getType().equals("method")) {
        String permission = priv.getPrivilegeProperty("permission");
        privilegeMap.put(permission, NONE);
      }
    }

    List<Permission> permissionList = Lists.newArrayList();
    List<String> permissionNameList = Lists.newArrayList();

    for (String privilegeKey : privilegeMap.keySet()) {
      permissionList.add(new WildcardPermission(privilegeKey + ":read"));
      permissionList.add(new WildcardPermission(privilegeKey + ":create"));
      permissionList.add(new WildcardPermission(privilegeKey + ":update"));
      permissionList.add(new WildcardPermission(privilegeKey + ":delete"));
      permissionNameList.add(privilegeKey + ":read");
      permissionNameList.add(privilegeKey + ":create");
      permissionNameList.add(privilegeKey + ":update");
      permissionNameList.add(privilegeKey + ":delete");
    }

    // get the privileges for this subject
    boolean[] boolResults = subject.isPermitted(permissionList);

    // put then in a map so we can access them easily
    Map<String, Boolean> resultMap = Maps.newHashMap();
    for (int ii = 0; ii < permissionList.size(); ii++) {
      String permissionName = permissionNameList.get(ii);
      boolean b = boolResults[ii];
      resultMap.put(permissionName, b);
    }

    // now loop through the original set and figure out the correct value
    for (Entry<String, Integer> priv : privilegeMap.entrySet()) {

      boolean readPriv = resultMap.get(priv.getKey() + ":read");
      boolean createPriv = resultMap.get(priv.getKey() + ":create");
      boolean updaetPriv = resultMap.get(priv.getKey() + ":update");
      boolean deletePriv = resultMap.get(priv.getKey() + ":delete");

      int perm = NONE;

      if (readPriv) {
        perm |= READ;
      }
      if (createPriv) {
        perm |= CREATE;
      }
      if (updaetPriv) {
        perm |= UPDATE;
      }
      if (deletePriv) {
        perm |= DELETE;
      }
      // now set the value
      priv.setValue(perm);
    }

    return privilegeMap;
  }

  private List<PermissionXO> asPermissions(final Map<String, Integer> privilegeMap) {
    List<PermissionXO> perms = Lists.newArrayList();

    for (Entry<String, Integer> entry : privilegeMap.entrySet()) {
      if (entry.getValue() > NONE) {
        PermissionXO permissionXO = new PermissionXO();
        permissionXO.setId(entry.getKey());
        permissionXO.setValue(entry.getValue());

        perms.add(permissionXO);
      }
    }

    Collections.sort(perms, new Comparator<PermissionXO>()
    {
      @Override
      public int compare(final PermissionXO o1, final PermissionXO o2) {
        return o1.getId().compareTo(o2.getId());
      }
    });

    return perms;
  }

}
