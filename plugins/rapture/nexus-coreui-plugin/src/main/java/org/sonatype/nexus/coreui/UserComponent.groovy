/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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

import com.google.inject.Key
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.bval.guice.Validate
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.apache.shiro.authz.annotation.RequiresUser
import org.eclipse.sisu.inject.BeanLocator
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.user.UserAccountManager
import org.sonatype.nexus.util.Tokens
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.nexus.wonderland.AuthTicketService
import org.sonatype.security.SecuritySystem
import org.sonatype.security.usermanagement.*
import org.sonatype.security.usermanagement.xml.SecurityXmlUserManager

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

/**
 * User {@link DirectComponent}.
 *
 * @since 2.8
 */
@Named
@Singleton
@DirectAction(action = 'coreui_User')
class UserComponent
extends DirectComponentSupport
{

  public static final String DEFAULT_SOURCE = SecurityXmlUserManager.SOURCE

  @Inject
  SecuritySystem securitySystem

  @Inject
  UserAccountManager userAccountManager

  @Inject
  AuthTicketService authTickets

  @Inject
  BeanLocator beanLocator

  /**
   * Retrieve a list of available users.
   */
  @DirectMethod
  @RequiresPermissions('security:users:read')
  List<UserXO> read(final @Nullable StoreLoadParameters parameters) {
    def source = parameters?.getFilter('source')
    if (!source) {
      source = DEFAULT_SOURCE
    }
    securitySystem.searchUsers(new UserSearchCriteria(source: source)).collect { user ->
      asUserXO(user)
    }
  }

  /**
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
   * @return current logged in user account.
   */
  @DirectMethod
  @RequiresPermissions('security:users:read')
  @RequiresUser
  UserAccountXO readAccount() {
    String currentUserId = securitySystem.getSubject().getPrincipal().toString()
    User user = userAccountManager.readAccount(currentUserId)
    return new UserAccountXO(
        id: user.userId,
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.emailAddress
    )
  }

  /**
   * @param userXO to be created
   * @return created user
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:users:create')
  @Validate(groups = [Create.class, Default.class])
  UserXO create(final @NotNull(message = 'UserXO may not be null') @Valid UserXO userXO) {
    asUserXO(securitySystem.addUser(new DefaultUser(
        userId: userXO.id,
        source: DEFAULT_SOURCE,
        firstName: userXO.firstName,
        lastName: userXO.lastName,
        emailAddress: userXO.email,
        status: userXO.status,
        roles: userXO.roles?.collect { id ->
          new RoleIdentifier(DEFAULT_SOURCE, id)
        }
    ), userXO.password?.valueIfValid))
  }

  /**
   * @param userXO to be updated
   * @return updated user
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:users:create')
  @Validate(groups = [Update.class, Default.class])
  UserXO update(final @NotNull(message = 'UserXO may not be null') @Valid UserXO userXO) {
    asUserXO(securitySystem.updateUser(securitySystem.getUser(userXO.id).with {
      firstName = userXO.firstName
      lastName = userXO.lastName
      emailAddress = userXO.email
      status = userXO.status
      roles = userXO.roles?.collect { id ->
        new RoleIdentifier(DEFAULT_SOURCE, id)
      }
      return it
    }))
  }

  /**
   * @param userAccountXO to be updated
   * @return current logged in user account
   */
  @DirectMethod
  @RequiresUser
  @RequiresAuthentication
  @RequiresPermissions('security:users:create')
  @Validate
  UserAccountXO updateAccount(final @NotNull(message = 'UserAccountXO may not be null') @Valid UserAccountXO userAccountXO) {
    String currentUserId = securitySystem.getSubject().getPrincipal().toString()
    userAccountManager.updateAccount(userAccountManager.readAccount(currentUserId).with {
      firstName = userAccountXO.firstName
      lastName = userAccountXO.lastName
      emailAddress = userAccountXO.email
      return it
    })
    return readAccount()
  }

  @DirectMethod
  @RequiresUser
  @RequiresAuthentication
  @RequiresPermissions('security:userschangepw:update')
  @Validate
  void changePassword(final @NotNull(message = 'AuthToken may not be null') String authToken,
                      final @NotNull(message = 'Password may not be null') String password)
  {
    if (authTickets.redeemTicket(authToken)) {
      String currentUserId = securitySystem.getSubject().getPrincipal().toString()
      securitySystem.changePassword(currentUserId, Tokens.decodeBase64String(password))
    }
    else {
      throw new IllegalAccessException('Invalid authentication ticket')
    }
  }

  /**
   * Deletes a user.
   * @param id of user to be deleted
   * @param realm of user to be deleted
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:users:delete')
  @Validate
  void delete(final @NotNull(message = 'ID may not be null') String id,
              final @NotNull(message = 'Realm may not be null') String realm)
  {
    securitySystem.deleteUser(id, realm)
  }

  private static asUserXO(final User user) {
    new UserXO(
        id: user.userId,
        realm: user.source,
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.emailAddress,
        status: user.status,
        password: Password.fakePassword(),
        roles: user.roles.collect { role ->
          role.roleId
        }
    )
  }

}
