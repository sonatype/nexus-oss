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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import com.sonatype.security.ldap.realms.persist.model.CLdapConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;

import org.sonatype.configuration.validation.ValidationContext;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.realms.validator.ConfigurationIdGenerator;

import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class DefaultLdapConfigurationValidator
    implements LdapConfigurationValidator
{

  private final ConfigurationIdGenerator idGenerator;

  @Inject
  public DefaultLdapConfigurationValidator(final ConfigurationIdGenerator idGenerator) {
    this.idGenerator = checkNotNull(idGenerator);
  }

  public ValidationResponse validateLdapServerConfiguration(CLdapServerConfiguration ldapServerConfiguration,
                                                            boolean update)
  {
    ValidationResponse response = new ValidationResponse();

    if (ldapServerConfiguration == null) {
      ValidationMessage msg = new ValidationMessage("*", "Configuration is missing.");
      response.addValidationError(msg);
    }
    else {

      if (StringUtils.isEmpty(ldapServerConfiguration.getId()) && !update) {
        ValidationMessage msg = new ValidationMessage("id", "Id was generated.");
        response.addValidationWarning(msg);

        ldapServerConfiguration.setId(idGenerator.generateId());
      }
      else if (StringUtils.isEmpty(ldapServerConfiguration.getId()) && update) {
        ValidationMessage msg = new ValidationMessage("id", "Id cannot be empty.");
        response.addValidationWarning(msg);
      }

      if (StringUtils.isEmpty(ldapServerConfiguration.getName())) {
        ValidationMessage msg = new ValidationMessage("name", "Name cannot be empty.");
        response.addValidationError(msg);
      }

      if (ldapServerConfiguration.getConnectionInfo() != null) {
        ValidationResponse vr = this.validateConnectionInfo(null, ldapServerConfiguration.getConnectionInfo());
        response.append(vr);
      }
      else {
        ValidationMessage msg = new ValidationMessage("*", "Connection Configuration is missing.");
        response.addValidationError(msg);
      }

      if (ldapServerConfiguration.getUserAndGroupConfig() != null) {
        ValidationResponse vr = this.validateUserAndGroupAuthConfiguration(null, ldapServerConfiguration
            .getUserAndGroupConfig());
        response.append(vr);
      }
      else {
        ValidationMessage msg = new ValidationMessage("*", "User And Group Configuration is missing.");
        response.addValidationError(msg);
      }
    }

    return response;
  }

  public ValidationResponse validateModel(ValidationRequest<CLdapConfiguration> validationRequest) {
    ValidationResponse response = new ValidationResponse();

    for (CLdapServerConfiguration ldapServer : validationRequest.getConfiguration().getServers()) {
      response.append(this.validateLdapServerConfiguration(ldapServer, false));
    }

    return response;
  }

  public ValidationResponse validateConnectionInfo(ValidationContext ctx, CConnectionInfo connectionInfo) {
    ValidationResponse response = new ValidationResponse();

    if (StringUtils.isEmpty(connectionInfo.getHost())) {
      ValidationMessage msg = new ValidationMessage("host", "Host cannot be empty.");
      response.addValidationError(msg);
    }
    if (StringUtils.isEmpty(connectionInfo.getAuthScheme())) {
      ValidationMessage msg = new ValidationMessage("authScheme", "Authorization Scheme cannot be empty.");
      response.addValidationError(msg);
    }
    if (StringUtils.isEmpty(connectionInfo.getProtocol())) {
      ValidationMessage msg = new ValidationMessage("protocol", "Protocol cannot be empty.");
      response.addValidationError(msg);
    }
    if (StringUtils.isEmpty(connectionInfo.getSearchBase())) {
      ValidationMessage msg = new ValidationMessage("searchBase", "Search Base cannot be empty.");
      response.addValidationError(msg);
    }
    if (connectionInfo.getPort() < 1) {
      ValidationMessage msg = new ValidationMessage("port", "Port cannot be empty.");
      response.addValidationError(msg);
    }

    if (StringUtils.isNotEmpty(connectionInfo.getAuthScheme())
        && !connectionInfo.getAuthScheme().toLowerCase().equals("none")) {

      // user and pass required if authScheme != none
      if (StringUtils.isEmpty(connectionInfo.getSystemUsername())) {
        ValidationMessage msg = new ValidationMessage(
            "systemUsername",
            "Username cannot be empty unless the 'Authorization Scheme' is 'Anonymous Authentication'.");
        response.addValidationError(msg);
      }

      if (StringUtils.isEmpty(connectionInfo.getSystemPassword())) {
        ValidationMessage msg = new ValidationMessage(
            "systemPassword",
            "Password cannot be empty unless the 'Authorization Scheme' is 'Anonymous Authentication'.");
        response.addValidationError(msg);
      }
    }

    return response;
  }

  private ValidationResponse validateUserAndGroupAuthConfiguration(ValidationContext ctx,
                                                                   CUserAndGroupAuthConfiguration userAndGroupAuthConf)
  {
    ValidationResponse response = new ValidationResponse();

    if (StringUtils.isEmpty(userAndGroupAuthConf.getUserIdAttribute())) {
      ValidationMessage msg = new ValidationMessage("userIdAttribute", "User ID Attribute cannot be empty.");
      response.addValidationError(msg);
    }
    if (StringUtils.isEmpty(userAndGroupAuthConf.getUserObjectClass())) {
      ValidationMessage msg = new ValidationMessage("userObjectClass", "User Object Class cannot be empty.");
      response.addValidationError(msg);
    }
    if (StringUtils.isEmpty(userAndGroupAuthConf.getUserRealNameAttribute())) {
      ValidationMessage msg = new ValidationMessage(
          "userRealNameAttribute",
          "User Real Name Attribute cannot be empty.");
      response.addValidationError(msg);
    }
    if (StringUtils.isEmpty(userAndGroupAuthConf.getEmailAddressAttribute())) {
      ValidationMessage msg = new ValidationMessage(
          "emailAddressAttribute",
          "Email Address Attribute cannot be empty.");
      response.addValidationError(msg);
    }

    if (userAndGroupAuthConf.isLdapGroupsAsRoles()
        && StringUtils.isEmpty(userAndGroupAuthConf.getUserMemberOfAttribute())) {
      if (StringUtils.isEmpty(userAndGroupAuthConf.getGroupIdAttribute())) {
        ValidationMessage msg = new ValidationMessage(
            "groupIdAttribute",
            "Group ID Attribute cannot be empty when Use LDAP Groups as Roles is true.");
        response.addValidationError(msg);
      }
      if (StringUtils.isEmpty(userAndGroupAuthConf.getGroupMemberAttribute())) {
        ValidationMessage msg = new ValidationMessage(
            "groupMemberAttribute",
            "Group Member Attribute cannot be empty when Use LDAP Groups as Roles is true.");
        response.addValidationError(msg);
      }
      if (StringUtils.isEmpty(userAndGroupAuthConf.getGroupMemberFormat())) {
        ValidationMessage msg = new ValidationMessage(
            "groupMemberFormat",
            "Group Member Format cannot be empty when Use LDAP Groups as Roles is true.");
        response.addValidationError(msg);
      }
      if (StringUtils.isEmpty(userAndGroupAuthConf.getGroupObjectClass())) {
        ValidationMessage msg = new ValidationMessage(
            "groupObjectClass",
            "Group Object Class cannot be empty when Use LDAP Groups as Roles is true.");
        response.addValidationError(msg);
      }
    }
    return response;
  }

  public ValidationResponse validateLdapServerOrder(List<CLdapServerConfiguration> ldapServers,
                                                    List<String> proposedOrder)
  {
    ValidationResponse response = new ValidationResponse();

    // make sure all the Ids of the servers are the same
    List<String> actualServerIds = new ArrayList<String>();
    for (CLdapServerConfiguration ldapServerConfig : ldapServers) {
      actualServerIds.add(ldapServerConfig.getId());
    }

    if (proposedOrder.size() != actualServerIds.size() || !proposedOrder.containsAll(actualServerIds)) {
      ValidationMessage msg = new ValidationMessage(
          "*",
          "Invalid order, the propsed order contains different elements then the actual order: propsed: " +
              proposedOrder + ", actual: " + actualServerIds);
      response.addValidationError(msg);
    }

    return response;
  }
}
