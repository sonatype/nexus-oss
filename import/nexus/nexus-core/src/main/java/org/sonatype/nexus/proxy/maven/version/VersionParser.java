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
package org.sonatype.nexus.proxy.maven.version;

/**
 * A version scheme that handles interpretation of version strings to facilitate their comparison.
 * <p>
 * Note: sources copied from Aether release 1.9 for Nexus internal uses. Once Maven support is moved out from a core to
 * a plugin, this class should be removed and switch to Aether Version classes is to be done.
 * 
 * @author Benjamin Bentmann
 * @author Alin Dreghiciu
 */
public interface VersionParser
{
    /**
     * Parses the specified version string, for example "1.0".
     * 
     * @param version The version string to parse, must not be {@code null}.
     * @return The parsed version, never {@code null}.
     * @throws InvalidVersionSpecificationException If the string violates the syntax rules of this scheme.
     */
    Version parseVersion( String version )
        throws InvalidVersionSpecificationException;

}
