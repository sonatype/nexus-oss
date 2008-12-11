/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.model.CProps;

/**
 * A simple CProps to Map converter, to ease handling of CProps.
 * 
 * @author cstamas
 */
public class ModelloUtils
{
    @SuppressWarnings( "unchecked" )
    public static Map<String, String> getMapFromConfigList( List list )
    {
        Map<String, String> result = new HashMap<String, String>( list.size() );

        for ( Object obj : list )
        {
            CProps props = (CProps) obj;
            result.put( props.getKey(), props.getValue() );
        }

        return result;
    }
}
