package org.sonatype.nexus.client.core.subsystem.repository;

/**
 * Base class for hosted/proxy repositories.
 *
 * @since 2.5
 */
interface BaseRepository<T extends Repository, S extends RepositoryStatus>
    extends Repository<T, S>
{

    /**
     * Enable browsing (see content of repository)
     *
     * @return itself, for fluent api usage
     */
    T enableBrowsing();

    /**
     * Disable browsing (see content of repository).
     *
     * @return itself, for fluent api usage
     */
    T disableBrowsing();

    /**
     * @return {@code true} if browsing is allowed for this repository, {@code false} otherwise.
     */
    boolean isBrowsable();
}
