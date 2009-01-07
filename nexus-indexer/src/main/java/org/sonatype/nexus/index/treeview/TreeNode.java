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

import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;

public interface TreeNode
{
    public enum Type
    {
        G, A, V, artifact
    };

    Type getType();

    void setType( Type t );

    boolean isLeaf();

    void setLeaf( boolean leaf );

    String getNodeName();

    void setNodeName( String name );

    String getPath();

    void setPath( String path );

    String getGroupId();

    void setGroupId( String groupId );

    String getArtifactId();

    void setArtifactId( String artifactId );

    String getVersion();

    void setVersion( String version );

    String getRepositoryId();

    void setRepositoryId( String repositoryId );

    List<TreeNode> getChildren();

    List<TreeNode> listChildren()
        throws IndexContextInInconsistentStateException,
            IOException;

    TreeNode findChildByPath( String path, Type type )
        throws IndexContextInInconsistentStateException,
            IOException;
}
