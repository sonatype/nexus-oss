/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.index.treeview;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;

public interface TreeNodeFactory
{
    IndexingContext getIndexingContext();

    TreeNode createGNode( IndexTreeView tview, String path, String name );

    TreeNode createANode( IndexTreeView tview, ArtifactInfo ai, String path );

    TreeNode createVNode( IndexTreeView tview, ArtifactInfo ai, String path );

    TreeNode createArtifactNode( IndexTreeView tview, ArtifactInfo ai, String path );
}
