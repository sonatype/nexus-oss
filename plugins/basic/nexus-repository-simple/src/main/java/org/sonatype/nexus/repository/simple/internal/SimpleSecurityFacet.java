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
import org.sonatype.nexus.repository.simple.internal.DynamicSecurityResourceImpl.Customizer;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.SecurityModelConfiguration;

import static com.google.common.base.Preconditions.checkNotNull;

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
  private final DynamicSecurityResourceImpl dynamicSecurityResource;

  @Inject
  public SimpleSecurityFacet(final DynamicSecurityResourceImpl dynamicSecurityResource) {
    this.dynamicSecurityResource = checkNotNull(dynamicSecurityResource);
  }

  private String prefix() {
    return "nexus:repository-instance:" + getRepository().getName();
  }

  private String name(final String prefix, final String method) {
    return prefix + ":" + method;
  }

  private String id(final String prefix, final String method) {
    return name(prefix, method).replaceAll(":", "-");
  }

  @Override
  protected void doStart() throws Exception {
    final String prefix = prefix();

    log.trace("Adding privileges for: {}", prefix);
    dynamicSecurityResource.apply(new Customizer()
    {
      @Override
      public void apply(final SecurityModelConfiguration model) {
        model.addPrivilege(privilege(prefix, "browse"));
        model.addPrivilege(privilege(prefix, "read"));
        model.addPrivilege(privilege(prefix, "edit"));
        model.addPrivilege(privilege(prefix, "add"));
        model.addPrivilege(privilege(prefix, "delete"));
      }
    });
  }

  private CPrivilege privilege(final String prefix, final String method) {
    String name = name(prefix, method);
    CPrivilege privilege = new CPrivilege();
    privilege.setType("method");
    privilege.setId(id(prefix, method));
    privilege.setName(name);
    privilege.setDescription(name);
    privilege.setProperty("method", method);
    privilege.setProperty("permission", prefix);
    return privilege;
  }

  @Override
  protected void doStop() throws Exception {
    final String prefix = prefix();

    log.trace("Removing privileges for: {}", prefix);
    dynamicSecurityResource.apply(new Customizer()
    {
      @Override
      public void apply(final SecurityModelConfiguration model) {
        model.removePrivilege(id(prefix, "browse"));
        model.removePrivilege(id(prefix, "read"));
        model.removePrivilege(id(prefix, "edit"));
        model.removePrivilege(id(prefix, "add"));
        model.removePrivilege(id(prefix, "delete"));
      }
    });
  }
}
