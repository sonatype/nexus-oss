package org.sonatype.nexus.index;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

/**
 * Pulling out ArtifactInfo, clearing up. TBD. This gonna be extensible "map-like" class with fields.
 * 
 * @author cstamas
 */
public class ArtifactInfoRecord
    implements Serializable
{
    private static final long serialVersionUID = -4577081994768263824L;

    /** Field separator */
    public static final String FS = "|";

    public static final Pattern FS_PATTERN = Pattern.compile( Pattern.quote( FS ) );

    /** Non available value */
    public static final String NA = "NA";

    // ----------
    // V3 changes
    // TODO: use getters instead of public fields
    // ----------
    // Listing all the fields that ArtifactInfo has on LuceneIndex

    /**
     * Unique groupId, artifactId, version, classifier, extension (or packaging). Stored, indexed untokenized
     */
    public static final IndexerField FLD_UINFO =
        new IndexerField( NEXUS.UINFO, IndexerFieldVersion.V1, "u", "Artifact UINFO (as keyword, stored)", Store.YES,
                          Index.UN_TOKENIZED );

    /**
     * Del: contains UINFO to mark record as deleted (needed for incremental updates!). The original document IS
     * removed, but this marker stays on index to note that fact.
     */
    public static final IndexerField FLD_DELETED =
        new IndexerField( NEXUS.DELETED, IndexerFieldVersion.V1, "del",
                          "Deleted field, will contain UINFO if document is deleted from index (not indexed, stored)",
                          Store.YES, Index.NO );



}
