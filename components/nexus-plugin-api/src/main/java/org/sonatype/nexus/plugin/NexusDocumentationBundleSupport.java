/*
 * Copyright (c) 2008-2013 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugin;

import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusDocumentationBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link NexusDocumentationBundle} implementations.
 *
 * @since 2.7
 */
public abstract class NexusDocumentationBundleSupport
    extends AbstractDocumentationNexusResourceBundle
{
  private final PluginIdentity owner;

  protected NexusDocumentationBundleSupport(final PluginIdentity plugin) {
    this.owner = checkNotNull(plugin);
  }

  @Override
  public String getPluginId() {
    return owner.getId();
  }

}
