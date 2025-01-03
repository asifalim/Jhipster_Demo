package com.sc.service;

import com.sc.domain.Movies;
import com.sc.repository.MoviesRepository;
import com.sc.service.dto.MoviesDTO;
import com.sc.service.mapper.MoviesMapper;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Movies}.
 */
@Service
@Transactional
public class MoviesService {

    private final Logger log = LoggerFactory.getLogger(MoviesService.class);

    private final MoviesRepository moviesRepository;

    private final MoviesMapper moviesMapper;

    public MoviesService(MoviesRepository moviesRepository, MoviesMapper moviesMapper) {
        this.moviesRepository = moviesRepository;
        this.moviesMapper = moviesMapper;
    }

    //Added by AsifAlim
    public List<Movies> searchByNameOrCategory(String keyword) {
        return moviesRepository.searchByNameOrCategory(keyword);
    }

    /**
     * Save a movies.
     *
     * @param moviesDTO the entity to save.
     * @return the persisted entity.
     */
    public MoviesDTO save(MoviesDTO moviesDTO) {
        log.debug("Request to save Movies : {}", moviesDTO);
        Movies movies = moviesMapper.toEntity(moviesDTO);
        movies = moviesRepository.save(movies);
        return moviesMapper.toDto(movies);
    }

    /**
     * Update a movies.
     *
     * @param moviesDTO the entity to save.
     * @return the persisted entity.
     */
    public MoviesDTO update(MoviesDTO moviesDTO) {
        log.debug("Request to update Movies : {}", moviesDTO);
        Movies movies = moviesMapper.toEntity(moviesDTO);
        movies = moviesRepository.save(movies);
        return moviesMapper.toDto(movies);
    }

    /**
     * Partially update a movies.
     *
     * @param moviesDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MoviesDTO> partialUpdate(MoviesDTO moviesDTO) {
        log.debug("Request to partially update Movies : {}", moviesDTO);

        return moviesRepository
            .findById(moviesDTO.getId())
            .map(existingMovies -> {
                moviesMapper.partialUpdate(existingMovies, moviesDTO);

                return existingMovies;
            })
            .map(moviesRepository::save)
            .map(moviesMapper::toDto);
    }

    /**
     * Get all the movies.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MoviesDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Movies");
        return moviesRepository.findAll(pageable).map(moviesMapper::toDto);
    }

    /**
     * Get one movies by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MoviesDTO> findOne(Long id) {
        log.debug("Request to get Movies : {}", id);
        return moviesRepository.findById(id).map(moviesMapper::toDto);
    }

    /**
     * Delete the movies by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Movies : {}", id);
        moviesRepository.deleteById(id);
    }
}
