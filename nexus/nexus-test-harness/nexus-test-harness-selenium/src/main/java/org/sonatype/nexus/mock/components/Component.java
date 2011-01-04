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
package org.sonatype.nexus.mock.components;

import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.mock.util.ThreadUtils;

import com.thoughtworks.selenium.Selenium;

public class Component
{
    private Component parent;

    protected Selenium selenium;

    protected String expression;

    public String idFunction = ".getId()";

    public Component( Selenium selenium )
    {
        this.selenium = selenium;
    }

    /**
     * Makes a proxy for a root Ext component; selenium - Selenium proxy through which it can fire Selenese commands,
     * expression - JavaScript that evaluates to the Ext component.
     */
    public Component( Selenium selenium, String expression )
    {
        this.parent = null;
        this.selenium = selenium;
        this.expression = expression;
    }

    /**
     * Makes a proxy for an Ext component that is contained within another; parent - proxy for the container Ext
     * component, expression - JavaScript expression that evaluates this proxy's component on that of the container.
     */
    public Component( Component parent, String expression )
    {
        this.parent = parent;
        this.selenium = parent.selenium;
        this.expression = expression;
    }

    /**
     * Returns the ID of the Ext component, found with the proxy's JS expression. This is overridden in some subclasses
     * for where the expression to get the ID varies.
     */
    public String getId()
    {
        return selenium.getEval( this.getExpression() + idFunction );
    }

    /**
     * Returns an XPath to the Ext component, which contains the ID provided by getId()
     */
    public String getXPath()
    {
        return "//*[@id='" + getId() + "']";
    }

    /**
     * Returns the absolute expression that resolves this proxy's Ext component.
     */
    public String getExpression()
    {
        return ( parent != null ) ? parent.getExpression() + expression : expression;
    }

    protected void waitForEvalTrue( String expr )
    {
        String fullExpr = getExpression() + expr;

        waitEvalTrue( fullExpr );
    }

    protected void waitEvalTrue( String fullExpr )
    {
        for ( int second = 0;; second++ )
        {
            if ( second >= 15 )
            {
                throw new RuntimeException( "Timeout" );
            }

            try
            {
                if ( "true".equals( selenium.getEval( fullExpr ) ) )
                {
                    break;
                }
            }
            catch ( Exception e )
            {
                // ignore
            }

            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
                // ignore
            }
        }
    }

    protected String getEval( String expr )
    {
        String fullExpr = expression + expr;

        return selenium.getEval( fullExpr );
    }

    protected void runScript( String expr )
    {
        String fullExpr = expression + expr;

        selenium.runScript( fullExpr );
    }

    protected boolean evalTrue( String expr )
    {
        try
        {
            return "true".equals( getEval( expr ) );
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    public boolean hidden()
    {
        return evalTrue( " == null" ) || evalTrue( ".hidden" );
    }

    public boolean visible()
    {
        return !hidden();
    }

    public boolean isDisabled()
    {
        return evalTrue( ".disabled" );
    }

    public void waitForHidden()
    {
        boolean success = ThreadUtils.waitFor( new ThreadUtils.WaitCondition()
        {
            public boolean checkCondition( long elapsedTimeInMs )
            {
                return hidden();
            }
        }, TimeUnit.SECONDS, 15 );

        if ( !success )
        {
            throw new RuntimeException( "Timeout" );
        }
    }

    public boolean waitForVisible()
    {
        boolean success = ThreadUtils.waitFor( new ThreadUtils.WaitCondition()
        {
            public boolean checkCondition( long elapsedTimeInMs )
            {
                return visible();
            }
        }, TimeUnit.SECONDS, 15 );

        if ( !success )
        {
            throw new RuntimeException( "Timeout" );
        }

        return success;
    }

    public void waitToLoad()
    {
        waitForEvalTrue( ".disabled != true" );
    }

}
