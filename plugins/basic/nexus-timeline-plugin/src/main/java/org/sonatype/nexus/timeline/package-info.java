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
/**
 * Timeline plugin.
 * <p/>
 * This plugin introduces the OrientDB backed {@link org.sonatype.nexus.timeline.Timeline} implementation, along
 * with some maintenance tasks, and a complete RSS/2.0 and Atom/1.0 capable feed endpoints and feed related API.
 * <p/>
 * Subpackages of this package are:
 * <ul>
 *   <li>tasks - where the Timeline maintenance tasks are</li>
 *   <li>internal - the implementation</li>
 *   <li>feeds - where the Feed related event recorder and API is</li>
 * </ul>
 *
 * @since 3.0
 */

package org.sonatype.nexus.timeline;
