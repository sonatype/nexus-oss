/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import org.apache.lucene.store.Directory;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.plugin.ExtensionPoint;

/**
 * Ability to spread index updates to (possible) plugin receivers. (NEXUS-2644)
 *
 * @author Toni Menzel
 */
@ExtensionPoint
public interface IndexUpdateSideEffect
{

    /**
     * Given a full or partial (see context partial parameter) lucene index (directory + context it has been integrated into),
     * this can let other participants (implementations of this type) know about the update.
     *
     * Any activity should not influence the callers further process (not fail via unchecked exception) if possible.
     * Implementations are most likely optional plugins.
     *
     * @param directory - the directory to merge
     * @param context   - original context
     * @param partial   - this update is partial (true) or a full update (false).
     */
    void updateIndex( Directory directory, IndexingContext context, boolean partial );

}
