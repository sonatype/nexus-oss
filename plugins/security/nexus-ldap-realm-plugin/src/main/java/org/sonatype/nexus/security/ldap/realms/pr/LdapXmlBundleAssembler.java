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

package org.sonatype.nexus.security.ldap.realms.pr;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;
import org.sonatype.security.ldap.realms.persist.model.Configuration;
import org.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Writer;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;
import org.sonatype.sisu.pr.bundle.ManagedBundle;
import org.sonatype.sisu.pr.bundle.StorageManager;

import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;

/**
 * Load LDAP configuration from disk and mask passwords.
 *
 * @since 2.2
 */
@Named
public class LdapXmlBundleAssembler
    implements BundleAssembler
{

  private ApplicationConfiguration cfg;

  private LdapConfiguration source;

  private final StorageManager storage;

  @Inject
  public LdapXmlBundleAssembler(final ApplicationConfiguration cfg, final LdapConfiguration source,
                                final StorageManager storage)
  {
    this.cfg = cfg;
    this.source = source;
    this.storage = storage;
  }

  @Override
  public boolean isParticipating(final IssueSubmissionRequest issueSubmissionRequest) {
    // file is created if ldap realm is activated
    return new File(cfg.getConfigurationDirectory(), "ldap.xml").exists();
  }

  @Override
  public Bundle assemble(final IssueSubmissionRequest issueSubmissionRequest)
      throws IssueSubmissionException
  {
    ManagedBundle bundle;
    try {
      bundle = storage.createBundle("ldap.xml", "application/xml");

      final Configuration configuration = source.getConfiguration();

      if (configuration == null) {
        final OutputStream outputStream = bundle.getOutputStream();
        try {
          outputStream.write("No ldap configuration".getBytes("us-ascii"));
        }
        finally {
          outputStream.close();
        }
      }
      else {
        Writer fw = null;
        try {
          fw = new OutputStreamWriter(bundle.getOutputStream());

          LdapConfigurationXpp3Writer writer = new LdapConfigurationXpp3Writer();

          configuration.getConnectionInfo().setSystemPassword("***");

          writer.write(fw, configuration);
        }
        finally {
          if (fw != null) {
            fw.flush();

            fw.close();
          }
        }
      }

      return bundle;
    }
    catch (Exception e) {
      throw new IssueSubmissionException("Could not create ldap.xml to PR bundle", e);
    }

  }
}
