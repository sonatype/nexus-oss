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

package org.sonatype.nexus.proxy.events;

/**
 * Marker interface for subscribers wanting to get events from Nexus Event Bus, aka. the new event inspectors.
 * Example of event subscriber:
 * <pre>
 *   @Singleton
 *   @Named
 *   public class MySubscriber
 *     implements EventSubscriber {
 *
 *     @Subscribe
 *     public void onSomeEvent(final SomeEvent evt) {
 *       ... do something
 *     }
 *   }
 * </pre>
 * In short, you code as you would do usually with Google Guava EventBus (so using @Subscribe and
 * @AllowConcurrentEvents as usually), and to those annotated methods same constrains applies as for
 * plain event bus subscribers (method should be public, and have one parameter). The "trick" here is
 * that your component should implement the EventSubscriber interface, and in that case it will get
 * auto-registered with Nexus EventBus.
 *
 * @since 2.7.0
 */
public interface EventSubscriber
{
}
