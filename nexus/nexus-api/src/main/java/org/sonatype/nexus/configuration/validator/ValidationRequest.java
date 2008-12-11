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
package org.sonatype.nexus.configuration.validator;

/**
 * A request for validation, holding the configuration.
 * 
 * @author cstamas
 */
public class ValidationRequest
{
    /**
     * The configuration to validate.
     */
    private Object configuration;

    public ValidationRequest( Object configuration )
    {
        super();

        this.configuration = configuration;
    }

    public Object getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Object configuration )
    {
        this.configuration = configuration;
    }
}
