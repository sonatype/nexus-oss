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
package org.sonatype.nexus;

/**
 * The Default Nexus Enforcer.  At some piont this can all be config based, but for now
 * we are just always using strict mode of false
 * 
 * @plexus.component
 */
public class DefaultNexusEnforcer
    implements NexusEnforcer
{
    // This parameter will let nexus slip outside the lines if need be.
    // To maintain a strict versioning regimen (for example) set to true.
    private boolean strictMode = false;
    
    public boolean isStrict()
    {
        return strictMode;
    }
}