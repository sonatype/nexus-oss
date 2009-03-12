package org.sonatype.nexus.groovytest;

import org.apache.maven.mercury.artifact.version.ArtifactVersion;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.aspectj.lang.reflect.MethodSignature;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public aspect VersionCheck
{

    @SuppressWarnings( "unchecked" )
    void around(): execution(@Test * *(..) ){
        MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
        NexusCompatibility compatibility = signature.getMethod().getAnnotation( NexusCompatibility.class );
        if ( compatibility == null )
        {
            Assert.fail( "Test case does not have @NexusCompatibility annotation!" );
        }

        ArtifactVersion version = new DefaultArtifactVersion( MavenProperties.NEXUS_VERSION );

        ArtifactVersion minVersion = null;
        if ( !"".equals( compatibility.minVersion() ) )
        {
            minVersion = new DefaultArtifactVersion( compatibility.minVersion() );
        }
        ArtifactVersion maxVersion = null;
        if ( !"".equals( compatibility.maxVersion() ) )
        {
            maxVersion = new DefaultArtifactVersion( compatibility.maxVersion() );
        }

        if ( minVersion == null && maxVersion == null )
        {
            System.out.println( "Running test: " + thisJoinPoint );
            proceed();
        }
        else if ( minVersion == null && version.compareTo( maxVersion ) <= 0 )
        {
            System.out.println( "Running test: " + thisJoinPoint );
            proceed();
        }
        else if ( maxVersion == null && version.compareTo( minVersion ) >= 0 )
        {
            System.out.println( "Running test: " + thisJoinPoint );
            proceed();
        }
        else if ( minVersion != null && maxVersion != null && version.compareTo( maxVersion ) <= 0
            && version.compareTo( minVersion ) >= 0 )
        {
            System.out.println( "Running test: " + thisJoinPoint );
            proceed();
        }
        else
        {
            throw new SkipException( "Invalid version range" );
        }
    }
}