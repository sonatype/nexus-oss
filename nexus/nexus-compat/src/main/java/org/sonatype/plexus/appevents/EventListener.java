/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.plexus.appevents;

/**
 * The listener interface for receiving events. The class that is interested in processing a event implements this
 * interface, and the object created with that class is registered with a component using the component's
 * <code>addEventListener<code> method. When
 * the  event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see AbstractEvent
 */
public interface EventListener
{

    /**
     * On event.
     * 
     * @param evt the evt
     */
    void onEvent( Event<?> evt );

}
