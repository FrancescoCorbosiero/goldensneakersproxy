package it.alpacode.goldensneakersproxy.repository;

import it.alpacode.goldensneakersproxy.entity.wordpress.WpBrandLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WpBrandLookupRepository extends JpaRepository<WpBrandLookup, Long> {

    Optional<WpBrandLookup> findByName(String name);

    Optional<WpBrandLookup> findBySlug(String slug);

    boolean existsByName(String name);
}
