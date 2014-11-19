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
 * A Role is a bucket for privileges, can also contain other roles.
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("all")
public class CRole
    implements java.io.Serializable, java.lang.Cloneable
{

  //--------------------------/
  //- Class/Member Variables -/
  //--------------------------/

  /**
   * The ID used to reference this role.
   */
  private String id;

  /**
   * A descriptive name for the role.
   */
  private String name;

  /**
   * An explanation of the use of the role.
   */
  private String description;

  /**
   * The number of minutes to wait before the session timeouts
   * because of inactivity.
   */
  private int sessionTimeout = 0;

  /**
   * Field privileges.
   */
  private java.util.List<String> privileges;

  /**
   * Field roles.
   */
  private java.util.List<String> roles;

  /**
   * Marks if this role is editable.
   */
  private boolean readOnly = false;


  //-----------/
  //- Methods -/
  //-----------/

  /**
   * Method addPrivilege.
   */
  public void addPrivilege(String string) {
    getPrivileges().add(string);
  } //-- void addPrivilege( String )

  /**
   * Method addRole.
   */
  public void addRole(String string) {
    getRoles().add(string);
  } //-- void addRole( String )

  /**
   * Method clone.
   *
   * @return CRole
   */
  public CRole clone() {
    try {
      CRole copy = (CRole) super.clone();

      if (this.privileges != null) {
        copy.privileges = new java.util.ArrayList<String>();
        copy.privileges.addAll(this.privileges);
      }

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
  } //-- CRole clone()

  /**
   * Get an explanation of the use of the role.
   *
   * @return String
   */
  public String getDescription() {
    return this.description;
  } //-- String getDescription()

  /**
   * Get the ID used to reference this role.
   *
   * @return String
   */
  public String getId() {
    return this.id;
  } //-- String getId()

  /**
   * Get a descriptive name for the role.
   *
   * @return String
   */
  public String getName() {
    return this.name;
  } //-- String getName()

  /**
   * Method getPrivileges.
   *
   * @return List
   */
  public java.util.List<String> getPrivileges() {
    if (this.privileges == null) {
      this.privileges = new java.util.ArrayList<String>();
    }

    return this.privileges;
  } //-- java.util.List<String> getPrivileges()

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
   * Get the number of minutes to wait before the session
   * timeouts because of inactivity.
   *
   * @return int
   */
  public int getSessionTimeout() {
    return this.sessionTimeout;
  } //-- int getSessionTimeout()

  /**
   * Get marks if this role is editable.
   *
   * @return boolean
   */
  public boolean isReadOnly() {
    return this.readOnly;
  } //-- boolean isReadOnly()

  /**
   * Method removePrivilege.
   */
  public void removePrivilege(String string) {
    getPrivileges().remove(string);
  } //-- void removePrivilege( String )

  /**
   * Method removeRole.
   */
  public void removeRole(String string) {
    getRoles().remove(string);
  } //-- void removeRole( String )

  /**
   * Set an explanation of the use of the role.
   */
  public void setDescription(String description) {
    this.description = description;
  } //-- void setDescription( String )

  /**
   * Set the ID used to reference this role.
   */
  public void setId(String id) {
    this.id = id;
  } //-- void setId( String )

  /**
   * Set a descriptive name for the role.
   */
  public void setName(String name) {
    this.name = name;
  } //-- void setName( String )

  /**
   * Set list of privilege ids that will define this role.
   */
  public void setPrivileges(java.util.List<String> privileges) {
    this.privileges = privileges;
  } //-- void setPrivileges( java.util.List )

  /**
   * Set marks if this role is editable.
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  } //-- void setReadOnly( boolean )

  /**
   * Set list of role ids, whose contents, will also define this
   * role.
   */
  public void setRoles(java.util.List<String> roles) {
    this.roles = roles;
  } //-- void setRoles( java.util.List )

  /**
   * Set the number of minutes to wait before the session
   * timeouts because of inactivity.
   */
  public void setSessionTimeout(int sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  } //-- void setSessionTimeout( int )

}
