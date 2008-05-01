/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
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
