package org.sonatype.nexus.gwt.ui.client.repository;

/**
 *
 * @author barath
 */
public class HostedRepositoryPage extends RepositoryPage {

    public HostedRepositoryPage() {
        super("hosted");
    }

    protected void addTypeSpecificInputs() {
        addCommonInputs();
    }

}
