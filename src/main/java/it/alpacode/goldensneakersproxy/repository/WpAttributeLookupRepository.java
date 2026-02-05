package it.alpacode.goldensneakersproxy.repository;

import it.alpacode.goldensneakersproxy.entity.wordpress.WpAttributeLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WpAttributeLookupRepository extends JpaRepository<WpAttributeLookup, Long> {

    Optional<WpAttributeLookup> findByName(String name);

    Optional<WpAttributeLookup> findBySlug(String slug);

    boolean existsByName(String name);
}
