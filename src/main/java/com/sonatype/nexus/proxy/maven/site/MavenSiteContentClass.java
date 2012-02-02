/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.maven.site;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * The Maven Site content class. It is not compatible with anything, hence it is not groupable.
 * 
 * @author cstamas
 */
@Component( role = ContentClass.class, hint = MavenSiteContentClass.ID )
public class MavenSiteContentClass
    extends AbstractIdContentClass
{
    public static final String ID = "maven-site";
    public static final String NAME = "Maven Site";

    public String getId()
    {
        return ID;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    public boolean isCompatible( ContentClass contentClass )
    {
        return false;
    }
    
    public boolean isGroupable()
    {
        return false;
    }
}
