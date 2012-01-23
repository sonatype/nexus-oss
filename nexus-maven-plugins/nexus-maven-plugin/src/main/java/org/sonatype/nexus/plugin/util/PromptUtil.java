/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
