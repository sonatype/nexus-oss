/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.treeview;

import org.sonatype.nexus.index.ArtifactInfo;

public interface TreeNodeFactory
{
    String getRepositoryId();

    TreeNode createGNode( IndexTreeView tview, TreeViewRequest request, String path, String name );

    TreeNode createANode( IndexTreeView tview, TreeViewRequest request, ArtifactInfo ai, String path );

    TreeNode createVNode( IndexTreeView tview, TreeViewRequest request, ArtifactInfo ai, String path );

    TreeNode createArtifactNode( IndexTreeView tview, TreeViewRequest request, ArtifactInfo ai, String path );
}
