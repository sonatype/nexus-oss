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

package org.sonatype.nexus.configuration.model;

import java.util.List;

import org.sonatype.nexus.configuration.PasswordHelper;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

import com.thoughtworks.xstream.XStream;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

@Component(role = ConfigurationHelper.class)
public class DefaultConfigurationHelper
    extends AbstractLoggingComponent
    implements ConfigurationHelper
{
  @Requirement
  private PasswordHelper passwordHelper;

  private static final String PASSWORD_MASK = "*****";

  /**
   * XStream is used for a deep clone (TODO: not sure if this is a great idea)
   */
  private static XStream xstream = new XStream();

  public Configuration encryptDecryptPasswords(final Configuration config, final boolean encrypt) {
    if (null == config) {
      return null;
    }

    final Configuration copy = clone(config);

    handlePasswords(copy, encrypt, false);

    return copy;
  }

  public Configuration maskPasswords(final Configuration config) {
    if (null == config) {
      return null;
    }

    final Configuration copy = clone(config);

    handlePasswords(copy, false, true);

    return copy;
  }

  protected Configuration clone(final Configuration config) {
    // use Xstream
    return (Configuration) xstream.fromXML(xstream.toXML(config));
  }

  protected void handlePasswords(final Configuration config, final boolean encrypt, final boolean mask) {
    if (config.getErrorReporting() != null
        && StringUtils.isNotEmpty(config.getErrorReporting().getJiraPassword())) {
      CErrorReporting errorConfig = config.getErrorReporting();
      errorConfig.setJiraPassword(encryptDecryptPassword(errorConfig.getJiraPassword(), encrypt, mask));
    }

    if (config.getSmtpConfiguration() != null
        && StringUtils.isNotEmpty(config.getSmtpConfiguration().getPassword())) {
      CSmtpConfiguration smtpConfig = config.getSmtpConfiguration();
      smtpConfig.setPassword(encryptDecryptPassword(smtpConfig.getPassword(), encrypt, mask));
    }

    // global proxy
    final CRemoteProxySettings rps = config.getRemoteProxySettings();
    if (rps != null) {
      if (rps.getHttpProxySettings() != null
          && rps.getHttpProxySettings().getAuthentication() != null
          && StringUtils.isNotEmpty(rps.getHttpProxySettings().getAuthentication().getPassword())) {
        CRemoteAuthentication auth = rps.getHttpProxySettings().getAuthentication();
        auth.setPassword(encryptDecryptPassword(auth.getPassword(), encrypt, mask));
      }
      if (rps.getHttpsProxySettings() != null
          && rps.getHttpsProxySettings().getAuthentication() != null
          && StringUtils.isNotEmpty(rps.getHttpsProxySettings().getAuthentication().getPassword())) {
        CRemoteAuthentication auth = rps.getHttpsProxySettings().getAuthentication();
        auth.setPassword(encryptDecryptPassword(auth.getPassword(), encrypt, mask));
      }
    }

    // each repo
    for (CRepository repo : (List<CRepository>) config.getRepositories()) {
      // remote auth
      if (repo.getRemoteStorage() != null && repo.getRemoteStorage().getAuthentication() != null
          && StringUtils.isNotEmpty(repo.getRemoteStorage().getAuthentication().getPassword())) {
        CRemoteAuthentication auth = repo.getRemoteStorage().getAuthentication();
        auth.setPassword(encryptDecryptPassword(auth.getPassword(), encrypt, mask));
      }
    }
  }

  protected String encryptDecryptPassword(final String password, final boolean encrypt, final boolean mask) {
    if (mask) {
      return PASSWORD_MASK;
    }

    if (encrypt) {
      try {
        return passwordHelper.encrypt(password);
      }
      catch (PlexusCipherException e) {
        getLogger().error("Failed to encrypt password in nexus.xml.", e);
      }
    }
    else {
      try {
        return passwordHelper.decrypt(password);
      }
      catch (PlexusCipherException e) {
        getLogger().error("Failed to decrypt password in nexus.xml.", e);
      }
    }

    return password;
  }
}
