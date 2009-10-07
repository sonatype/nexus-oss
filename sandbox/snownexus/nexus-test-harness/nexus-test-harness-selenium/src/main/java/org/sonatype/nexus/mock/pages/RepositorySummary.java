package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextArea;

import com.thoughtworks.selenium.Selenium;

public class RepositorySummary
    extends Component
{

    private TextArea distributionManagement;

    private TextArea repositoryInformation;

    public RepositorySummary( Selenium selenium, String expression )
    {
        super( selenium, expression );

        this.repositoryInformation = new TextArea( selenium, expression + ".find('name', 'informationField')[0]" );
        this.distributionManagement = new TextArea( selenium, expression + ".find('name', 'distMgmtField')[0]" );
    }

    public final TextArea getDistributionManagement()
    {
        return distributionManagement;
    }

    public final TextArea getRepositoryInformation()
    {
        return repositoryInformation;
    }

}
