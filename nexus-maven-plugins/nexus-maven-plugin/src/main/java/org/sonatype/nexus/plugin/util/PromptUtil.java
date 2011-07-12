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
