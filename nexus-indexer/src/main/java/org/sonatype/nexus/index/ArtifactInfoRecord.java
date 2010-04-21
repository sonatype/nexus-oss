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
        new IndexerField( IndexerFieldVersion.V1, "u", "Artifact UINFO (as keyword, stored)", Store.YES,
                          Index.UN_TOKENIZED );

    /**
     * Info: packaging, lastModified, size, sourcesExists, javadocExists, signatureExists. Stored, not indexed.
     */
    public static final IndexerField FLD_INFO =
        new IndexerField( IndexerFieldVersion.V1, "i", "Artifact INFO (not indexed, stored)", Store.YES, Index.NO );

    public static final IndexerField FLD_GROUP_ID_KW =
        new IndexerField( IndexerFieldVersion.V1, "g", "Artifact GroupID (as keyword)", Store.NO, Index.UN_TOKENIZED );

    public static final IndexerField FLD_GROUP_ID =
        new IndexerField( IndexerFieldVersion.V3, "groupId", "Artifact GroupID (tokenized)", Store.NO, Index.TOKENIZED );

    public static final IndexerField FLD_ARTIFACT_ID_KW =
        new IndexerField( IndexerFieldVersion.V1, "a", "Artifact ArtifactID (as keyword)", Store.NO, Index.UN_TOKENIZED );

    public static final IndexerField FLD_ARTIFACT_ID =
        new IndexerField( IndexerFieldVersion.V3, "artifactId", "Artifact ArtifactID (tokenized)", Store.NO,
                          Index.TOKENIZED );

    public static final IndexerField FLD_VERSION_KW =
        new IndexerField( IndexerFieldVersion.V1, "v", "Artifact Version (as keyword)", Store.NO, Index.UN_TOKENIZED );

    public static final IndexerField FLD_VERSION =
        new IndexerField( IndexerFieldVersion.V3, "version", "Artifact Version (tokenized)", Store.NO, Index.TOKENIZED );

    public static final IndexerField FLD_PACKAGING =
        new IndexerField( IndexerFieldVersion.V1, "p", "Artifact Packaging (as keyword)", Store.NO, Index.UN_TOKENIZED );

    public static final IndexerField FLD_CLASSIFIER =
        new IndexerField( IndexerFieldVersion.V1, "l", "Artifact classifier (as keyword)", Store.NO, Index.UN_TOKENIZED );

    public static final IndexerField FLD_NAME =
        new IndexerField( IndexerFieldVersion.V1, "n", "Artifact name (tokenized, stored)", Store.YES, Index.TOKENIZED );

    public static final IndexerField FLD_DESCRIPTION =
        new IndexerField( IndexerFieldVersion.V1, "d", "Artifact description (tokenized, stored)", Store.YES,
                          Index.TOKENIZED );

    public static final IndexerField FLD_LAST_MODIFIED =
        new IndexerField( IndexerFieldVersion.V1, "m", "Artifact last modified (not indexed, stored)", Store.YES,
                          Index.NO );

    public static final IndexerField FLD_SHA1 =
        new IndexerField( IndexerFieldVersion.V1, "1", "Artifact SHA1 checksum (as keyword, stored)", Store.YES,
                          Index.UN_TOKENIZED );

    /**
     * NexusAnalyzer makes exception with this field only, to keep backward compatibility with old consumers of
     * nexus-indexer.
     */
    public static final IndexerField FLD_CLASSNAMES_KW =
        new IndexerField( IndexerFieldVersion.V1, "c", "Artifact Classes (tokenized on newlines only)", Store.COMPRESS,
                          Index.TOKENIZED );

    public static final IndexerField FLD_CLASSNAMES =
        new IndexerField( IndexerFieldVersion.V3, "classnames", "Artifact Classes (tokenized)", Store.NO,
                          Index.TOKENIZED );

    public static final IndexerField FLD_PLUGIN_PREFIX =
        new IndexerField( IndexerFieldVersion.V1, "px", "MavenPlugin prefix (as keyword, stored)", Store.NO,
                          Index.UN_TOKENIZED );

    public static final IndexerField FLD_PLUGIN_GOALS =
        new IndexerField( IndexerFieldVersion.V1, "gx", "MavenPlugin goals (as keyword, stored)", Store.NO,
                          Index.UN_TOKENIZED );

    public static final IndexerField FLD_DELETED =
        new IndexerField( IndexerFieldVersion.V1, "del",
                          "Deleted field, will contain UINFO if document is deleted from index (not indexed, stored)",
                          Store.YES, Index.NO );

}
