/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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