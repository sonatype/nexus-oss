/*
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

package org.sonatype.nexus.security;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.realms.privileges.AbstractPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.security.realms.validator.SecurityValidationContext;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

@Component(role = PrivilegeDescriptor.class, hint = "RepositoryViewPrivilegeDescriptor")
public class RepositoryViewPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
  public static final String TYPE = "repository";

  @Requirement(role = PrivilegePropertyDescriptor.class, hint = "RepositoryPropertyDescriptor")
  private PrivilegePropertyDescriptor repoProperty;

  public String getName() {
    return "Repository View";
  }

  public List<PrivilegePropertyDescriptor> getPropertyDescriptors() {
    List<PrivilegePropertyDescriptor> propertyDescriptors = new ArrayList<PrivilegePropertyDescriptor>();

    propertyDescriptors.add(repoProperty);

    return propertyDescriptors;
  }

  public String getType() {
    return TYPE;
  }

  public String buildPermission(CPrivilege privilege) {
    if (!TYPE.equals(privilege.getType())) {
      return null;
    }

    String repoId = getProperty(privilege, RepositoryPropertyDescriptor.ID);

    if (StringUtils.isEmpty(repoId)) {
      repoId = "*";
    }

    return buildPermission(NexusItemAuthorizer.VIEW_REPOSITORY_KEY, repoId);
  }

  @Override
  public ValidationResponse validatePrivilege(CPrivilege privilege,
                                              SecurityValidationContext ctx, boolean update)
  {
    ValidationResponse response = super.validatePrivilege(privilege, ctx, update);

    if (!TYPE.equals(privilege.getType())) {
      return response;
    }

    return response;
  }

  public static String buildPermission(String objectType, String objectId) {
    return "nexus:view:" + objectType + ":" + objectId;
  }
}
