/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.VersionUtils;

/**
 * ArtifactInfo holds the values known about an repository artifact. This is a simple Value Object kind of stuff.
 * 
 * @author Jason van Zyl
 * @author Eugene Kuleshov
 */
public class ArtifactInfo
    implements Serializable
{
    private static final long serialVersionUID = 6028843453477511104L;

    /** Field separator */
    public static final String FS = "|";

    public static final Pattern FS_PATTERN = Pattern.compile( Pattern.quote( FS ) );

    /** Non available value */
    public static final String NA = "NA";

    public static final String ROOT_GROUPS = "rootGroups";

    public static final String ROOT_GROUPS_VALUE = "rootGroups";

    public static final String ROOT_GROUPS_LIST = "rootGroupsList";

    public static final String ALL_GROUPS = "allGroups";

    public static final String ALL_GROUPS_VALUE = "allGroups";

    public static final String ALL_GROUPS_LIST = "allGroupsList";

    /**
     * Unique groupId, artifactId, version, classifier, extension (or packaging). 
     * Stored, indexed untokenized
     */
    public static final String UINFO = "u";

    /**
     * GroupId.
     * Not stored, indexed untokenized
     */
    public static final String GROUP_ID = "g";

    /**
     * ArtifactId.
     * Not stored, indexed tokenized
     */
    public static final String ARTIFACT_ID = "a";

    /**
     * Version.
     * Not stored, indexed tokenized
     */
    public static final String VERSION = "v";

    /**
     * Packaging.
     * Not stored, indexed untokenized
     */
    public static final String PACKAGING = "p";

    /**
     * Classifier.
     * Not stored, indexed untokenized
     */
    public static final String CLASSIFIER = "l";
    
    /**
     * Info: packaging, lastModified, size, sourcesExists, javadocExists, signatureExists. 
     * Stored, not indexed.
     */
    public static final String INFO = "i";

    /**
     * Name.
     * Stored, not indexed
     */
    public static final String NAME = "n";

    /**
     * Description.
     * Stored, not indexed
     */
    public static final String DESCRIPTION = "d";

    /**
     * Last modified.
     * Stored, not indexed
     */
    public static final String LAST_MODIFIED = "m";

    /**
     * SHA1. 
     * Stored, indexed untokenized
     */
    public static final String SHA1 = "1";

    /**
     * Class names
     * Stored compressed, indexed tokeninzed
     */
    public static final String NAMES = "c";

    /**
     * Plugin prefix.
     * Stored, not indexed
     */
    public static final String PLUGIN_PREFIX = "px";

    /**
     * Plugin goals.
     * Stored, not indexed
     */
    public static final String PLUGIN_GOALS = "gx";

    /**
     * Field that contains {@link #UINFO} value for deleted artifact
     */
    public static final String DELETED = "del";

    public static final Comparator<ArtifactInfo> VERSION_COMPARATOR = new VersionComparator();

    public static final Comparator<ArtifactInfo> REPOSITORY_VERSION_COMPARATOR = new RepositoryVersionComparator();

    public String fname;

    public String fextension;

    public String groupId;

    public String artifactId;

    public String version;

    private transient ArtifactVersion artifactVersion;

    public String classifier;

    /**
     * Artifact packaging for the main artifact and extension for secondary artifact (no classifier)
     */
    public String packaging;

    public String name;

    public String description;

    public long lastModified = -1;

    public long size = -1;

    public String md5;

    public String sha1;

    public ArtifactAvailablility sourcesExists = ArtifactAvailablility.NOT_PRESENT;

    public ArtifactAvailablility javadocExists = ArtifactAvailablility.NOT_PRESENT;

    public ArtifactAvailablility signatureExists = ArtifactAvailablility.NOT_PRESENT;

    public String classNames;

    public String repository;

    public String path;

    public String remoteUrl;

    public String context;

    /**
     * Plugin goal prefix (only if packaging is "maven-plugin")
     */
    public String prefix;

    /**
     * Plugin goals (only if packaging is "maven-plugin")
     */
    public List<String> goals;

    private String uinfo = null;
    
    private final Map<String,String> attributes = new HashMap<String,String>();

    public ArtifactInfo()
    {
    }

    public ArtifactInfo( String repository, String groupId, String artifactId, String version, String classifier )
    {
        this.repository = repository;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
    }

    public ArtifactVersion getArtifactVersion()
    {
        if ( artifactVersion == null )
        {
            artifactVersion = new DefaultArtifactVersion( version );
        }
        return artifactVersion;
    }

    public String getUinfo() 
    {
        if( uinfo == null )
        {
            uinfo = new StringBuilder() //
            .append( groupId ).append( FS ) //
            .append( artifactId ).append( FS ) //
            .append( version ).append( FS ) //
            .append( nvl( classifier ) ) //
            .append( StringUtils.isEmpty( classifier ) || StringUtils.isEmpty( packaging ) ? "" : FS + packaging ) //
            .toString();  // extension is stored in the packaging field when classifier is not used
        }
        
        return uinfo;    
    }
    
    public String getRootGroup()
    {
        int n = groupId.indexOf( '.' );
        if ( n > -1 )
        {
            return groupId.substring( 0, n );
        }
        return groupId;
    }
    
    public Gav calculateGav() 
    {
        return new Gav( 
            groupId, 
            artifactId, 
            version, 
            classifier, 
            fextension,
            null,  // snapshotBuildNumber
            null,  // snapshotTimeStamp 
            fname,  // name
            VersionUtils.isSnapshot( version ),  // isSnapshot
            false,  // hash
            null,   // hashType
            false,  // signature
            null ); // signatureType
    }
    
    public Map<String, String> getAttributes()
    {
        return attributes;
    }
    
    @Override
    public String toString()
    {
        return new StringBuilder( groupId )
            .append( ':' ).append( artifactId )  //
            .append( ':' ).append( version )  //
            .append( ':' ).append( classifier ) //
            .append( ':' ).append( packaging ).toString();
    }

    // ----------------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------------

    public static String nvl( String v )
    {
        return v == null ? NA : v;
    }

    public static String renvl( String v )
    {
        return NA.equals( v ) ? null : v;
    }

    public static String lst2str( Collection<String> list )
    {
        StringBuilder sb = new StringBuilder();
        for ( String s : list )
        {
            sb.append( s ).append( ArtifactInfo.FS );
        }
        return sb.length()==0 ? sb.toString() : sb.substring( 0, sb.length() - 1 );
    }
    
    public static List<String> str2lst( String str )
    {
        return Arrays.asList( ArtifactInfo.FS_PATTERN.split( str ) );
    }

    /**
     * A version comparator
     */
    static class VersionComparator
        implements Comparator<ArtifactInfo>
    {
        @SuppressWarnings("unchecked")
        public int compare( ArtifactInfo f1, ArtifactInfo f2 )
        {
            int n = f1.groupId.compareTo( f2.groupId );
            if ( n != 0 )
            {
                return n;
            }

            n = f1.artifactId.compareTo( f2.artifactId );
            if ( n != 0 )
            {
                return n;
            }

            n = -f1.getArtifactVersion().compareTo( f2.getArtifactVersion() );
            if ( n != 0 )
            {
                return n;
            }

            {
                String c1 = f1.classifier;
                String c2 = f2.classifier;
                if ( c1 == null )
                {
                    if ( c2 != null )
                    {
                        return -1;
                    }
                }
                else
                {
                    if ( c2 == null )
                    {
                        return 1;
                    }

                    n = c1.compareTo( c2 );
                    if ( n != 0 )
                    {
                        return n;
                    }
                }
            }

            {
                String p1 = f1.packaging;
                String p2 = f2.packaging;
                if ( p1 == null )
                {
                    return p2 == null ? 0 : -1;
                }
                else
                {
                    return p2 == null ? 1 : p1.compareTo( p2 );
                }
            }
        }
    }

    /**
     * A repository and version comparator
     */
    static class RepositoryVersionComparator
        extends VersionComparator
    {
        @Override
        public int compare( ArtifactInfo f1, ArtifactInfo f2 )
        {
            int n = super.compare( f1, f2 );
            if ( n != 0 )
            {
                return n;
            }

            String r1 = f1.repository;
            String r2 = f2.repository;
            if ( r1 == null )
            {
                return r2 == null ? 0 : -1;
            }
            else
            {
                return r2 == null ? 1 : r1.compareTo( r2 );
            }
        }
    }

}
