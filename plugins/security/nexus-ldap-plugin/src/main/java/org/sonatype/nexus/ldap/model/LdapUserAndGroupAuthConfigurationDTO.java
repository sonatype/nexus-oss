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
package org.sonatype.nexus.ldap.model;

import javax.xml.bind.annotation.XmlType;

/**
 * The user and group mapping configuration mapping.
 */
@XmlType(name = "ldapUserAndGroupAuthConfiguration")
public class LdapUserAndGroupAuthConfigurationDTO
    implements java.io.Serializable
{

  /**
   * Email Address Attribute.  The attribute that stores the
   * users email address.
   */
  private String emailAddressAttribute;

  /**
   * Use LDAP groups as roles.  True if LDAP groups should be
   * used as roles.
   */
  private boolean ldapGroupsAsRoles = false;

  /**
   * Group Base DN.  The base DN that defines Groups.
   */
  private String groupBaseDn;

  /**
   * Group Id Attribute. The ID attribute for the Group.
   */
  private String groupIdAttribute;

  /**
   * Group Member Attribute,  An attribute that defines the a
   * user is a member of the group.
   */
  private String groupMemberAttribute;

  /**
   * Group Member Format. The format that the user info is stored
   * in the groupMappingsAttribute.  Such as ${username}, or
   * uid=${username},ou=people,o=yourBiz.  ${username} will be
   * replaced with the username.
   */
  private String groupMemberFormat;

  /**
   * Group Object Class. The Object class used for groups.
   */
  private String groupObjectClass;

  /**
   * User Password Attribute.  The attribute that stores the
   * users password.
   */
  private String userPasswordAttribute;

  /**
   * User Id Attribute.  THe attribute of the userId field.
   */
  private String userIdAttribute;

  /**
   * User Object Class.  The object class used for users.
   */
  private String userObjectClass;

  /**
   * User Base DN. The base DN for the users.
   */
  private String userBaseDn;

  /**
   * User Real Name Attribute.  The attribute that defines the
   * users real name.
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
   * Groups are generally one of two types in LDAP systems -
   * static or dynamic. A static group maintains its own
   * membership list. A dynamic group records its membership on a
   * user entry. If dynamic groups this should be set to the
   * attribute used to store the group string in the user object.
   */
  private String userMemberOfAttribute;


  /**
   * This filter expression is added to the user search.
   * <p/>
   * Example: {@code (&(mail=*@domain.com)(uid=user_*)}
   */
  private String ldapFilter;

  /**
   * Get email Address Attribute.  The attribute that stores the
   * users email address.
   */
  public String getEmailAddressAttribute() {
    return this.emailAddressAttribute;
  }

  /**
   * Get group Base DN.  The base DN that defines Groups.
   */
  public String getGroupBaseDn() {
    return this.groupBaseDn;
  }

  /**
   * Get group Id Attribute. The ID attribute for the Group.
   */
  public String getGroupIdAttribute() {
    return this.groupIdAttribute;
  }

  /**
   * Get group Member Attribute,  An attribute that defines the a
   * user is a member of the group.
   */
  public String getGroupMemberAttribute() {
    return this.groupMemberAttribute;
  }

  /**
   * Get group Member Format. The format that the user info is
   * stored in the groupMappingsAttribute.  Such as ${username},
   * or uid=${username},ou=people,o=yourBiz.  ${username} will be
   * replaced with the username.
   */
  public String getGroupMemberFormat() {
    return this.groupMemberFormat;
  }

  /**
   * Get group Object Class. The Object class used for groups.
   */
  public String getGroupObjectClass() {
    return this.groupObjectClass;
  }

  /**
   * Get user Base DN. The base DN for the users.
   */
  public String getUserBaseDn() {
    return this.userBaseDn;
  }

  /**
   * Get user Id Attribute.  THe attribute of the userId field.
   */
  public String getUserIdAttribute() {
    return this.userIdAttribute;
  }

  /**
   * Get groups are generally one of two types in LDAP systems -
   * static or dynamic. A static group maintains its own
   * membership list. A dynamic group records its membership on a
   * user entry. If dynamic groups this should be set to the
   * attribute used to store the group string in the user object.
   */
  public String getUserMemberOfAttribute() {
    return this.userMemberOfAttribute;
  }

  /**
   * Get user Object Class.  The object class used for users.
   */
  public String getUserObjectClass() {
    return this.userObjectClass;
  }

  /**
   * Get user Password Attribute.  The attribute that stores the
   * users password.
   */
  public String getUserPasswordAttribute() {
    return this.userPasswordAttribute;
  }

  /**
   * Get user Real Name Attribute.  The attribute that defines
   * the users real name.
   */
  public String getUserRealNameAttribute() {
    return this.userRealNameAttribute;
  }

  /**
   * Get groups are Stored in a subtree of the groupBaseDn.
   */
  public boolean isGroupSubtree() {
    return this.groupSubtree;
  }

  /**
   * Get use LDAP groups as roles.  True if LDAP groups should be
   * used as roles.
   */
  public boolean isLdapGroupsAsRoles() {
    return this.ldapGroupsAsRoles;
  }

  /**
   * Get users are Stored in a subtree of the userBaseDn.
   */
  public boolean isUserSubtree() {
    return this.userSubtree;
  }

  /**
   * Set email Address Attribute.  The attribute that stores the
   * users email address.
   */
  public void setEmailAddressAttribute(String emailAddressAttribute) {
    this.emailAddressAttribute = emailAddressAttribute;
  }

  /**
   * Set group Base DN.  The base DN that defines Groups.
   */
  public void setGroupBaseDn(String groupBaseDn) {
    this.groupBaseDn = groupBaseDn;
  }

  /**
   * Set group Id Attribute. The ID attribute for the Group.
   */
  public void setGroupIdAttribute(String groupIdAttribute) {
    this.groupIdAttribute = groupIdAttribute;
  }

  /**
   * Set group Member Attribute,  An attribute that defines the a
   * user is a member of the group.
   */
  public void setGroupMemberAttribute(String groupMemberAttribute) {
    this.groupMemberAttribute = groupMemberAttribute;
  }

  /**
   * Set group Member Format. The format that the user info is
   * stored in the groupMappingsAttribute.  Such as ${username},
   * or uid=${username},ou=people,o=yourBiz.  ${username} will be
   * replaced with the username.
   */
  public void setGroupMemberFormat(String groupMemberFormat) {
    this.groupMemberFormat = groupMemberFormat;
  }

  /**
   * Set group Object Class. The Object class used for groups.
   */
  public void setGroupObjectClass(String groupObjectClass) {
    this.groupObjectClass = groupObjectClass;
  }

  /**
   * Set groups are Stored in a subtree of the groupBaseDn.
   */
  public void setGroupSubtree(boolean groupSubtree) {
    this.groupSubtree = groupSubtree;
  }

  /**
   * Set use LDAP groups as roles.  True if LDAP groups should be
   * used as roles.
   */
  public void setLdapGroupsAsRoles(boolean ldapGroupsAsRoles) {
    this.ldapGroupsAsRoles = ldapGroupsAsRoles;
  }


  /**
   * Set user Base DN. The base DN for the users.
   */
  public void setUserBaseDn(String userBaseDn) {
    this.userBaseDn = userBaseDn;
  }

  /**
   * Set user Id Attribute.  THe attribute of the userId field.
   */
  public void setUserIdAttribute(String userIdAttribute) {
    this.userIdAttribute = userIdAttribute;
  }

  /**
   * Set groups are generally one of two types in LDAP systems -
   * static or dynamic. A static group maintains its own
   * membership list. A dynamic group records its membership on a
   * user entry. If dynamic groups this should be set to the
   * attribute used to store the group string in the user object.
   */
  public void setUserMemberOfAttribute(String userMemberOfAttribute) {
    this.userMemberOfAttribute = userMemberOfAttribute;
  }

  /**
   * Set user Object Class.  The object class used for users.
   */
  public void setUserObjectClass(String userObjectClass) {
    this.userObjectClass = userObjectClass;
  }

  /**
   * Set user Password Attribute.  The attribute that stores the
   * users password.
   */
  public void setUserPasswordAttribute(String userPasswordAttribute) {
    this.userPasswordAttribute = userPasswordAttribute;
  }

  /**
   * Set user Real Name Attribute.  The attribute that defines
   * the users real name.
   */
  public void setUserRealNameAttribute(String userRealNameAttribute) {
    this.userRealNameAttribute = userRealNameAttribute;
  }

  /**
   * Set users are Stored in a subtree of the userBaseDn.
   */
  public void setUserSubtree(boolean userSubtree) {
    this.userSubtree = userSubtree;
  }


  /**
   * Set the filter expression to be added to the user search.
   * <p/>
   * Example: {@code (&(mail=*@domain.com)(uid=user_*)}
   */
  public void setLdapFilter(final String ldapFilter) {
    this.ldapFilter = ldapFilter;
  }

  /**
   * Return the filter expression added to the user search.
   * <p/>
   * Example: {@code (&(mail=*@domain.com)(uid=user_*)}
   */
  public String getLdapFilter() {
    return ldapFilter;
  }
}
