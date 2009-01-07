/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.artifact;

import org.sonatype.nexus.DefaultNexusEnforcer;
import org.sonatype.nexus.NexusEnforcer;

public class Gav
{
    public enum HashType
    {
        sha1, md5
    }

    public enum SignatureType
    {
        gpg;

        public String toString()
        {
            switch ( this )
            {
                case gpg:
                {
                    return "asc";
                }

                default:
                {
                    return "unknown-signature-type";
                }
            }
        }
    }

    private String groupId;

    private String artifactId;

    private String version;

    private String baseVersion;

    private String classifier;

    private String extension;

    private Integer snapshotBuildNumber;

    private Long snapshotTimeStamp;

    private String name;

    private boolean snapshot;

    private boolean hash;

    private HashType hashType;

    private boolean signature;

    private SignatureType signatureType;

    private NexusEnforcer enforcer = new DefaultNexusEnforcer();

    public Gav( String groupId, String artifactId, String version, String classifier, String extension,
        Integer snapshotBuildNumber, Long snapshotTimeStamp, String name, boolean snapshot, boolean hash,
        HashType hashType, boolean signature, SignatureType signatureType )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        if ( !snapshot )
        {
            this.baseVersion = null;
        }
        else
        {
            if ( !VersionUtils.isSnapshot( version ) )
            {
                throw new IllegalArgumentException( "GAV marked as snapshot but the supplied version '" + version
                    + "' is not!" );
            }

            if ( version.contains( "SNAPSHOT" ) )
            {
                // this is not a timestamped version
                this.baseVersion = null;
            }
            else
            {
                // this is a timestamped version (verified against pattern, see above)
                // we have XXXXXX-YYYYMMDD.HHMMSS-B
                // but XXXXXX may contain "-" too!

                if ( enforcer.isStrict() )
                {
                    this.baseVersion = version.substring( 0, version.lastIndexOf( '-' ) );
                    this.baseVersion = baseVersion.substring( 0, baseVersion.lastIndexOf( '-' ) ) + "-SNAPSHOT";
                }
                // also there may be no XXXXXX (i.e. when version is strictly named SNAPSHOT
                // BUT this is not the proper scheme, we will simply loosen up here if requested
                else
                {
                    String tempBaseVersion = version.substring( 0, version.lastIndexOf( '-' ) );
                    int baseVersionEndPos = tempBaseVersion.lastIndexOf( "-" );

                    if ( baseVersionEndPos >= 0 )
                    {
                        this.baseVersion = tempBaseVersion.substring( 0, baseVersionEndPos ) + "-SNAPSHOT";
                    }
                    else
                    {
                        this.baseVersion = "SNAPSHOT";
                    }
                }
            }
        }

        this.classifier = classifier;
        this.extension = extension;
        this.snapshotBuildNumber = snapshotBuildNumber;
        this.snapshotTimeStamp = snapshotTimeStamp;
        this.name = name;
        this.snapshot = snapshot;
        this.hash = hash;
        this.hashType = hashType;
        this.signature = signature;
        this.signatureType = signatureType;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getBaseVersion()
    {
        if ( baseVersion == null )
        {
            return getVersion();
        }
        else
        {
            return baseVersion;
        }
    }

    public String getClassifier()
    {
        return classifier;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getName()
    {
        return name;
    }

    public boolean isSnapshot()
    {
        return snapshot;
    }

    public Integer getSnapshotBuildNumber()
    {
        return snapshotBuildNumber;
    }

    public Long getSnapshotTimeStamp()
    {
        return snapshotTimeStamp;
    }

    public boolean isHash()
    {
        return hash;
    }

    public HashType getHashType()
    {
        return hashType;
    }

    public boolean isSignature()
    {
        return signature;
    }

    public SignatureType getSignatureType()
    {
        return signatureType;
    }

}
