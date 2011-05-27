/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * {@link ContentClass} representing OBR content.
 */
@Component( role = ContentClass.class, hint = ObrContentClass.ID )
public class ObrContentClass
    extends AbstractIdContentClass
{
    public static final String ID = "obr";
    
    public static final String NAME = "OBR";

    public String getId()
    {
        return ID;
    }
    
    @Override
    public String getName()
    {
        return NAME;
    }
}
