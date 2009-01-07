/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.index;

import java.util.Map;

/**
 * Grouping drives the search Map filling, if groupes search requested.
 * 
 * @author cstamas
 */
public interface Grouping
{

    /**
     * Adds a single ArticatInfo to the result map.
     * 
     * @param result
     * @param artifactInfo
     * @return true, if the Grouping changed by addition of new artifactInfo
     */
    boolean addArtifactInfo( Map<String, ArtifactInfoGroup> result, ArtifactInfo artifactInfo );

}
