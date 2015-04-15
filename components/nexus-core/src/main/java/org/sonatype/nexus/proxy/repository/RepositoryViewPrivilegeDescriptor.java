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
package org.sonatype.nexus.proxy.repository;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.nexus.security.config.CPrivilege;
import org.sonatype.nexus.security.config.SecurityConfigurationValidationContext;
import org.sonatype.nexus.security.privilege.AbstractPrivilegeDescriptor;
import org.sonatype.nexus.security.privilege.PrivilegeDescriptor;
import org.sonatype.nexus.validation.ValidationResponse;

import org.codehaus.plexus.util.StringUtils;

@Singleton
@Named("RepositoryViewPrivilegeDescriptor")
public class RepositoryViewPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
  public static final String TYPE = "repository";

  public static final String P_REPOSITORY_ID = "repositoryId";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  protected String buildPermission(CPrivilege privilege) {
    if (!TYPE.equals(privilege.getType())) {
      return null;
    }

    String repoId = privilege.getProperty(P_REPOSITORY_ID);

    if (StringUtils.isEmpty(repoId)) {
      repoId = "*";
    }

    return "nexus:view:" + NexusItemAuthorizer.VIEW_REPOSITORY_KEY + ":" + repoId;
  }

  @Override
  public ValidationResponse validatePrivilege(CPrivilege privilege, SecurityConfigurationValidationContext ctx, boolean update) {
    ValidationResponse response = super.validatePrivilege(privilege, ctx, update);

    if (!TYPE.equals(privilege.getType())) {
      return response;
    }

    return response;
  }
}
