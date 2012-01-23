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
import org.sonatype.nexus.mock.components.Tree;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class RepoTargetForm
    extends Component
{

    private Button addButton;

    private Button cancel;

    private TextField name;

    private TextField pattern;

    private Tree patterns;

    private Button removeAllButton;

    private Button removeButton;

    private Combobox repositoryType;

    private Button save;

    public RepoTargetForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        this.name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        this.repositoryType = new Combobox( selenium, expression + ".find('name', 'contentClass')[0]" );
        this.pattern = new TextField( selenium, expression + ".find('name', 'pattern')[0]" );
        this.patterns = new Tree( selenium, expression + ".find('name', 'repoTargets-pattern-list')[0]" );

        this.addButton = new Button( selenium, "window.Ext.getCmp('button-add')" );
        this.removeButton = new Button( selenium, "window.Ext.getCmp('button-remove')" );
        this.removeAllButton = new Button( selenium, "window.Ext.getCmp('button-remove-all')" );

        this.save = new Button( selenium, "window.Ext.getCmp('savebutton')" );
        this.cancel = new Button( selenium, "window.Ext.getCmp('cancelbutton')" );
    }

    public final Button getAddButton()
    {
        return addButton;
    }

    public final Button getCancel()
    {
        return cancel;
    }

    public final TextField getName()
    {
        return name;
    }

    public final TextField getPattern()
    {
        return pattern;
    }

    public final Tree getPatterns()
    {
        return patterns;
    }

    public final Button getRemoveAllButton()
    {
        return removeAllButton;
    }

    public final Button getRemoveButton()
    {
        return removeButton;
    }

    public final Combobox getRepositoryType()
    {
        return repositoryType;
    }

    public final Button getSave()
    {
        return save;
    }

    public RepoTargetForm save()
    {
        save.click();

        new Window( selenium ).waitFor();

        return this;
    }

    public void addPattern( String pattern )
    {
        this.pattern.type( pattern );

        this.addButton.click();
    }

    public void cancel()
    {
        cancel.click();
    }

    public RepoTargetForm populate( String name, String repoType, String... patterns )
    {
        this.name.type( name );
        this.repositoryType.setValue( repoType );
        for ( String pattern : patterns )
        {
            this.pattern.type( pattern );

            this.addButton.click();
        }

        return this;
    }

    public void removePattern( String pattern )
    {
        patterns.select( pattern );

        removeButton.click();
    }

}
