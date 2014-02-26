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
package org.sonatype.nexus.analytics.internal;

import java.util.Map;

import org.sonatype.nexus.capability.support.CapabilityConfigurationSupport;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link CollectionCapability} configuration.
 *
 * @since 2.8
 */
public class CollectionCapabilityConfiguration
  extends CapabilityConfigurationSupport
{
  public static final String HOST_ID = "hostId";

  public static final String SALT = "salt";

  private String hostId;

  private String salt;

  public CollectionCapabilityConfiguration(final Map<String, String> properties) {
    checkNotNull(properties);
    this.hostId = properties.get(HOST_ID);
    this.salt = properties.get(SALT);
  }

  public String getHostId() {
    return hostId;
  }

  public String getSalt() {
    return salt;
  }

  public byte[] getSaltBytes() {
    checkState(salt != null);
    return salt.getBytes(Charsets.UTF_8);
  }

  public Map<String,String> asMap() {
    Map<String, String> props = Maps.newHashMap();
    props.put(HOST_ID, hostId);
    props.put(SALT, salt);
    return props;
  }

  @Override
  public String toString() {
    return "CollectionCapabilityConfiguration{" +
        "hostId='" + hostId + '\'' +
        ", salt='" + salt + '\'' +
        '}';
  }
}
