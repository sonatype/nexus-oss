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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.inject.Named;

import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.sisu.pr.bundle.AbstractBundle;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;

import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;

/**
 * Adds the contextListing.txt to the error report bundle.
 * This assembler renders {@link ErrorReportRequest#getContext}.
 */
@Named("context")
public class MapContentsAssembler
    implements BundleAssembler
{

  @Override
  public boolean isParticipating(final IssueSubmissionRequest request) {

    return request.getContext() != null && request.getContext() instanceof ErrorReportRequest;
  }

  @Override
  public Bundle assemble(final IssueSubmissionRequest request)
      throws IssueSubmissionException
  {
    return new MapContentsBundle(((ErrorReportRequest) request.getContext()).getContext());
  }

  public static class MapContentsBundle
      extends AbstractBundle
  {

    private static final String LINE_SEPERATOR = System.getProperty("line.separator");

    private byte[] content;

    public MapContentsBundle(Map<String, Object> context) {
      super("contextListing.txt", "text/plain");

      StringBuilder sb = new StringBuilder();

      for (String key : context.keySet()) {
        sb.append("key: " + key);
        sb.append(LINE_SEPERATOR);

        Object o = context.get(key);
        sb.append("value: " + o == null ? "null" : o.toString());
        sb.append(LINE_SEPERATOR);
        sb.append(LINE_SEPERATOR);
      }

      try {
        this.content = sb.toString().getBytes("utf-8");
      }
      catch (UnsupportedEncodingException e) {
        // use default platform encoding
        this.content = sb.toString().getBytes();
      }
    }

    @Override
    protected InputStream openStream()
        throws IOException
    {
      return new ByteArrayInputStream(content);
    }

    @Override
    public long getContentLength() {
      return content.length;
    }

  }
}
