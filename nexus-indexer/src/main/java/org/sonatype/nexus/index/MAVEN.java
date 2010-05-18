package org.sonatype.nexus.index;

/**
 * Maven ontology.
 * 
 * @author cstamas
 */
public interface MAVEN
{
    public static final String MAVEN_NAMESPACE = "urn:maven#";

    public static final Field REPOSITORY_ID =
        new Field( null, MAVEN_NAMESPACE, "repositoryId", "Artifact Repository ID." );

    public static final Field GROUP_ID = new Field( null, MAVEN_NAMESPACE, "groupId", "Artifact Group ID." );

    public static final Field ARTIFACT_ID = new Field( null, MAVEN_NAMESPACE, "artifactId", "Artifact ID." );

    public static final Field VERSION = new Field( null, MAVEN_NAMESPACE, "version", "Artifact Version ID." );

    public static final Field BASE_VERSION =
        new Field( null, MAVEN_NAMESPACE, "baseVersion", "Artifact Base Version ID." );

    public static final Field CLASSNAMES = new Field( null, MAVEN_NAMESPACE, "classNames", "Artifact Classes." );

    public static final Field PACKAGING =
        new Field( null, MAVEN_NAMESPACE, "packaging", "Artifact Packaging (extension for secondary artifacts!)." );

    public static final Field CLASSIFIER = new Field( null, MAVEN_NAMESPACE, "classifier", "Artifact Classifier." );

    public static final Field NAME = new Field( null, MAVEN_NAMESPACE, "name", "Artifact Name (from POM)." );

    public static final Field DESCRIPTION =
        new Field( null, MAVEN_NAMESPACE, "name", "Artifact Description (from POM)." );

    public static final Field LAST_MODIFIED =
        new Field( null, MAVEN_NAMESPACE, "lastModified", "Artifact Last Modified Timestamp (UTC millis)." );

    public static final Field SHA1 = new Field( null, MAVEN_NAMESPACE, "sha1", "Artifact SHA1 checksum." );

    public static final Field PLUGIN_PREFIX =
        new Field( null, MAVEN_NAMESPACE, "pluginPrefix", "MavenPlugin Artifact Plugin Prefix." );

    public static final Field PLUGIN_GOALS =
        new Field( null, MAVEN_NAMESPACE, "pluginGoals", "MavenPlugin Artifact Plugin Goals." );
}
