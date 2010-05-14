package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Field
{
    public static final String NOT_PRESENT = "N/P";
    
    private final Field parent;

    private final String namespace;

    private final String fieldName;

    private final String description;

    private final List<IndexerField> indexerFields;

    public Field( final Field parent, final String namespace, final String name, final String description )
    {
        this.parent = parent;

        this.namespace = namespace;

        this.fieldName = name;

        this.description = description;

        this.indexerFields = new ArrayList<IndexerField>();
    }

    public Field getParent()
    {
        return parent;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getDescription()
    {
        return description;
    }

    public Collection<IndexerField> getIndexerFields()
    {
        return Collections.unmodifiableList( indexerFields );
    }

    public boolean addIndexerField( IndexerField field )
    {
        return indexerFields.add( field );
    }

    public boolean removeIndexerField( IndexerField field )
    {
        return indexerFields.remove( field );
    }

    public String getFQN()
    {
        return getNamespace() + getFieldName();
    }

    public String toString()
    {
        return getFQN() + " (with " + getIndexerFields().size() + " registered index fields)";
    }
}
