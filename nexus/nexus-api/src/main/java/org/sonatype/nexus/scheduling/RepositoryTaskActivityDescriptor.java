package org.sonatype.nexus.scheduling;

import java.util.Set;

/**
 * An activity descriptor for tasks that are running against one or set of Repositories.
 * 
 * @author cstamas
 */
public interface RepositoryTaskActivityDescriptor
    extends TaskActivityDescriptor
{
    /**
     * Regarding repository content: create = creates new files/content in repository, update = modifies the content of
     * existing content, delete = removes content from repository.
     * 
     * @author cstamas
     */
    public enum ModificationOperator
    {
        create, update, delete
    };

    /**
     * Regarding repository attributes (they are 1:1 to content always): extend = creates new values in attributes (ie.
     * adds custom attributes), refresh = updates/freshens current attributes, lessen = removes existing attributes.
     * 
     * @author cstamas
     */
    public enum AttributesModificationOperator
    {
        extend, refresh, lessen
    };

    /**
     * Returns true if it will scan/walk repository.
     * 
     * @return
     */
    boolean isScanningRepository();

    /**
     * Will scan/walk the repository. Naturally, it implies READ operation happening against repository. If returned
     * null, it will not scan.
     */
    String getRepositoryScanningStartingPath();

    /**
     * Will apply these <b>modify</b> operations to the content of the repository.
     * 
     * @return
     */
    Set<ModificationOperator> getContentModificationOperators();

    /**
     * Will apply these attribute <b>modify</b> operations to the attributes of the repository.
     * 
     * @return
     */
    Set<AttributesModificationOperator> getAttributesModificationOperators();
}
