/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.treeview;

import java.io.IOException;

/**
 * Component rendering a "tree" based on index data.
 * 
 * @author cstamas
 */
public interface IndexTreeView
{
    /**
     * Returns the TreeNode, as calculated by passed in parameters using index data.
     * 
     * @param factory
     * @param path
     * @return
     * @throws IOException
     * @deprecated Use {{@link #listNodes(TreeViewRequest)}
     */
    TreeNode listNodes( TreeNodeFactory factory, String path )
        throws IOException;

    /**
     * Returns the TreeNode, as calculated by passed in request, using index data.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    TreeNode listNodes( TreeViewRequest request )
        throws IOException;
}
