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
package org.sonatype.nexus.plugin.util;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

public class PromptUtil
{
    public static boolean booleanPrompt( final Prompter prompter, final CharSequence prompt, final Boolean defaultValue )
    {
        Boolean result = defaultValue;
        do
        {
            String txt = null;
            try
            {
                txt = prompter.prompt( prompt.toString() );
                if ( txt != null && txt.trim().length() > 0 )
                {
                    txt = txt.trim().toLowerCase();
                    if ( "y".equals( txt ) || "yes".equals( txt ) )
                    {
                        result = true;
                    }
                    else if ( "n".equals( txt ) || "no".equals( txt ) )
                    {
                        result = false;
                    }
                }
            }
            catch ( PrompterException e )
            {
                throw new IllegalStateException( "Prompt for input failed: " + e.getMessage(), e );
            }
        }
        while ( result == null );

        return result;
    }
}
