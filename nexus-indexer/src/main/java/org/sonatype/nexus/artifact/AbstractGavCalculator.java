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

public abstract class AbstractGavCalculator
    implements GavCalculator
{
    private NexusEnforcer enforcer = new DefaultNexusEnforcer();
    
    protected NexusEnforcer getEnforcer()
    {
        return enforcer;
    }
}
