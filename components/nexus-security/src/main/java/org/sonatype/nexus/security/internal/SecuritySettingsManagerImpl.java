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
package org.sonatype.nexus.security.internal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.validation.ValidationResponse;
import org.sonatype.nexus.common.validation.ValidationResponseException;
import org.sonatype.nexus.security.settings.SecuritySettings;
import org.sonatype.nexus.security.settings.SecuritySettingsManager;
import org.sonatype.nexus.security.settings.SecuritySettingsSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * Default {@link SecuritySettingsManager} implementation.
 */
@Named
@Singleton
public class SecuritySettingsManagerImpl
    extends ComponentSupport
    implements SecuritySettingsManager
{
  private final SecuritySettingsSource source;

  private SecuritySettings model = null;

  private final ReentrantLock lock = new ReentrantLock();

  @Inject
  public SecuritySettingsManagerImpl(final SecuritySettingsSource source) {
    this.source = source;
  }

  private SecuritySettings getModel() {
    // FIXME this has race-condition potential
    if (model != null) {
      return model;
    }

    lock.lock();
    try {
      source.load();
      model = source.get();
    }
    finally {
      lock.unlock();
    }

    return model;
  }

  @Override
  public boolean isAnonymousAccessEnabled() {
    return getModel().isAnonymousAccessEnabled();
  }

  @Override
  public void setAnonymousAccessEnabled(final boolean enabled) {
    getModel().setAnonymousAccessEnabled(enabled);
  }

  @Override
  public String getAnonymousPassword() {
    return getModel().getAnonymousPassword();
  }

  @Override
  public void setAnonymousPassword(final String password) {
    getModel().setAnonymousPassword(password);
  }

  @Override
  public String getAnonymousUsername() {
    return getModel().getAnonymousUsername();
  }

  @Override
  public void setAnonymousUsername(final String username) {
    getModel().setAnonymousUsername(username);
  }

  @Override
  public List<String> getRealms() {
    return Collections.unmodifiableList(getModel().getRealms());
  }

  @Override
  public void setRealms(final List<String> realms) {
    ValidationResponse response = new ValidationResponse();
    if (realms.isEmpty()) {
      response.addError("At least one realm must be configured");
      throw new ValidationResponseException(response);
    }

    getModel().setRealms(realms);
  }

  @Override
  public void clearCache() {
    lock.lock();
    try {
      model = null;
    }
    finally {
      lock.unlock();
    }
  }

  @Override
  public void save() {
    lock.lock();
    try {
      source.save();
    }
    finally {
      lock.unlock();
    }
  }
}
