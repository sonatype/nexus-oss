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
package org.sonatype.nexus.coreui

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

import org.sonatype.micromailer.Address
import org.sonatype.nexus.common.text.Strings2
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.security.SecuritySystem
import org.sonatype.nexus.security.anonymous.AnonymousManager
import org.sonatype.nexus.security.role.RoleIdentifier
import org.sonatype.nexus.security.user.User
import org.sonatype.nexus.security.user.UserManager
import org.sonatype.nexus.security.user.UserSearchCriteria
import org.sonatype.nexus.validation.Validate
import org.sonatype.nexus.validation.ValidationMessage
import org.sonatype.nexus.validation.ValidationResponse
import org.sonatype.nexus.validation.ValidationResponseException
import org.sonatype.nexus.validation.group.Create
import org.sonatype.nexus.validation.group.Update
import org.sonatype.nexus.wonderland.AuthTicketService

import com.google.inject.Key
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import groovy.transform.PackageScope
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.apache.shiro.authz.annotation.RequiresUser
import org.apache.shiro.subject.Subject
import org.eclipse.sisu.inject.BeanLocator
import org.hibernate.validator.constraints.NotEmpty

/**
 * User {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_User')
class UserComponent
extends DirectComponentSupport
{

  public static final String DEFAULT_SOURCE = UserManager.DEFAULT_SOURCE

  @Inject
  SecuritySystem securitySystem

  @Inject
  AnonymousManager anonymousManager

  @Inject
  AuthTicketService authTickets

  @Inject
  BeanLocator beanLocator

  /**
   * Retrieve users.
   * @return a list of users
   */
  @DirectMethod
  @RequiresPermissions('security:users:read')
  List<UserXO> read(final @Nullable StoreLoadParameters parameters) {
    def source = parameters?.getFilter('source')
    if (!source) {
      source = DEFAULT_SOURCE
    }
    def userId = parameters?.getFilter('userId')
    securitySystem.searchUsers(new UserSearchCriteria(source: source, userId: userId)).collect { user ->
      asUserXO(user)
    }
  }

  /**
   * Retrieves available user sources.
   * @return a list of available user sources (user managers)
   */
  @DirectMethod
  @RequiresPermissions('security:users:read')
  List<ReferenceXO> readSources() {
    beanLocator.locate(Key.get(UserManager.class, Named.class)).collect { entry ->
      new ReferenceXO(
          id: entry.key.value,
          name: entry.description ?: entry.key.value
      )
    }
  }

  /**
   * Retrieves user account (logged in user info).
   * @return current logged in user account.
   */
  @DirectMethod
  @RequiresUser
  @RequiresPermissions('security:users')
  UserAccountXO readAccount() {
    User user = securitySystem.currentUser()
    return new UserAccountXO(
        userId: user.userId,
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.emailAddress
    )
  }

  /**
   * Creates a user.
   * @param userXO to be created
   * @return created user
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:users:create')
  @Validate(groups = [Create.class, Default.class])
  UserXO create(final @NotNull(message = '[userXO] may not be null') @Valid UserXO userXO) {
    asUserXO(securitySystem.addUser(new User(
        userId: userXO.userId,
        source: DEFAULT_SOURCE,
        firstName: userXO.firstName,
        lastName: userXO.lastName,
        emailAddress: validateEmail(userXO.email),
        status: userXO.status,
        roles: userXO.roles?.collect { id ->
          new RoleIdentifier(DEFAULT_SOURCE, id)
        }
    ), userXO.password?.valueIfValid))
  }

  /**
   * Update a user.
   * @param userXO to be updated
   * @return updated user
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:users:update')
  @Validate(groups = [Update.class, Default.class])
  UserXO update(final @NotNull(message = '[userXO] may not be null') @Valid UserXO userXO) {
    asUserXO(securitySystem.updateUser(new User(
        userId: userXO.userId,
        version: userXO.version,
        source: DEFAULT_SOURCE,
        firstName: userXO.firstName,
        lastName: userXO.lastName,
        emailAddress: validateEmail(userXO.email),
        status: userXO.status,
        roles: userXO.roles?.collect { id ->
          new RoleIdentifier(DEFAULT_SOURCE, id)
        }
    )))
  }

  /**
   * Update user role mappings.
   * @param userRoleMappingsXO to be updated
   * @return updated user
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:users:update')
  @Validate(groups = [Update.class, Default.class])
  UserXO updateRoleMappings(final @NotNull(message = '[UserRoleMappingsXO] may not be null') @Valid UserRoleMappingsXO userRoleMappingsXO) {
    def mappedRoles = userRoleMappingsXO.roles
    if (mappedRoles?.size()) {
      User user = securitySystem.getUser(userRoleMappingsXO.userId, userRoleMappingsXO.realm)
      user.roles.each { role ->
        if (role.source == userRoleMappingsXO.realm) {
          mappedRoles.remove(role.roleId)
        }
      }
    }
    securitySystem.setUsersRoles(
        userRoleMappingsXO.userId,
        userRoleMappingsXO.realm,
        mappedRoles?.size() > 0
        ? mappedRoles?.collect { roleId -> new RoleIdentifier(DEFAULT_SOURCE, roleId) } as Set
        : null
    )
    return asUserXO(securitySystem.getUser(userRoleMappingsXO.userId, userRoleMappingsXO.realm))
  }

  /**
   * Update user account (logged in user info).
   * @param userAccountXO to be updated
   * @return current logged in user account
   */
  @DirectMethod
  @RequiresUser
  @RequiresAuthentication
  @RequiresPermissions('security:users')
  @Validate
  UserAccountXO updateAccount(final @NotNull(message = '[userAccountXO] may not be null') @Valid UserAccountXO userAccountXO) {
    User user = securitySystem.currentUser().with {
      firstName = userAccountXO.firstName
      lastName = userAccountXO.lastName
      emailAddress = userAccountXO.email
      return it
    }
    securitySystem.updateUser(user)
    return readAccount()
  }

  /**
   * Change password of a specified user.
   * @param authToken authentication token
   * @param userId id of user to change password for
   * @param password new password
   */
  @DirectMethod
  @RequiresUser
  @RequiresAuthentication
  @RequiresPermissions('security:userschangepw:create')
  @Validate
  void changePassword(final @NotEmpty(message = '[authToken] may not be empty') String authToken,
                      final @NotEmpty(message = '[userId] may not be empty') String userId,
                      final @NotEmpty(message = '[password] may not be empty') String password)
  {
    if (authTickets.redeemTicket(authToken)) {
      if (isAnonymousUser(userId)) {
        throw new Exception("Password cannot be changed for user ${userId}, since is marked as the Anonymous user")
      }
      securitySystem.changePassword(userId, Strings2.decodeBase64(password))
    }
    else {
      throw new IllegalAccessException('Invalid authentication ticket')
    }
  }

  /**
   * Resets password of a specified user.
   * @param authToken authentication token
   * @param userId id of user to reset password for
   */
  @DirectMethod
  @RequiresUser
  @RequiresAuthentication
  @RequiresPermissions('security:usersreset:delete')
  @Validate
  void resetPassword(final @NotEmpty(message = '[authToken] may not be empty') String authToken,
                     final @NotEmpty(message = '[userId] may not be empty') String userId)
  {
    if (authTickets.redeemTicket(authToken)) {
      if (isAnonymousUser(userId)) {
        throw new Exception("Password cannot be reset for user ${userId}, since is marked as the Anonymous user")
      }

      // FIXME: This api has been removed, should sort out why this extdirect endpoint still exists
      securitySystem.resetPassword(userId)
    }
    else {
      throw new IllegalAccessException('Invalid authentication ticket')
    }
  }

  /**
   * Deletes a user.
   * @param id of user to be deleted
   * @param source of user to be deleted
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:users:delete')
  @Validate
  void remove(final @NotEmpty(message = '[id] may not be empty') String id,
              final @NotEmpty(message = '[source] may not be empty') String source)
  {
    // TODO check if source is required or we always delete from default realm
    if (isAnonymousUser(id)) {
      throw new Exception("User ${id} cannot be deleted, since is marked as the Anonymous user")
    }
    if (isCurrentUser(id)) {
      throw new Exception("User ${id} cannot be deleted, since is the user currently logged into the application")
    }
    securitySystem.deleteUser(id, source)
  }

  @PackageScope
  UserXO asUserXO(final User user) {
    UserXO userXO = new UserXO(
        userId: user.userId,
        version: user.version,
        realm: user.source,
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.emailAddress,
        status: user.status,
        password: Password.fakePassword(),
        roles: user.roles.collect { role ->
          role.roleId
        },
        external: user.source != DEFAULT_SOURCE
    )
    if (userXO.external) {
      userXO.externalRoles = user.roles
          .findResults { RoleIdentifier role -> return role.source == DEFAULT_SOURCE ? null : role }
          .collect { role -> role.roleId }
    }
    return userXO
  }

  private boolean isAnonymousUser(final String userId) {
    def config = anonymousManager.configuration
    return config.enabled && config.userId == userId
  }

  private boolean isCurrentUser(final String userId) {
    Subject subject = securitySystem.subject
    if (!subject || !subject.principal) {
      return false
    }
    return subject.principal == userId
  }

  @PackageScope
  String validateEmail(final String email) {
    if (email) {
      try {
        new Address(email)
      }
      catch (IllegalArgumentException e) {
        def validations = new ValidationResponse()
        def message = e.message
        if (e.cause?.message) {
          message += ': ' + e.cause.message
        }
        validations.addError(new ValidationMessage('email', message))
        throw new ValidationResponseException(validations)
      }
    }
    return email
  }

}
