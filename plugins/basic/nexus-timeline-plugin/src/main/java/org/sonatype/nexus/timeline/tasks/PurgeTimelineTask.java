/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.timeline.tasks;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.scheduling.TaskSupport;
import org.sonatype.nexus.timeline.Timeline;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Purge Timeline task.
 *
 * @since 3.0
 */
@Named
public class PurgeTimelineTask
    extends TaskSupport
{
  private final Timeline timeline;

  @Inject
  public PurgeTimelineTask(final Timeline timeline) {
    this.timeline = checkNotNull(timeline);
  }

  public int getPurgeOlderThan() {
    return getConfiguration().getInteger(PurgeTimelineTaskDescriptor.OLDER_THAN_FIELD_ID, 10);
  }

  @Override
  protected Void execute()
      throws Exception
  {
    timeline.purgeOlderThan(getPurgeOlderThan());
    return null;
  }

  @Override
  public String getMessage() {
    return "Purging Timeline.";
  }
}
