/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.events;

/**
 * The listener interface for receiving proximityEvent events.
 * The class that is interested in processing a proximityEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addProximityEventListener<code> method. When
 * the proximityEvent event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see AbstractEvent
 */
public interface EventListener
{

    /**
     * On proximity event.
     * 
     * @param evt the evt
     */
    void onProximityEvent( AbstractEvent evt );

}
