/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.registry;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = ContentClass.class, hint = RootContentClass.ID )
public class RootContentClass
    extends AbstractIdContentClass
{
    public static final String ID = "any";
    public static final String NAME = "Any Content";

    public String getId()
    {
        return ID;
    }
    
    @Override
    public String getName() 
    {
        return NAME;
    };
    
    @Override
    public boolean isCompatible( ContentClass contentClass )
    {
        //root is compatible with all !
        return true;
    }
    
    @Override
    public boolean isGroupable()
    {
        //you can't create repos w/ 'root' type content, so groupable isn't an option
        return false;
    }
}
