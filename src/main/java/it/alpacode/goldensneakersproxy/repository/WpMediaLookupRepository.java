package it.alpacode.goldensneakersproxy.repository;

import it.alpacode.goldensneakersproxy.entity.wordpress.WpMediaLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WpMediaLookupRepository extends JpaRepository<WpMediaLookup, Long> {

    Optional<WpMediaLookup> findBySourceUrl(String sourceUrl);

    boolean existsBySourceUrl(String sourceUrl);
}
