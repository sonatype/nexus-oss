package org.sonatype.nexus.index;

/**
 * Ontology of Nexus. This is still Maven2 specific. Ideally, Nexus Ontology should contain three things only: path,
 * sha1 and last_modified. And Indexer should index _everything_, and Maven should be just "topping" extending these
 * informations. This would enable us to search and easily detect maven metadata files too, or to support non-maven
 * repository indexing, like P2 is.
 * 
 * @author cstamas
 */
public interface NEXUS
{
    public static final String NEXUS_NAMESPACE = "urn:nexus#";

    // UINFO: Artifact Unique Info (groupId, artifactId, version, classifier, extension (or packaging))
    public static final Field UINFO = new Field( null, NEXUS_NAMESPACE, "uinfo", "Artifact Unique Info" );

    // INFO: Artifact Info (packaging, lastModified, size, sourcesExists, javadocExists, signatureExists)
    public static final Field INFO = new Field( null, NEXUS_NAMESPACE, "info", "Artifact Info" );

    // DELETED: Deleted field marker (will contain UINFO if document is deleted from index)
    public static final Field DELETED = new Field( null, NEXUS_NAMESPACE, "del", "Deleted field marker" );
}
