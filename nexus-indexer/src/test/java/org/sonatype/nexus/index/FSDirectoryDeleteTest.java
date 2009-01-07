/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.index;

import java.io.File;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.context.IndexingContext;

public class FSDirectoryDeleteTest
    extends PlexusTestCase
{
    protected NexusIndexer nexusIndexer;
    
    protected File repo = new File( getBasedir(), "src/test/nexus-13" );
    
    protected IndexingContext context;
    protected File indexDirFile = new File( getBasedir(), "target/fsdirectorytest/one" ); 
    protected Directory indexDir;
    
    protected IndexingContext otherContext;
    protected File otherIndexDirFile = new File( getBasedir(), "target/fsdirectorytest/other" ); 
    protected Directory otherIndexDir;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        nexusIndexer = (NexusIndexer) lookup( NexusIndexer.class );
        
        indexDir = FSDirectory.getDirectory( indexDirFile );
        
        context = nexusIndexer.addIndexingContext(
            "one",
            "nexus-13",
            repo,
            indexDir,
            null,
            null,
            NexusIndexer.DEFAULT_INDEX, false );
        
        nexusIndexer.scan( context );
        
        otherIndexDir = FSDirectory.getDirectory( otherIndexDirFile );
        
        otherContext = nexusIndexer.addIndexingContext(
            "other",
            "nexus-13",
            repo,
            otherIndexDir,
            null,
            null,
            NexusIndexer.DEFAULT_INDEX, false );
        
        nexusIndexer.scan( otherContext );
    }
    
    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        
        nexusIndexer.removeIndexingContext( context, true );
        
        nexusIndexer.removeIndexingContext( otherContext, true );
        
        FileUtils.deleteDirectory( indexDirFile );
        
        FileUtils.deleteDirectory( otherIndexDirFile );
    }
    
    public void testIndexAndDelete()
        throws Exception
    {
        context.getIndexReader().maxDoc();
        
        otherContext.getIndexReader().maxDoc();
        
        context.replace( otherIndexDir );
        
        context.merge( otherIndexDir );
    }
}
