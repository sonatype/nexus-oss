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
package org.sonatype.security.model;

/**
 * Defines the mapping between users and roles, if LDAP groups are
 * not used.
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("all")
public class CUserRoleMapping
    implements java.io.Serializable, java.lang.Cloneable
{

  //--------------------------/
  //- Class/Member Variables -/
  //--------------------------/

  /**
   * User ID.  The id of the user.
   */
  private String userId;

  /**
   * The source or realm id, of the user.  For example an CROWD
   * realm might have a source id of CROWD.
   */
  private String source;

  /**
   * Field roles.
   */
  private java.util.List<String> roles;


  //-----------/
  //- Methods -/
  //-----------/

  /**
   * Method addRole.
   */
  public void addRole(String string) {
    getRoles().add(string);
  } //-- void addRole( String )

  /**
   * Method clone.
   *
   * @return CUserRoleMapping
   */
  public CUserRoleMapping clone() {
    try {
      CUserRoleMapping copy = (CUserRoleMapping) super.clone();

      if (this.roles != null) {
        copy.roles = new java.util.ArrayList<String>();
        copy.roles.addAll(this.roles);
      }

      return copy;
    }
    catch (java.lang.Exception ex) {
      throw (java.lang.RuntimeException) new java.lang.UnsupportedOperationException(getClass().getName()
          + " does not support clone()").initCause(ex);
    }
  } //-- CUserRoleMapping clone()

  /**
   * Method getRoles.
   *
   * @return List
   */
  public java.util.List<String> getRoles() {
    if (this.roles == null) {
      this.roles = new java.util.ArrayList<String>();
    }

    return this.roles;
  } //-- java.util.List<String> getRoles()

  /**
   * Get the source or realm id, of the user.  For example an
   * CROWD realm might have a source id of CROWD.
   *
   * @return String
   */
  public String getSource() {
    return this.source;
  } //-- String getSource()

  /**
   * Get user ID.  The id of the user.
   *
   * @return String
   */
  public String getUserId() {
    return this.userId;
  } //-- String getUserId()

  /**
   * Method removeRole.
   */
  public void removeRole(String string) {
    getRoles().remove(string);
  } //-- void removeRole( String )

  /**
   * Set list of the users roles.
   */
  public void setRoles(java.util.List<String> roles) {
    this.roles = roles;
  } //-- void setRoles( java.util.List )

  /**
   * Set the source or realm id, of the user.  For example an
   * CROWD realm might have a source id of CROWD.
   */
  public void setSource(String source) {
    this.source = source;
  } //-- void setSource( String )

  /**
   * Set user ID.  The id of the user.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  } //-- void setUserId( String )

}
