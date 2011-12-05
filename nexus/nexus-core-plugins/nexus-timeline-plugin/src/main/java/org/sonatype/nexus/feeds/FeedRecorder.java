/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.feeds;

import java.util.List;
import java.util.Set;

import org.sonatype.nexus.timeline.Entry;
import com.google.common.base.Predicate;

/**
 * A recorder for events for later retrieval. The Actions are "generic" Nexus event related. For specific (Maven, P2)
 * actions, look into specific sources. Note: This is actually event recorder, not feed recorder.
 * 
 * @author cstamas
 */
public interface FeedRecorder
{
    /**
     * System event action: boot
     */
    public static final String SYSTEM_BOOT_ACTION = "BOOT";

    /**
     * System event action: configuration
     */
    public static final String SYSTEM_CONFIG_ACTION = "CONFIG";

    /**
     * System event action: repository local status changes
     */
    public static final String SYSTEM_REPO_LSTATUS_CHANGES_ACTION = "REPO_LSTATUS_CHANGES";

    /**
     * System event action: repository proxy status auto change
     */
    public static final String SYSTEM_REPO_PSTATUS_CHANGES_ACTION = "REPO_PSTATUS_CHANGES";

    /**
     * System event action: repository proxy status auto change
     */
    public static final String SYSTEM_REPO_PSTATUS_AUTO_CHANGES_ACTION = "REPO_PSTATUS_AUTO_CHANGES";

    /**
     * System event action: authentication
     */
    public static final String SYSTEM_AUTHC = "AUTHC";
    
    /**
     * System event action: authorization
     */
    public static final String SYSTEM_AUTHZ = "AUTHZ";

    // creating

    void addErrorWarningEvent( String action, String message);
    
    void addErrorWarningEvent( String action, String message, Throwable throwable);
    
    void addNexusArtifactEvent( NexusArtifactEvent nae );

    void addSystemEvent( String action, String message );

    void addAuthcAuthzEvent( AuthcAuthzEvent evt );

    SystemProcess systemProcessStarted( String action, String message );

    void systemProcessFinished( SystemProcess prc, String finishMessage );

    void systemProcessCanceled( SystemProcess prc, String cancelMessage );

    void systemProcessBroken( SystemProcess prc, Throwable e );

    // reading

    List<NexusArtifactEvent> getNexusArtifectEvents( Set<String> subtypes, Integer from, Integer count,
        Predicate<Entry> filter );
    
    List<SystemEvent> getSystemEvents( Set<String> subtypes, Integer from, Integer count, Predicate<Entry> filter );

    List<AuthcAuthzEvent> getAuthcAuthzEvents( Set<String> subtypes, Integer from, Integer count, Predicate<Entry> filter );
    
    List<ErrorWarningEvent> getErrorWarningEvents( Set<String> subtypes, Integer from, Integer count, Predicate<Entry> filter );
}
