package org.sonatype.nexus.index.treeview;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.index.ArtifactInfoFilter;
import org.sonatype.nexus.index.Field;
import org.sonatype.nexus.index.MAVEN;
import org.sonatype.nexus.index.context.IndexingContext;

public class TreeViewRequest
{
    private final TreeNodeFactory factory;

    private final String path;

    private final ArtifactInfoFilter artifactInfoFilter;

    private final Map<Field, String> fieldHints;

    private final IndexingContext indexingContext;

    public TreeViewRequest( final TreeNodeFactory factory, final String path, final IndexingContext ctx )
    {
        this( factory, path, null, null, ctx );
    }

    public TreeViewRequest( final TreeNodeFactory factory, final String path, final Map<Field, String> hints,
                            final ArtifactInfoFilter artifactInfoFilter, final IndexingContext ctx )
    {
        this.factory = factory;

        this.path = path;

        this.fieldHints = new HashMap<Field, String>();

        if ( hints != null && hints.size() != 0 )
        {
            this.fieldHints.putAll( hints );
        }

        this.artifactInfoFilter = artifactInfoFilter;

        this.indexingContext = ctx;
    }

    public TreeNodeFactory getFactory()
    {
        return factory;
    }

    public String getPath()
    {
        return path;
    }

    public ArtifactInfoFilter getArtifactInfoFilter()
    {
        return artifactInfoFilter;
    }

    public void addFieldHint( Field field, String hint )
    {
        fieldHints.put( field, hint );
    }

    public void removeFieldHint( Field field )
    {
        fieldHints.remove( field );
    }

    public boolean hasFieldHints()
    {
        return fieldHints.size() > 0 && ( hasFieldHint( MAVEN.GROUP_ID ) );
    }

    public boolean hasFieldHint( Field... fields )
    {
        for ( Field f : fields )
        {
            if ( !fieldHints.containsKey( f ) )
            {
                return false;
            }
        }

        return true;
    }

    public String getFieldHint( Field field )
    {
        return fieldHints.get( field );
    }
    
    public Map<Field, String> getFieldHints()
    {
        return fieldHints;
    }

    public IndexingContext getIndexingContext()
    {
        return indexingContext;
    }
}
