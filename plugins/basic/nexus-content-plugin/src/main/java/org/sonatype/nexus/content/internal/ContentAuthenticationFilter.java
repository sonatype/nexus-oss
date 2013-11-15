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

package org.sonatype.nexus.content.internal;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.sonatype.nexus.content.ContentRestrictionConstituent;
import org.sonatype.nexus.security.filter.authc.NexusHttpAuthenticationFilter;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;

/**
 * Nexus {code}/content{code} {@link AuthenticationFilter}.
 *
 * @see ContentRestrictionConstituent
 * @see ContentRestrictedToken
 * @since 2.1
 */
public class ContentAuthenticationFilter
    extends NexusHttpAuthenticationFilter
{
  private final List<ContentRestrictionConstituent> constituents;

  @Inject
  public ContentAuthenticationFilter(final @Nullable List<ContentRestrictionConstituent> constituents) {
    this.constituents = constituents;
    setApplicationName("Sonatype Nexus Repository Manager");
  }

  /**
   * Determine if content restriction is enabled, by asking each constituent.
   * If any constituent reports a restriction then returns true.
   */
  private boolean isRestricted(final ServletRequest request) {
    if (constituents != null) {
      for (ContentRestrictionConstituent constituent : constituents) {
        if (constituent.isContentRestricted(request)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
    if (isRestricted(request)) {
      getLogger().debug("Content authentication for request is restricted");

      // We know our super-class makes UsernamePasswordTokens, ask super to pull out the relevant details
      UsernamePasswordToken basis = (UsernamePasswordToken) super.createToken(request, response);

      // And include more information than is normally provided to a token (ie. the request)
      return new ContentRestrictedToken(basis, request);
    }
    else {
      return super.createToken(request, response);
    }
  }
}
