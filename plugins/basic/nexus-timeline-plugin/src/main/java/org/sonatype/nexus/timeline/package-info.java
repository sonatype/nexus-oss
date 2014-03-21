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

/**
 * Nexus Timeline plugin.
 * <p/>
 * This plugin introduces the Kazuki backed {@link org.sonatype.nexus.timeline.NexusTimeline} implementation, along
 * with some maintenance tasks, feed related REST endpoints and feed related internal code.
 * <p/>
 * Subpackages of this package are:
 * <ul>
 *   <li>feeds - where the RSS Feed related event recorder and listeners relies</li>
 *   <li>rest - where the RSS feed exposing REST resources are, among with some Feed UI related resources</li>
 *   <li>tasks - where the Timeline maintenance tasks (purge timeline) is</li>
 *   <li>internal - the implementation and Guice module</li>
 * </ul>
 *
 * @since 3.0
 */

package org.sonatype.nexus.timeline;
