package org.sonatype.nexus.index.treeview;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.index.Field;
import org.sonatype.nexus.index.MAVEN;

public class TreeViewRequest
{
    private final TreeNodeFactory factory;

    private final String path;

    private final Map<Field, String> fieldHints;

    public TreeViewRequest( TreeNodeFactory factory, String path )
    {
        this( factory, path, null );
    }

    public TreeViewRequest( TreeNodeFactory factory, String path, Map<Field, String> hints )
    {
        this.factory = factory;

        this.path = path;

        this.fieldHints = new HashMap<Field, String>();

        if ( hints != null && hints.size() != 0 )
        {
            this.fieldHints.putAll( hints );
        }
    }

    public TreeNodeFactory getFactory()
    {
        return factory;
    }

    public String getPath()
    {
        return path;
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
}
