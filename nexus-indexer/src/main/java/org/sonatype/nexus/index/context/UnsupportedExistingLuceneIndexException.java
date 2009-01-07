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
package org.sonatype.nexus.index.context;

/**
 * Thrown when a user tries to create a NexusInder IndexingContext over and existing Lucene index. The reason for
 * throwing this exception may be multiple: non-NexusIndexer Lucene index, index version is wrong, repositoryId does not
 * matches the context repositoryId, etc.
 * 
 * @author cstamas
 */
public class UnsupportedExistingLuceneIndexException
    extends Exception
{
    private static final long serialVersionUID = -3206758653346308322L;

    public UnsupportedExistingLuceneIndexException( String message )
    {
        super( message );
    }

}
