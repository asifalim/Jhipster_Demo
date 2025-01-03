package com.sc.web.rest;

import com.sc.domain.Movies;
import com.sc.repository.MoviesRepository;
import com.sc.service.MoviesQueryService;
import com.sc.service.MoviesService;
import com.sc.service.criteria.MoviesCriteria;
import com.sc.service.dto.MoviesDTO;
import com.sc.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.sc.domain.Movies}.
 */
@RestController
@RequestMapping("/api")
public class MoviesResource {

    private final Logger log = LoggerFactory.getLogger(MoviesResource.class);

    private static final String ENTITY_NAME = "movies";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MoviesService moviesService;

    private final MoviesRepository moviesRepository;

    private final MoviesQueryService moviesQueryService;

    public MoviesResource(MoviesService moviesService, MoviesRepository moviesRepository, MoviesQueryService moviesQueryService) {
        this.moviesService = moviesService;
        this.moviesRepository = moviesRepository;
        this.moviesQueryService = moviesQueryService;
    }

    //Added by AsifAlim

    @GetMapping("/movies/search")
    public List<Movies> searchMovies(@RequestParam String keyword) {
        return moviesService.searchByNameOrCategory(keyword);
    }

    /**
     * {@code POST  /movies} : Create a new movies.
     *
     * @param moviesDTO the moviesDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new moviesDTO, or with status {@code 400 (Bad Request)} if the movies has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/movies")
    public ResponseEntity<MoviesDTO> createMovies(@RequestBody MoviesDTO moviesDTO) throws URISyntaxException {
        log.debug("REST request to save Movies : {}", moviesDTO);
        if (moviesDTO.getId() != null) {
            throw new BadRequestAlertException("A new movies cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MoviesDTO result = moviesService.save(moviesDTO);
        return ResponseEntity
            .created(new URI("/api/movies/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /movies/:id} : Updates an existing movies.
     *
     * @param id the id of the moviesDTO to save.
     * @param moviesDTO the moviesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated moviesDTO,
     * or with status {@code 400 (Bad Request)} if the moviesDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the moviesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/movies/{id}")
    public ResponseEntity<MoviesDTO> updateMovies(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MoviesDTO moviesDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Movies : {}, {}", id, moviesDTO);
        if (moviesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, moviesDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!moviesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        MoviesDTO result = moviesService.update(moviesDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, moviesDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /movies/:id} : Partial updates given fields of an existing movies, field will ignore if it is null
     *
     * @param id the id of the moviesDTO to save.
     * @param moviesDTO the moviesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated moviesDTO,
     * or with status {@code 400 (Bad Request)} if the moviesDTO is not valid,
     * or with status {@code 404 (Not Found)} if the moviesDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the moviesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/movies/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MoviesDTO> partialUpdateMovies(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MoviesDTO moviesDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Movies partially : {}, {}", id, moviesDTO);
        if (moviesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, moviesDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!moviesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MoviesDTO> result = moviesService.partialUpdate(moviesDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, moviesDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /movies} : get all the movies.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of movies in body.
     */
    @GetMapping("/movies")
    public ResponseEntity<List<MoviesDTO>> getAllMovies(
        MoviesCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Movies by criteria: {}", criteria);
        Page<MoviesDTO> page = moviesQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /movies/count} : count all the movies.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/movies/count")
    public ResponseEntity<Long> countMovies(MoviesCriteria criteria) {
        log.debug("REST request to count Movies by criteria: {}", criteria);
        return ResponseEntity.ok().body(moviesQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /movies/:id} : get the "id" movies.
     *
     * @param id the id of the moviesDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the moviesDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/movies/{id}")
    public ResponseEntity<MoviesDTO> getMovies(@PathVariable Long id) {
        log.debug("REST request to get Movies : {}", id);
        Optional<MoviesDTO> moviesDTO = moviesService.findOne(id);
        return ResponseUtil.wrapOrNotFound(moviesDTO);
    }

    /**
     * {@code DELETE  /movies/:id} : delete the "id" movies.
     *
     * @param id the id of the moviesDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovies(@PathVariable Long id) {
        log.debug("REST request to delete Movies : {}", id);
        moviesService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
