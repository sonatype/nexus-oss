/*
 * Copyright (c) 2008-2013 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugin;

import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link StaticSecurityResource} implementations.
 *
 * @since 2.7
 */
public class StaticSecurityResourceSupport
    extends AbstractStaticSecurityResource
{
  private final PluginIdentity owner;

  public StaticSecurityResourceSupport(final PluginIdentity plugin) {
    this.owner = checkNotNull(plugin);
  }

  public String getResourcePath() {
    return String.format("/META-INF/%s-security.xml", owner.getId()); //NON-NLS
  }
}
