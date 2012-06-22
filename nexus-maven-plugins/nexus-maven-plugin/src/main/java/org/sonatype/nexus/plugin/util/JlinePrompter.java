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

import jline.ConsoleReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.util.List;

/**
 * JLine {@link Prompter}, quick work-around for issues with plexus-interactivity component wiring.
 * @since 2.1
 */
@Component(role=Prompter.class, hint="jline")
public class JlinePrompter
    implements Prompter
{
    @Requirement(hint="default")
    private Prompter delegate;

    @Override
    public String promptForPassword( final String message )
        throws PrompterException
    {
        try
        {
            ConsoleReader reader = new ConsoleReader();
            return reader.readLine( message, '*' );
        }
        catch ( Exception e )
        {
            throw new PrompterException( e.getMessage(), e );
        }
    }

    @Override
    public String prompt( String message )
        throws PrompterException
    {
        return delegate.prompt( message );
    }

    @Override
    public String prompt( String message, String defaultReply )
        throws PrompterException
    {
        return delegate.prompt( message, defaultReply );
    }

    @Override
    public String prompt( String message, List possibleValues )
        throws PrompterException
    {
        return delegate.prompt( message, possibleValues );
    }

    @Override
    public String prompt( String message, List possibleValues, String defaultReply )
        throws PrompterException
    {
        return delegate.prompt( message, possibleValues, defaultReply );
    }

    @Override
    public void showMessage( String message )
        throws PrompterException
    {
        delegate.showMessage( message );
    }
}
