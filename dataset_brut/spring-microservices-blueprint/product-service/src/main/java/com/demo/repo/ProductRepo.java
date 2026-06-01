package com.demo.repo;

import com.demo.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    List<Product> findAllByCreatorId(Long creatorId);

    @Query("SELECT p FROM Product p WHERE " +
           "(:id IS NULL OR p.id = :id) AND " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:description IS NULL OR p.description LIKE %:description%) AND " +
           "(:creatorId IS NULL OR p.creatorId = :creatorId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts(@Param("id") Long id,
                                @Param("name") String name,
                                @Param("description") String description,
                                @Param("creatorId") Long creatorId,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByCreatorId(Long creatorId);
}
