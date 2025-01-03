package com.sc.service;

import com.sc.domain.*; // for static metamodels
import com.sc.domain.Movies;
import com.sc.repository.MoviesRepository;
import com.sc.service.criteria.MoviesCriteria;
import com.sc.service.dto.MoviesDTO;
import com.sc.service.mapper.MoviesMapper;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Movies} entities in the database.
 * The main input is a {@link MoviesCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link MoviesDTO} or a {@link Page} of {@link MoviesDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MoviesQueryService extends QueryService<Movies> {

    private final Logger log = LoggerFactory.getLogger(MoviesQueryService.class);

    private final MoviesRepository moviesRepository;

    private final MoviesMapper moviesMapper;

    public MoviesQueryService(MoviesRepository moviesRepository, MoviesMapper moviesMapper) {
        this.moviesRepository = moviesRepository;
        this.moviesMapper = moviesMapper;
    }

    /**
     * Return a {@link List} of {@link MoviesDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<MoviesDTO> findByCriteria(MoviesCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Movies> specification = createSpecification(criteria);
        return moviesMapper.toDto(moviesRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link MoviesDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<MoviesDTO> findByCriteria(MoviesCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Movies> specification = createSpecification(criteria);
        return moviesRepository.findAll(specification, page).map(moviesMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MoviesCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Movies> specification = createSpecification(criteria);
        return moviesRepository.count(specification);
    }

    /**
     * Function to convert {@link MoviesCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Movies> createSpecification(MoviesCriteria criteria) {
        Specification<Movies> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Movies_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Movies_.name));
            }
            if (criteria.getCategory() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCategory(), Movies_.category));
            }
            if (criteria.getRating() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRating(), Movies_.rating));
            }
        }
        return specification;
    }
}
