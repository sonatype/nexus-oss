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
package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.TwinPanel;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class RouteForm
    extends Component
{

    private Button cancel;

    private Combobox repositoriesGroup;

    private TextField pattern;

    private TwinPanel repositoriesOrder;

    private Combobox ruleType;

    private Button save;

    public RouteForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        this.pattern = new TextField( selenium, expression + ".find('name', 'pattern')[0]" );
        this.ruleType = new Combobox( selenium, expression + ".find('name', 'ruleType')[0]" );
        this.repositoriesGroup = new Combobox( selenium, expression + ".find('name', 'groupId')[0]" );

        this.repositoriesOrder = new TwinPanel( selenium, expression + ".find('name', 'repositories')[0]" );

        this.save = new Button( selenium, "window.Ext.getCmp('savebutton')" );
        this.cancel = new Button( selenium, "window.Ext.getCmp('cancelbutton')" );
    }

    public final Button getCancel()
    {
        return cancel;
    }

    public final Combobox getRepositoriesGroup()
    {
        return repositoriesGroup;
    }

    public final TextField getPattern()
    {
        return pattern;
    }

    public final TwinPanel getRepositoriesOrder()
    {
        return repositoriesOrder;
    }

    public final Combobox getRuleType()
    {
        return ruleType;
    }

    public final Button getSave()
    {
        return save;
    }

    public RouteForm save()
    {
        save.click();

        new Window( selenium ).waitFor();

        return this;
    }

    public void cancel()
    {
        cancel.click();
    }

    public RouteForm populate( String pattern, String ruleType, String groupId, String... repos )
    {
        this.pattern.type( pattern );
        this.ruleType.setValue( ruleType );
        this.repositoriesGroup.setValue( groupId );

        for ( String repo : repos )
        {
            this.repositoriesOrder.add( repo );
        }

        return this;
    }

}
