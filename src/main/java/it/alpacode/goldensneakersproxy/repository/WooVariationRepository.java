package it.alpacode.goldensneakersproxy.repository;

import it.alpacode.goldensneakersproxy.entity.woocommerce.WooVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WooVariationRepository extends JpaRepository<WooVariation, Long> {

    List<WooVariation> findByProductId(Long productId);

    Page<WooVariation> findByProductId(Long productId, Pageable pageable);

    Optional<WooVariation> findBySku(String sku);

    List<WooVariation> findByStatus(String status);

    List<WooVariation> findByStockStatus(String stockStatus);

    @Query("SELECT v FROM WooVariation v WHERE v.productId = :productId AND v.status = :status")
    List<WooVariation> findByProductIdAndStatus(
            @Param("productId") Long productId,
            @Param("status") String status);

    @Query("SELECT v FROM WooVariation v WHERE v.productId = :productId AND v.stockStatus = :stockStatus")
    List<WooVariation> findByProductIdAndStockStatus(
            @Param("productId") Long productId,
            @Param("stockStatus") String stockStatus);

    long countByProductId(Long productId);

    long countByStatus(String status);

    void deleteByProductId(Long productId);
}
