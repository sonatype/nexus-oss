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
package org.sonatype.nexus.security.authz;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.sonatype.nexus.security.internal.AuthorizingRealmImpl;
import org.sonatype.nexus.security.settings.SecuritySettingsManager;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Binds special anonymous subject if current subject is guest and anonymous access is enabled.
 *
 * @since 3.0
 */
public class AnonymousFilter
    extends AdviceFilter
{
  public static final String NAME = "nx-anonymous";

  private static final String ORIGINAL_SUBJECT = AnonymousFilter.class.getName() + ".originalSubject";

  private static final Logger log = LoggerFactory.getLogger(AnonymousFilter.class);

  private final SecuritySettingsManager settingsManager;

  @Inject
  public AnonymousFilter(final SecuritySettingsManager settingsManager) {
    this.settingsManager = checkNotNull(settingsManager);
  }

  @Override
  protected boolean preHandle(final ServletRequest request, final ServletResponse response) throws Exception {
    Subject subject = SecurityUtils.getSubject();

    if (subject.getPrincipal() == null && settingsManager.isAnonymousAccessEnabled()) {
      request.setAttribute(ORIGINAL_SUBJECT, subject);

      String principal = settingsManager.getAnonymousUsername();
      // FIXME: Expose realm name in settings
      String realmName = AuthorizingRealmImpl.NAME;
      log.trace("Binding anonymous subject: principal={}, realmName={}", principal, realmName);

      subject = new Subject.Builder()
          .principals(new SimplePrincipalCollection(principal, realmName))
          .authenticated(false)
          .buildSubject();

      // TODO: Sort out if we need sessionCreationEnabled(false), this presently causes the UI to freak-out

      ThreadContext.bind(subject);
      log.trace("Bound anonymous subject: {}", subject);
    }

    return true;
  }

  @Override
  public void afterCompletion(final ServletRequest request, final ServletResponse response, final Exception exception)
      throws Exception
  {
    Subject subject = (Subject) request.getAttribute(ORIGINAL_SUBJECT);
    if (subject != null) {
      log.trace("Binding original subject: {}", subject);
      ThreadContext.bind(subject);
    }

    // TODO: Sort out if this is needed here or not
    //else {
    //  log.trace("Unbinding subject");
    //  ThreadContext.unbindSubject();
    //}
  }
}
