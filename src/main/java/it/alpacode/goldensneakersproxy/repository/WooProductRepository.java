package it.alpacode.goldensneakersproxy.repository;

import it.alpacode.goldensneakersproxy.entity.woocommerce.WooProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WooProductRepository extends JpaRepository<WooProduct, Long> {

    Optional<WooProduct> findBySku(String sku);

    Optional<WooProduct> findBySlug(String slug);

    List<WooProduct> findByType(String type);

    List<WooProduct> findByStatus(String status);

    Page<WooProduct> findByStatus(String status, Pageable pageable);

    List<WooProduct> findByFeatured(Boolean featured);

    @Query("SELECT p FROM WooProduct p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<WooProduct> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p FROM WooProduct p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<WooProduct> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<WooProduct> findByStockStatus(String stockStatus);

    @Query("SELECT p FROM WooProduct p JOIN p.categories c WHERE c.id = :categoryId")
    List<WooProduct> findByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT p FROM WooProduct p JOIN p.tags t WHERE t.id = :tagId")
    List<WooProduct> findByTagId(@Param("tagId") Integer tagId);

    List<WooProduct> findByParentId(Integer parentId);

    @Query("SELECT DISTINCT p.type FROM WooProduct p")
    List<String> findAllProductTypes();

    @Query("SELECT DISTINCT p.status FROM WooProduct p")
    List<String> findAllStatuses();

    long countByStatus(String status);

    long countByType(String type);
}
