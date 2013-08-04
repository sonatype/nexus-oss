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

package com.sonatype.nexus.build.error.reporting;

import org.sonatype.nexus.error.report.ErrorReportComponent;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Error report component used in nexus-core UTs only. Basically equivalent to OSS edition one,
 * even reporting the same (to not screw existing UTs), but the real thing is in nexus-oss-edition module.
 */
@Component(role = ErrorReportComponent.class)
public class BuildErrorReportComponent
    implements ErrorReportComponent
{

  private static final String COMPONENT = "Nexus";

  public String getComponent() {
    return COMPONENT;
  }

}
