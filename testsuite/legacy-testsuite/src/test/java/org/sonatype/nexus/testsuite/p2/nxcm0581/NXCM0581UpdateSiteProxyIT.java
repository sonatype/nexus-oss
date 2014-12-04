/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.p2.nxcm0581;

import org.sonatype.nexus.plugins.p2.repository.updatesite.UpdateSiteMirrorTask;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.testsuite.p2.AbstractNexusProxyP2IT;

import org.junit.Test;

public class NXCM0581UpdateSiteProxyIT
    extends AbstractNexusProxyP2IT
{

  public NXCM0581UpdateSiteProxyIT() {
    super("nxcm0581");
  }

  @Test
  public void test()
      throws Exception
  {
    ScheduledServicePropertyResource forceMirror = new ScheduledServicePropertyResource();
    forceMirror.setKey("ForceMirror");
    forceMirror.setValue(Boolean.TRUE.toString());
    ScheduledServicePropertyResource repositoryId = new ScheduledServicePropertyResource();
    repositoryId.setKey("repositoryId");
    repositoryId.setValue("nxcm0581");
    TaskScheduleUtil.runTask("test", UpdateSiteMirrorTask.class.getName(), forceMirror, repositoryId);
    TaskScheduleUtil.waitForAllTasksToStop();

    installAndVerifyP2Feature();
  }

}
