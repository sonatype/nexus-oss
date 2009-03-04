package org.sonatype.nexus

import org.testng.*
import org.testng.annotations.*
import org.testng.internal.annotations.*
import org.sonatype.nexus.groovytest.plexus.*

public class RunFirst {

    @ObjectFactory
    public IObjectFactory createFactory() {
        return new PlexusObjectFactory();
    }
	
}