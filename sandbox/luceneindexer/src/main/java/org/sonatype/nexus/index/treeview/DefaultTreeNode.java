/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.treeview;

/**
 * The simplest treenode, that does not adds any "decoration" to the nodes.
 * 
 * @author Tamas Cservenak
 */
public class DefaultTreeNode
    extends AbstractTreeNode
{
    public DefaultTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        super( tview, factory );
    }
}
