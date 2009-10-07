/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.context;

// TODO TONI - copied from indexer where doc is a lucene document. 
// Chances are this will go away anyway.
public interface DocumentFilter
{
    boolean accept( Object doc );
}
