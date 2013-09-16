/*
 * Copyright (c) 2008-2013 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.capability.support;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.inject.EagerSingleton;
import org.sonatype.nexus.plugin.PluginIdentity;

/**
 * Capabilities plugin.
 *
 * @since 2.7
 */
@Named
@EagerSingleton
public class CapabilitiesPlugin
    extends PluginIdentity
{

  /**
   * Prefix for ID-like things.
   */
  public static final String ID_PREFIX = "capabilities";

  /**
   * Expected groupId for plugin artifact.
   */
  public static final String GROUP_ID = "com.sonatype.nexus.plugins";

  /**
   * Expected artifactId for plugin artifact.
   */
  public static final String ARTIFACT_ID = "nexus-" + ID_PREFIX + "-plugin";

  /**
   * Prefix for @Named configuration.
   */
  public static final String CONFIG_PREFIX = "${" + ID_PREFIX;

  /**
   * Prefix for REST resources
   */
  public static final String REST_PREFIX = "/" + ID_PREFIX;

  /**
   * Prefix for permissions.
   */
  public static final String PERMISSION_PREFIX = "nexus:" + ID_PREFIX + ":";

  /**
   * Prefix for permissions.
   */
  public static final String PERMISSION_PREFIX_TYPES = "nexus:capabilityTypes";

  @Inject
  public CapabilitiesPlugin()
      throws Exception
  {
    super(GROUP_ID, ARTIFACT_ID);
  }

}