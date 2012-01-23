/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.gwt.client.services;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.resource.Variant;

/**
 * Nexus Feeds service.
 * 
 * @author cstamas
 */
public interface FeedsService
{
    /**
     * List available feeds.
     * 
     * @param handler
     */
    void listFeeds( EntityResponseHandler handler );

    /**
     * Gets the feed in Nexus default variant.
     * 
     * @param path
     * @param handler
     */
    void readFeed( String path, EntityResponseHandler handler );

    /**
     * Gets the feed in custom variant. Feeds probably have no JSON representation but RSS or ATOM.
     * 
     * @param path
     * @param variant
     * @param handler
     */
    void readFeed( String path, Variant variant, EntityResponseHandler handler );
}
