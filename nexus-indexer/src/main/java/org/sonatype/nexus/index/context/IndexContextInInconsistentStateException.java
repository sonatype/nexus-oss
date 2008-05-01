/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.context;

import org.apache.lucene.document.Document;

/**
 * Thrown when a construction of an ArtifactInfo is requested using an unknown Lucene Document. This exception can
 * happen during search, when the search engine handles the create Lucene Hit List and tries to sort out who is
 * responsible for ArtifactInfo creation and finds no one.
 * 
 * @author cstamas
 */
public class IndexContextInInconsistentStateException
    extends Exception
{
    private static final long serialVersionUID = -4280462255273660843L;

    private Document document;

    private IndexingContext context;

    public IndexContextInInconsistentStateException( IndexingContext context, Document document, String message )
    {
        super( message );
        this.document = document;
        this.context = context;
    }

    public Document getDocument()
    {
        return document;
    }

    public IndexingContext getContext()
    {
        return context;
    }

}
