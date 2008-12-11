/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus;

/**
 * The enum of possible states in which Nexus Application may reside.
 * 
 * @author cstamas
 */
public enum SystemState
{
    /**
     * Nexus is in process of starting. Should not be bothered until it is RUNNING.
     */
    STARTING,

    /**
     * Nexus is running and is healthy. It is fully functional.
     */
    STARTED,

    /**
     * Nexus tried to start up, but is failed due to broken user configuration. It is nonfunctional.
     */
    BROKEN_CONFIGURATION,

    /**
     * Nexus tried to start up, but is failed due to some unexpected IO error. It is nonfunctional.
     */
    BROKEN_IO,

    /**
     * Nexus is being shutdown.
     */
    STOPPING,

    /**
     * Nexus is shut down.
     */
    STOPPED;
}
