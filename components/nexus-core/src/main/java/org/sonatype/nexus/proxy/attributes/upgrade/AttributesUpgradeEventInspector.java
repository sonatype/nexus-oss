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

package org.sonatype.nexus.proxy.attributes.upgrade;

import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * EventInspector that fires upgrade call to upgrader component, does it blindly.
 *
 * @since 2.0
 */
@Component(role = EventInspector.class, hint = "AttributesUpgradeEventInspector")
public class AttributesUpgradeEventInspector
    extends AbstractEventInspector
    implements EventInspector, AsynchronousEventInspector
{
  @Requirement
  private AttributeUpgrader attributeUpgrader;

  @Override
  public boolean accepts(Event<?> evt) {
    return evt instanceof NexusStartedEvent;
  }

  @Override
  public void inspect(Event<?> evt) {
    if (accepts(evt)) {
      attributeUpgrader.upgradeAttributes();
    }
  }
}
