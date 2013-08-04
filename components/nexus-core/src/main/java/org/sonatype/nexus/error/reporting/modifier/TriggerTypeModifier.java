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

package org.sonatype.nexus.error.reporting.modifier;

import javax.inject.Named;

import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.sisu.pr.Modifier;

import org.codehaus.plexus.swizzle.IssueSubmissionRequest;

import static org.sonatype.sisu.pr.Modifier.Priority.MODIFIER;

/**
 * Prepends MPR or APR to the issue summary based on {@link ErrorReportRequest#isManual()}
 */
@Named
public class TriggerTypeModifier
    implements Modifier
{

  @Override
  public IssueSubmissionRequest modify(IssueSubmissionRequest request) {
    String prefix = ((ErrorReportRequest) request.getContext()).isManual() ? "MPR" : "APR";
    request.setSummary(prefix + ": " + request.getSummary());
    return request;
  }

  @Override
  public int getPriority() {
    return MODIFIER.priority();
  }

}
