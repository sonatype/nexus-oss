/*******************************************************************************
 * Copyright (c) 2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *******************************************************************************/

package org.sonatype.nexus.index.packer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.DefaultIndexingContext;
import org.sonatype.nexus.index.context.IncrementalContext;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 */
public class DefaultIncrementalContext implements IncrementalContext {

    /*
     
      nexus.index.time=20081026044801.276 -0500
      nexus.index.id=central
  
      nexus.index.day-0=20081026044801.276 -0500  s
      nexus.index.day-1=20081026044801.276 -0500  s
      nexus.index.day-2=20081026044801.276 -0500  f
      nexus.index.day-3=20081026044801.276 -0500  t
      nexus.index.day-4=20081026044801.276 -0500  w
      nexus.index.day-5=20081026044801.276 -0500  t
      nexus.index.day-6=20081026044801.276 -0500  m
     
     */
    
    public static void main(String[] args) throws Exception 
    {
      
        DefaultIndexPacker indexPacker = new DefaultIndexPacker();
        
        SimpleDateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_DAY_FORMAT );
        
        IndexingContext context = new DefaultIndexingContext(
            "central.test",
            "central.test",
            null,
            // new File("C:\\dev\\workspace3.3asm\\.metadata\\.plugins\\org.maven.ide.eclipse\\nexus\\central"),
            // new File("C:\\dev\\sonatype\\nexus-aggregator\\nexus-indexer\\repo"),
            new File("C:\\dev\\sonatype\\nexus-aggregator\\nexus-indexer\\central.20081030\\"),
            null,
            null,
            NexusIndexer.FULL_INDEX, false );
        
        Properties info = new Properties();
        
        File targetDir = new File("C:\\dev\\sonatype\\nexus-aggregator\\nexus-indexer\\");
        
        Map<String, List<Integer>> chunks = indexPacker.getIndexChunks( context, df );
        
        indexPacker.writeIndexChunks( context, info, 30, chunks, df, targetDir );
        
        indexPacker.writeIndexArchive( context, new File( targetDir, IndexingContext.INDEX_FILE + ".zip" ) );

        indexPacker.writeIndexProperties( context, info, new File( targetDir, IndexingContext.INDEX_FILE + ".properties" ) );
    }
  
}

