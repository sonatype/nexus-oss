package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.index.context.IndexCreator;

public class AbstractIndexCreatorHelper
    extends PlexusTestCase
{
    public List<IndexCreator> DEFAULT_CREATORS;
    public List<IndexCreator> FULL_CREATORS;
    public List<IndexCreator> MIN_CREATORS;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        DEFAULT_CREATORS = new ArrayList<IndexCreator>();
        FULL_CREATORS = new ArrayList<IndexCreator>();
        MIN_CREATORS = new ArrayList<IndexCreator>();
        
        IndexCreator min = lookup( IndexCreator.class, "min" );
        IndexCreator jar = lookup( IndexCreator.class, "jarContent" );
        
        MIN_CREATORS.add( min );
        
        FULL_CREATORS.add( min );
        FULL_CREATORS.add( jar );
        
        DEFAULT_CREATORS.addAll( FULL_CREATORS );
    }
}
