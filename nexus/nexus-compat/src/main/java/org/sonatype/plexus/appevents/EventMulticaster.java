/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.plexus.appevents;

/**
 * The Interface EventMulticaster.
 */
public interface EventMulticaster
{
    /**
     * Adds the proximity event listener.
     * 
     * @param listener the listener
     */
    public void addEventListener( EventListener listener );

    /**
     * Removes the proximity event listener.
     * 
     * @param listener the listener
     */
    public void removeEventListener( EventListener listener );

    /**
     * Notify proximity event listeners.
     * 
     * @param evt the evt
     */
    public void notifyEventListeners( Event<?> evt );
}
