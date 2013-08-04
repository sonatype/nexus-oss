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
/**
 */

package org.sonatype.nexus.error.reporting.bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
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
 * Adds the security-configuration.xml to the error report bundle.
 * Anonymous password will be masked.
 */
@Named("security-configuration.xml")
public class SecurityConfigurationXmlAssembler
    extends AbstractXmlAssembler
    implements BundleAssembler
{

  SecurityConfigurationSource source;

  StorageManager storageManager;

  @Inject
  public SecurityConfigurationXmlAssembler(final SecurityConfigurationSource source,
                                           final StorageManager storageManager)
  {
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
    SecurityConfiguration configuration = (SecurityConfiguration) cloneViaXml(source.getConfiguration());

    try {
      final ManagedBundle bundle = storageManager.createBundle("security-configuration.xml", "application/xml");

      if (configuration != null) {
        configuration.setAnonymousPassword(PASSWORD_MASK);

        SecurityConfigurationXpp3Writer xppWriter = new SecurityConfigurationXpp3Writer();

        Writer writer = new OutputStreamWriter(bundle.getOutputStream());
        try {
          xppWriter.write(writer, configuration);
        }
        finally {
          IOUtil.close(writer);
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
      throw new IssueSubmissionException("Could not assemble security-configuration.xml-bundle", e);
    }

  }
}
