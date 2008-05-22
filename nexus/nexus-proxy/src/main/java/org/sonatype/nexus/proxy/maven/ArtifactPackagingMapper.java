package org.sonatype.nexus.proxy.maven;

/**
 * A utility component that resolves POM packaging to artifact extension. Different implementations may provide
 * different means to do it.
 * 
 * @author cstamas
 */
public interface ArtifactPackagingMapper
{
    String ROLE = ArtifactPackagingMapper.class.getName();

    String getExtensionForPackaging( String packaging );
}
