/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.plexus.appevents;

import javax.inject.Inject;

//TODO: dispose is gone now, need some other means to remove listener automagically
public abstract class AbstractApplicationEventListener
    implements EventListener
{
    @Inject
    public AbstractApplicationEventListener( ApplicationEventMulticaster applicationEventMulticaster )
    {
        applicationEventMulticaster.addEventListener( this );
    }
}
