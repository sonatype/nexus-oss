/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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