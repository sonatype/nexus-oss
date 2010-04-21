package org.sonatype.nexus.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

/**
 * Holds basic information about Indexer field, how it is stored. To keep this centralized, and not spread across code.
 * Since Lucene 2.x, the field names are encoded, so please use real chatty names instead of cryptic chars!
 * 
 * @author cstamas
 */
public class IndexerField
{
    private final IndexerFieldVersion version;

    private final String name;

    private final String description;

    private final Store storeMethod;

    private final Index indexMethod;

    public IndexerField( final IndexerFieldVersion version, final String name, final String description,
                         final Store storeMethod, final Index indexMethod )
    {
        this.version = version;

        this.name = name;

        this.description = description;

        this.storeMethod = storeMethod;

        this.indexMethod = indexMethod;
    }

    public IndexerFieldVersion getVersion()
    {
        return version;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public Field.Store getStoreMethod()
    {
        return storeMethod;
    }

    public Field.Index getIndexMethod()
    {
        return indexMethod;
    }

    public boolean isIndexed()
    {
        return !Index.NO.equals( indexMethod );
    }

    public boolean isStored()
    {
        return !Store.NO.equals( storeMethod );
    }

    public Field toField( String value )
    {
        return new Field( name, value, storeMethod, indexMethod );
    }
}
