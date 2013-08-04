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

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.error.report.ErrorReportComponent;
import org.sonatype.sisu.pr.Modifier;

import org.codehaus.plexus.swizzle.IssueSubmissionRequest;

import static org.sonatype.sisu.pr.Modifier.Priority.FILLIN;

/**
 * Sets the JIRA component as specified in the {@link ErrorReportComponent}.
 */
@Named
public class ComponentModifier
    implements Modifier
{

  ErrorReportComponent component;

  @Inject
  public ComponentModifier(final ErrorReportComponent component) {
    this.component = component;
  }

  @Override
  public IssueSubmissionRequest modify(final IssueSubmissionRequest request) {
    request.setComponent(component.getComponent());
    return request;
  }

  @Override
  public int getPriority() {
    return FILLIN.priority();
  }
}
