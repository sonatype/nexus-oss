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
package org.sonatype.nexus.test.booter;

/**
 * The interface to "boot" (control lifecycle) of the Nexus being under test in the ITs.
 * 
 * @author cstamas
 */
public interface NexusBooter
{
    /**
     * Starts one instance of Nexus bundle. May be invoked only once, or after {@link #stopNexus()} is invoked only,
     * otherwise will throw IllegalStateException.
     * 
     * @param testId
     * @throws Exception
     */
    public void startNexus( final String testId )
        throws Exception;

    /**
     * Stops, and cleans up the started Nexus instance. May be invoked any times, it will NOOP if not needed to do
     * anything. Will try to ditch the used classloader. The {@link #clean()} method will be invoked on every invocation
     * of this method, making it more plausible for JVM to recover/GC all the stuff from memory in case of any glitch.
     * 
     * @throws Exception
     */
    public void stopNexus()
        throws Exception;
}
