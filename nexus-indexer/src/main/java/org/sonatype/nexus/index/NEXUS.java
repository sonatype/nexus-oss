package org.sonatype.nexus.index;

/**
 * Ontology of Nexus.
 * 
 * @author cstamas
 */
public interface NEXUS
{
    public static final String NEXUS_NAMESPACE = "urn:nexus#";

    public static final Field UINFO =
        new Field( null, NEXUS_NAMESPACE, "uinfo",
                   "Artifact Unique Info (groupId, artifactId, version, classifier, extension (or packaging))." );

    public static final Field INFO =
        new Field( null, NEXUS_NAMESPACE, "info",
                   "Artifact Info (packaging, lastModified, size, sourcesExists, javadocExists, signatureExists)." );

    public static final Field DELETED =
        new Field( null, NEXUS_NAMESPACE, "del", "Deleted field, will contain UINFO if document is deleted from index." );
}
