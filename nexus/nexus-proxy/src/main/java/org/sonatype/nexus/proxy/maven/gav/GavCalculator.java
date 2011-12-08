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
package org.sonatype.nexus.proxy.maven.gav;

/**
 * An interface to calculate <code>Gav</code> based on provided artifact path and to calculate an artifact path from
 * provided <code>Gav</code>.
 * 
 * @author Tamas Cservenak
 */
public interface GavCalculator
{
    /**
     * Calculates GAV from provided <em>repository path</em>. The path has to be absolute starting from repository root.
     * If path represents a proper artifact path (conforming to given layout), GAV is "calculated" from it and is
     * returned. If path represents some file that is not an artifact, but is part of the repository layout (like
     * maven-metadata.xml), or in any other case it returns null. TODO: some place for different levels of "validation"?
     * 
     * @param path the repository path
     * @return Gav parsed from the path
     */
    Gav pathToGav( String path );

    /**
     * Reassembles the repository path from the supplied GAV. It will be an absolute path.
     * 
     * @param gav
     * @return the path calculated from GAV, obeying current layout.
     */
    String gavToPath( Gav gav );
}
