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

package org.sonatype.nexus.repository.simple.internal;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.security.CRoleBuilder;
import org.sonatype.nexus.repository.security.MutableDynamicSecurityResource.Mutator;
import org.sonatype.security.model.SecurityModelConfiguration;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.security.RepositoryInstancePrivilegeDescriptor.id;
import static org.sonatype.nexus.repository.security.RepositoryInstancePrivilegeDescriptor.privilege;

/**
 * Simple security facet.
 *
 * @since 3.0
 */
@Named
@Facet.Exposed
public class SimpleSecurityFacet
    extends FacetSupport
{
  private final SimpleDynamicSecurityResource dynamicSecurityResource;

  @Inject
  public SimpleSecurityFacet(final SimpleDynamicSecurityResource dynamicSecurityResource) {
    this.dynamicSecurityResource = checkNotNull(dynamicSecurityResource);
  }

  @Override
  protected void doStart() throws Exception {
    final String repositoryName = getRepository().getName();

    dynamicSecurityResource.apply(new Mutator()
    {
      @Override
      public void apply(final SecurityModelConfiguration model) {
        // add repository-instance privileges
        model.addPrivilege(privilege(repositoryName, "browse"));
        model.addPrivilege(privilege(repositoryName, "read"));
        model.addPrivilege(privilege(repositoryName, "edit"));
        model.addPrivilege(privilege(repositoryName, "add"));
        model.addPrivilege(privilege(repositoryName, "delete"));

        // add repository-instance 'admin' role
        model.addRole(new CRoleBuilder()
            .id(String.format("repository-instance-%s-admin", repositoryName))
            .privilege(id(repositoryName, "browse"))
            .privilege(id(repositoryName, "read"))
            .privilege(id(repositoryName, "edit"))
            .privilege(id(repositoryName, "add"))
            .privilege(id(repositoryName, "delete"))
            .create());

        // add repository-instance 'readonly' role
        model.addRole(new CRoleBuilder()
            .id(String.format("repository-instance-%s-readonly", repositoryName))
            .privilege(id(repositoryName, "browse"))
            .privilege(id(repositoryName, "read"))
            .create());

        // add repository-instance 'deployer' role
        model.addRole(new CRoleBuilder()
            .id(String.format("repository-instance-%s-deployer", repositoryName))
            .privilege(id(repositoryName, "browse"))
            .privilege(id(repositoryName, "read"))
            .privilege(id(repositoryName, "edit"))
            .privilege(id(repositoryName, "add"))
            .create());
      }
    });
  }

  @Override
  protected void doStop() throws Exception {
    final String repositoryName = getRepository().getName();

    dynamicSecurityResource.apply(new Mutator()
    {
      @Override
      public void apply(final SecurityModelConfiguration model) {
        // remove repository-instance privileges
        model.removePrivilege(id(repositoryName, "browse"));
        model.removePrivilege(id(repositoryName, "read"));
        model.removePrivilege(id(repositoryName, "edit"));
        model.removePrivilege(id(repositoryName, "add"));
        model.removePrivilege(id(repositoryName, "delete"));

        // remove repository-instance roles
        model.removeRole(String.format("repository-instance-%s-admin", repositoryName));
        model.removeRole(String.format("repository-instance-%s-readonly", repositoryName));
        model.removeRole(String.format("repository-instance-%s-deployer", repositoryName));
      }
    });
  }
}
