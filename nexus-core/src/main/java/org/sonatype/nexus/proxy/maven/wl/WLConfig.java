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
     * Returns {@code true} if feature is instance-wide active, {@code false} otherwise. This method is needed for UT
     * and IT workaround, and maybe as some problem avoidance in production environment. As this feature interferes a
     * lot (background threads, async nature, OOM on no proper cleanup), this is the recommended way to perform tests
     * that does not care or not aware of this new core feature..
     * 
     * @return {@code true} if feature is active.
     */
    boolean isFeatureActive();

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
     * Returns the maximum allowed entry count for prefix files. Files having more than allowed count of entries will be
     * refused to be loaded up. Note: Central prefix file has around 6000 entries.
     * 
     * @return the maximum allowed prefix file entry count.
     */
    int getPrefixFileMaxEntriesCount();

    /**
     * Returns the maximum allowed line length for prefix file line (entry). Files having more than allowed line length
     * will be refused to be loaded up.
     * 
     * @return the maximum allowed prefix file entry length.
     */
    public int getPrefixFileMaxLineLength();

    /**
     * Returns the maximum allowed file size for prefix file (in bytes). File being bigger than allowed file size will
     * be refused to be loaded up.
     * 
     * @return the maximum allowed file size for prefix file, in bytes.
     */
    public int getPrefixFileMaxSize();
}
