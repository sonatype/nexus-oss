/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
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
package org.sonatype.nexus.index;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.IndexPacker;

public class IndexPackagerTask
    extends AbstractIndexerTask<Object>
{
    
    private final IndexPacker indexPacker;
    
    private final IndexingContext indexingContext;

    public IndexPackagerTask( Nexus nexus, IndexerManager indexerManager, IndexPacker packer, IndexingContext context )
    {
        super( nexus, indexerManager );
        
        this.indexPacker = packer;
        
        this.indexingContext = context;
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        getIndexerManager().reindexRepository( indexingContext.getRepositoryId() );
        
        indexPacker.packIndex( indexingContext, null );

        return null;
    }

    @Override
    protected String getAction()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getMessage()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
