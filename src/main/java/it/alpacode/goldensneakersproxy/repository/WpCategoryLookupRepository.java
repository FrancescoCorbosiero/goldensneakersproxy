package it.alpacode.goldensneakersproxy.repository;

import it.alpacode.goldensneakersproxy.entity.wordpress.WpCategoryLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WpCategoryLookupRepository extends JpaRepository<WpCategoryLookup, Long> {

    Optional<WpCategoryLookup> findByName(String name);

    Optional<WpCategoryLookup> findBySlug(String slug);

    boolean existsByName(String name);
}
