/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap.internal.persist.entity;

/**
 * LDAP Server user and group mapping configuration.
 *
 * @since 3.0
 */
public final class Mapping
{
  /**
   * Email Address Attribute. The attribute that stores the users email address.
   */
  private String emailAddressAttribute;

  /**
   * Use LDAP groups as roles. True if LDAP groups should be used as roles.
   */
  private boolean ldapGroupsAsRoles = false;

  /**
   * Group Base DN. The base DN that defines Groups.
   */
  private String groupBaseDn;

  /**
   * Group Id Attribute. The ID attribute for the Group.
   */
  private String groupIdAttribute;

  /**
   * Group Member Attribute, An attribute that defines the a user is a member of the group.
   */
  private String groupMemberAttribute;

  /**
   * Group Member Format. The format that the user info is stored in the groupMappingsAttribute. Such as {@code
   * ${username}}, or {@code uid=${username},ou=people,o=yourBiz}. {@code ${username}} will be replaced with the
   * username.
   */
  private String groupMemberFormat;

  /**
   * Group Object Class. The Object class used for groups.
   */
  private String groupObjectClass;

  /**
   * User Password Attribute. The attribute that stores the users password.
   */
  private String userPasswordAttribute;

  /**
   * User Id Attribute. THe attribute of the userId field.
   */
  private String userIdAttribute;

  /**
   * User Object Class. The object class used for users.
   */
  private String userObjectClass;

  /**
   * Filter to limit user search to user with specific attribute.
   */
  private String ldapFilter;

  /**
   * User Base DN. The base DN for the users.
   */
  private String userBaseDn;

  /**
   * User Real Name Attribute. The attribute that defines the users real name.
   */
  private String userRealNameAttribute;

  /**
   * Users are Stored in a subtree of the userBaseDn.
   */
  private boolean userSubtree = false;

  /**
   * Groups are Stored in a subtree of the groupBaseDn.
   */
  private boolean groupSubtree = false;

  /**
   * Groups are generally one of two types in LDAP systems - static or dynamic. A static group maintains its own
   * membership list. A dynamic group records its membership on a user entry. If dynamic groups this should be set to
   * the attribute used to store the group string in the user object.
   */
  private String userMemberOfAttribute;

  public String getEmailAddressAttribute() {
    return emailAddressAttribute;
  }

  public void setEmailAddressAttribute(final String emailAddressAttribute) {
    this.emailAddressAttribute = emailAddressAttribute;
  }

  public boolean isLdapGroupsAsRoles() {
    return ldapGroupsAsRoles;
  }

  public void setLdapGroupsAsRoles(final boolean ldapGroupsAsRoles) {
    this.ldapGroupsAsRoles = ldapGroupsAsRoles;
  }

  public String getGroupBaseDn() {
    return groupBaseDn;
  }

  public void setGroupBaseDn(final String groupBaseDn) {
    this.groupBaseDn = groupBaseDn;
  }

  public String getGroupIdAttribute() {
    return groupIdAttribute;
  }

  public void setGroupIdAttribute(final String groupIdAttribute) {
    this.groupIdAttribute = groupIdAttribute;
  }

  public String getGroupMemberAttribute() {
    return groupMemberAttribute;
  }

  public void setGroupMemberAttribute(final String groupMemberAttribute) {
    this.groupMemberAttribute = groupMemberAttribute;
  }

  public String getGroupMemberFormat() {
    return groupMemberFormat;
  }

  public void setGroupMemberFormat(final String groupMemberFormat) {
    this.groupMemberFormat = groupMemberFormat;
  }

  public String getGroupObjectClass() {
    return groupObjectClass;
  }

  public void setGroupObjectClass(final String groupObjectClass) {
    this.groupObjectClass = groupObjectClass;
  }

  public String getUserPasswordAttribute() {
    return userPasswordAttribute;
  }

  public void setUserPasswordAttribute(final String userPasswordAttribute) {
    this.userPasswordAttribute = userPasswordAttribute;
  }

  public String getUserIdAttribute() {
    return userIdAttribute;
  }

  public void setUserIdAttribute(final String userIdAttribute) {
    this.userIdAttribute = userIdAttribute;
  }

  public String getUserObjectClass() {
    return userObjectClass;
  }

  public void setUserObjectClass(final String userObjectClass) {
    this.userObjectClass = userObjectClass;
  }

  public String getLdapFilter() {
    return ldapFilter;
  }

  public void setLdapFilter(final String ldapFilter) {
    this.ldapFilter = ldapFilter;
  }

  public String getUserBaseDn() {
    return userBaseDn;
  }

  public void setUserBaseDn(final String userBaseDn) {
    this.userBaseDn = userBaseDn;
  }

  public String getUserRealNameAttribute() {
    return userRealNameAttribute;
  }

  public void setUserRealNameAttribute(final String userRealNameAttribute) {
    this.userRealNameAttribute = userRealNameAttribute;
  }

  public boolean isUserSubtree() {
    return userSubtree;
  }

  public void setUserSubtree(final boolean userSubtree) {
    this.userSubtree = userSubtree;
  }

  public boolean isGroupSubtree() {
    return groupSubtree;
  }

  public void setGroupSubtree(final boolean groupSubtree) {
    this.groupSubtree = groupSubtree;
  }

  public String getUserMemberOfAttribute() {
    return userMemberOfAttribute;
  }

  public void setUserMemberOfAttribute(final String userMemberOfAttribute) {
    this.userMemberOfAttribute = userMemberOfAttribute;
  }
}
