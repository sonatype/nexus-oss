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

package org.sonatype.nexus.error.reporting.bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.security.model.CUser;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;
import org.sonatype.sisu.pr.bundle.ManagedBundle;
import org.sonatype.sisu.pr.bundle.StorageManager;

import com.google.common.io.ByteStreams;
import com.google.common.io.OutputSupplier;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.IOUtil;

/**
 * Adds the security.xml to the error report bundle.
 * User email addresses and passwords will be masked.
 */
@Named("security.xml")
public class SecurityXmlAssembler
    extends AbstractXmlAssembler
    implements BundleAssembler
{

  SecurityModelConfigurationSource source;

  StorageManager storageManager;

  @Inject
  public SecurityXmlAssembler(final SecurityModelConfigurationSource source, final StorageManager storageManager) {
    this.source = source;
    this.storageManager = storageManager;
  }

  @Override
  public boolean isParticipating(IssueSubmissionRequest request) {
    return source.getConfiguration() != null;
  }

  @Override
  public Bundle assemble(IssueSubmissionRequest request)
      throws IssueSubmissionException
  {
    OutputStreamWriter out = null;
    try {
      final ManagedBundle bundle = storageManager.createBundle("security.xml", "application/xml");
      Configuration configuration =
          (Configuration) cloneViaXml(source.getConfiguration());

      if (configuration != null) {
        for (CUser user : configuration.getUsers()) {
          user.setPassword(PASSWORD_MASK);
          user.setEmail(PASSWORD_MASK);
        }
        SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();

        out = new OutputStreamWriter(bundle.getOutputStream());
        try {
          writer.write(out, configuration);
        }
        finally {
          out.close();
        }
      }
      else {
        ByteStreams.write(
            "Got no security configuration".getBytes("utf-8"),
            new OutputSupplier<OutputStream>()
            {
              @Override
              public OutputStream getOutput()
                  throws IOException
              {
                return bundle.getOutputStream();
              }
            }
        );
      }

      return bundle;
    }
    catch (IOException e) {
      IOUtil.close(out);
      throw new IssueSubmissionException("Could not assemble security.xml: " + e.getMessage(), e);
    }
  }
}
