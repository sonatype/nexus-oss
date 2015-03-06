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
package org.sonatype.nexus.repository.maven.internal.policy;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Repository redeploy policy for Maven artifacts, is artifact overwrite allowed or not.
 *
 * @since 3.0
 */
public class RedeployPolicy
{
  private static final String V_ALLOW = "allow";

  private static final String V_DISALLOW = "disallow";

  public static final RedeployPolicy ALLOW = new RedeployPolicy(V_ALLOW);

  public static final RedeployPolicy DISALLOW = new RedeployPolicy(V_DISALLOW);

  public static RedeployPolicy valueOf(final String value) {
    checkNotNull(value);
    switch (value.toLowerCase(Locale.US)) {
      case V_ALLOW: {
        return ALLOW;
      }
      case V_DISALLOW: {
        return DISALLOW;
      }
      default:
        throw new IllegalArgumentException("Unknown deploy policy: " + value);
    }
  }

  private final String value;

  private RedeployPolicy(final String value) {
    this.value = checkNotNull(value);
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RedeployPolicy policy = (RedeployPolicy) o;
    if (!value.equals(policy.value)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }
}
