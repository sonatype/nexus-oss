/*
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
package org.sonatype.nexus.proxy.maven.wl;

import java.util.List;

/**
 * WL Configuration.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface WLConfig
{
    /**
     * Returns the "no scrape" file path used for flagging a repository to not be scraped.
     * 
     * @return the no scrape file path.
     */
    String getLocalNoScrapeFlagPath();

    /**
     * Returns the local file path to publish prefix file.
     * 
     * @return the prefix file path.
     */
    String getLocalPrefixFilePath();

    /**
     * Returns the paths that should be checked for published no scrape flags on remote.
     * 
     * @return the array of paths to have checked on remote.
     */
    List<String> getRemoteNoScrapeFlagPaths();

    /**
     * Returns the paths that should be checked for published prefix files on remote.
     * 
     * @return the array of paths to have checked on remote.
     */
    List<String> getRemotePrefixFilePaths();

    /**
     * Returns the depth (directory depth) that remote scrape should dive in.
     * 
     * @return the depth of the scrape.
     */
    int getRemoteScrapeDepth();

    /**
     * Returns the depth (directory depth) that local scrape should dive in.
     * 
     * @return the depth of the scrape.
     */
    int getLocalScrapeDepth();

    /**
     * Returns the depth (directory depth) that whitelist matching is used to perform matches.
     * 
     * @return the whitelist matching depth.
     */
    int getWLMatchingDepth();
}
