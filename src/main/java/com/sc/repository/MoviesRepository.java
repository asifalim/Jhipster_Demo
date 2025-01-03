package com.sc.repository;

import com.sc.domain.Movies;
import java.awt.print.Pageable;
import java.util.List;
import org.h2.mvstore.Page;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Movies entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MoviesRepository extends JpaRepository<Movies, Long>, JpaSpecificationExecutor<Movies> {
    @Query(
        "SELECT m FROM Movies m WHERE LOWER(m.name) LIKE LOWER(CONCAT(:keyword, '%')) " +
        "OR LOWER(m.category) LIKE LOWER(CONCAT(:keyword, '%'))"
    )
    List<Movies> searchByNameOrCategory(@Param("keyword") String keyword);
}
