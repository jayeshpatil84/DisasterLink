package com.disasterlink.repository;

/**
 * @deprecated Replaced by {@link SosBeaconRepository} in v2.0.
 * The original interface has been removed because DisasterReport is no longer
 * a JPA-managed entity. Spring Data JPA cannot create a repository for it.
 *
 * This stub class (NOT an interface) prevents Spring from scanning and
 * instantiating the old repository bean at startup.
 */
@Deprecated
@SuppressWarnings("all")
public final class DisasterReportRepository {
    private DisasterReportRepository() {}
}
