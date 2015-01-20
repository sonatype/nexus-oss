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
package org.sonatype.security.realms.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.configuration.validation.ValidationContext;

public class SecurityValidationContext
    implements ValidationContext
{
  private List<String> existingPrivilegeIds;

  private List<String> existingRoleIds;

  private List<String> existingUserIds;

  private Map<String, List<String>> roleContainmentMap;

  private Map<String, String> existingRoleNameMap;

  public void addExistingPrivilegeIds() {
    if (existingPrivilegeIds == null) {
      existingPrivilegeIds = new ArrayList<>();
    }
  }

  public void addExistingRoleIds() {
    if (existingRoleIds == null) {
      existingRoleIds = new ArrayList<>();
    }

    if (roleContainmentMap == null) {
      roleContainmentMap = new HashMap<>();
    }

    if (existingRoleNameMap == null) {
      existingRoleNameMap = new HashMap<>();
    }
  }

  public void addExistingUserIds() {
    if (existingUserIds == null) {
      existingUserIds = new ArrayList<>();
    }
  }

  public List<String> getExistingPrivilegeIds() {
    return existingPrivilegeIds;
  }

  public List<String> getExistingRoleIds() {
    return existingRoleIds;
  }

  public List<String> getExistingUserIds() {
    return existingUserIds;
  }

  public Map<String, List<String>> getRoleContainmentMap() {
    return roleContainmentMap;
  }

  public Map<String, String> getExistingRoleNameMap() {
    return existingRoleNameMap;
  }
}
