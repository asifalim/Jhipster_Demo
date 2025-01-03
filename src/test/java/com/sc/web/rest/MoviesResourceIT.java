package com.sc.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sc.IntegrationTest;
import com.sc.domain.Movies;
import com.sc.repository.MoviesRepository;
import com.sc.service.criteria.MoviesCriteria;
import com.sc.service.dto.MoviesDTO;
import com.sc.service.mapper.MoviesMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link MoviesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MoviesResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final Double DEFAULT_RATING = 1D;
    private static final Double UPDATED_RATING = 2D;
    private static final Double SMALLER_RATING = 1D - 1D;

    private static final String ENTITY_API_URL = "/api/movies";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MoviesRepository moviesRepository;

    @Autowired
    private MoviesMapper moviesMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMoviesMockMvc;

    private Movies movies;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Movies createEntity(EntityManager em) {
        Movies movies = new Movies().name(DEFAULT_NAME).category(DEFAULT_CATEGORY).rating(DEFAULT_RATING);
        return movies;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Movies createUpdatedEntity(EntityManager em) {
        Movies movies = new Movies().name(UPDATED_NAME).category(UPDATED_CATEGORY).rating(UPDATED_RATING);
        return movies;
    }

    @BeforeEach
    public void initTest() {
        movies = createEntity(em);
    }

    @Test
    @Transactional
    void createMovies() throws Exception {
        int databaseSizeBeforeCreate = moviesRepository.findAll().size();
        // Create the Movies
        MoviesDTO moviesDTO = moviesMapper.toDto(movies);
        restMoviesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moviesDTO)))
            .andExpect(status().isCreated());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeCreate + 1);
        Movies testMovies = moviesList.get(moviesList.size() - 1);
        assertThat(testMovies.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testMovies.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testMovies.getRating()).isEqualTo(DEFAULT_RATING);
    }

    @Test
    @Transactional
    void createMoviesWithExistingId() throws Exception {
        // Create the Movies with an existing ID
        movies.setId(1L);
        MoviesDTO moviesDTO = moviesMapper.toDto(movies);

        int databaseSizeBeforeCreate = moviesRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMoviesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moviesDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllMovies() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList
        restMoviesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(movies.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].rating").value(hasItem(DEFAULT_RATING.doubleValue())));
    }

    @Test
    @Transactional
    void getMovies() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get the movies
        restMoviesMockMvc
            .perform(get(ENTITY_API_URL_ID, movies.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(movies.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.rating").value(DEFAULT_RATING.doubleValue()));
    }

    @Test
    @Transactional
    void getMoviesByIdFiltering() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        Long id = movies.getId();

        defaultMoviesShouldBeFound("id.equals=" + id);
        defaultMoviesShouldNotBeFound("id.notEquals=" + id);

        defaultMoviesShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultMoviesShouldNotBeFound("id.greaterThan=" + id);

        defaultMoviesShouldBeFound("id.lessThanOrEqual=" + id);
        defaultMoviesShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMoviesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where name equals to DEFAULT_NAME
        defaultMoviesShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the moviesList where name equals to UPDATED_NAME
        defaultMoviesShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllMoviesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where name in DEFAULT_NAME or UPDATED_NAME
        defaultMoviesShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the moviesList where name equals to UPDATED_NAME
        defaultMoviesShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllMoviesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where name is not null
        defaultMoviesShouldBeFound("name.specified=true");

        // Get all the moviesList where name is null
        defaultMoviesShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllMoviesByNameContainsSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where name contains DEFAULT_NAME
        defaultMoviesShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the moviesList where name contains UPDATED_NAME
        defaultMoviesShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllMoviesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where name does not contain DEFAULT_NAME
        defaultMoviesShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the moviesList where name does not contain UPDATED_NAME
        defaultMoviesShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllMoviesByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where category equals to DEFAULT_CATEGORY
        defaultMoviesShouldBeFound("category.equals=" + DEFAULT_CATEGORY);

        // Get all the moviesList where category equals to UPDATED_CATEGORY
        defaultMoviesShouldNotBeFound("category.equals=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllMoviesByCategoryIsInShouldWork() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where category in DEFAULT_CATEGORY or UPDATED_CATEGORY
        defaultMoviesShouldBeFound("category.in=" + DEFAULT_CATEGORY + "," + UPDATED_CATEGORY);

        // Get all the moviesList where category equals to UPDATED_CATEGORY
        defaultMoviesShouldNotBeFound("category.in=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllMoviesByCategoryIsNullOrNotNull() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where category is not null
        defaultMoviesShouldBeFound("category.specified=true");

        // Get all the moviesList where category is null
        defaultMoviesShouldNotBeFound("category.specified=false");
    }

    @Test
    @Transactional
    void getAllMoviesByCategoryContainsSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where category contains DEFAULT_CATEGORY
        defaultMoviesShouldBeFound("category.contains=" + DEFAULT_CATEGORY);

        // Get all the moviesList where category contains UPDATED_CATEGORY
        defaultMoviesShouldNotBeFound("category.contains=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllMoviesByCategoryNotContainsSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where category does not contain DEFAULT_CATEGORY
        defaultMoviesShouldNotBeFound("category.doesNotContain=" + DEFAULT_CATEGORY);

        // Get all the moviesList where category does not contain UPDATED_CATEGORY
        defaultMoviesShouldBeFound("category.doesNotContain=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllMoviesByRatingIsEqualToSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where rating equals to DEFAULT_RATING
        defaultMoviesShouldBeFound("rating.equals=" + DEFAULT_RATING);

        // Get all the moviesList where rating equals to UPDATED_RATING
        defaultMoviesShouldNotBeFound("rating.equals=" + UPDATED_RATING);
    }

    @Test
    @Transactional
    void getAllMoviesByRatingIsInShouldWork() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where rating in DEFAULT_RATING or UPDATED_RATING
        defaultMoviesShouldBeFound("rating.in=" + DEFAULT_RATING + "," + UPDATED_RATING);

        // Get all the moviesList where rating equals to UPDATED_RATING
        defaultMoviesShouldNotBeFound("rating.in=" + UPDATED_RATING);
    }

    @Test
    @Transactional
    void getAllMoviesByRatingIsNullOrNotNull() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where rating is not null
        defaultMoviesShouldBeFound("rating.specified=true");

        // Get all the moviesList where rating is null
        defaultMoviesShouldNotBeFound("rating.specified=false");
    }

    @Test
    @Transactional
    void getAllMoviesByRatingIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where rating is greater than or equal to DEFAULT_RATING
        defaultMoviesShouldBeFound("rating.greaterThanOrEqual=" + DEFAULT_RATING);

        // Get all the moviesList where rating is greater than or equal to UPDATED_RATING
        defaultMoviesShouldNotBeFound("rating.greaterThanOrEqual=" + UPDATED_RATING);
    }

    @Test
    @Transactional
    void getAllMoviesByRatingIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where rating is less than or equal to DEFAULT_RATING
        defaultMoviesShouldBeFound("rating.lessThanOrEqual=" + DEFAULT_RATING);

        // Get all the moviesList where rating is less than or equal to SMALLER_RATING
        defaultMoviesShouldNotBeFound("rating.lessThanOrEqual=" + SMALLER_RATING);
    }

    @Test
    @Transactional
    void getAllMoviesByRatingIsLessThanSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where rating is less than DEFAULT_RATING
        defaultMoviesShouldNotBeFound("rating.lessThan=" + DEFAULT_RATING);

        // Get all the moviesList where rating is less than UPDATED_RATING
        defaultMoviesShouldBeFound("rating.lessThan=" + UPDATED_RATING);
    }

    @Test
    @Transactional
    void getAllMoviesByRatingIsGreaterThanSomething() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        // Get all the moviesList where rating is greater than DEFAULT_RATING
        defaultMoviesShouldNotBeFound("rating.greaterThan=" + DEFAULT_RATING);

        // Get all the moviesList where rating is greater than SMALLER_RATING
        defaultMoviesShouldBeFound("rating.greaterThan=" + SMALLER_RATING);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMoviesShouldBeFound(String filter) throws Exception {
        restMoviesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(movies.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].rating").value(hasItem(DEFAULT_RATING.doubleValue())));

        // Check, that the count call also returns 1
        restMoviesMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMoviesShouldNotBeFound(String filter) throws Exception {
        restMoviesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMoviesMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMovies() throws Exception {
        // Get the movies
        restMoviesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMovies() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();

        // Update the movies
        Movies updatedMovies = moviesRepository.findById(movies.getId()).get();
        // Disconnect from session so that the updates on updatedMovies are not directly saved in db
        em.detach(updatedMovies);
        updatedMovies.name(UPDATED_NAME).category(UPDATED_CATEGORY).rating(UPDATED_RATING);
        MoviesDTO moviesDTO = moviesMapper.toDto(updatedMovies);

        restMoviesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, moviesDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(moviesDTO))
            )
            .andExpect(status().isOk());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
        Movies testMovies = moviesList.get(moviesList.size() - 1);
        assertThat(testMovies.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testMovies.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testMovies.getRating()).isEqualTo(UPDATED_RATING);
    }

    @Test
    @Transactional
    void putNonExistingMovies() throws Exception {
        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();
        movies.setId(count.incrementAndGet());

        // Create the Movies
        MoviesDTO moviesDTO = moviesMapper.toDto(movies);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMoviesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, moviesDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(moviesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMovies() throws Exception {
        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();
        movies.setId(count.incrementAndGet());

        // Create the Movies
        MoviesDTO moviesDTO = moviesMapper.toDto(movies);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMoviesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(moviesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMovies() throws Exception {
        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();
        movies.setId(count.incrementAndGet());

        // Create the Movies
        MoviesDTO moviesDTO = moviesMapper.toDto(movies);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMoviesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(moviesDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMoviesWithPatch() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();

        // Update the movies using partial update
        Movies partialUpdatedMovies = new Movies();
        partialUpdatedMovies.setId(movies.getId());

        partialUpdatedMovies.category(UPDATED_CATEGORY);

        restMoviesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMovies.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMovies))
            )
            .andExpect(status().isOk());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
        Movies testMovies = moviesList.get(moviesList.size() - 1);
        assertThat(testMovies.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testMovies.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testMovies.getRating()).isEqualTo(DEFAULT_RATING);
    }

    @Test
    @Transactional
    void fullUpdateMoviesWithPatch() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();

        // Update the movies using partial update
        Movies partialUpdatedMovies = new Movies();
        partialUpdatedMovies.setId(movies.getId());

        partialUpdatedMovies.name(UPDATED_NAME).category(UPDATED_CATEGORY).rating(UPDATED_RATING);

        restMoviesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMovies.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMovies))
            )
            .andExpect(status().isOk());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
        Movies testMovies = moviesList.get(moviesList.size() - 1);
        assertThat(testMovies.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testMovies.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testMovies.getRating()).isEqualTo(UPDATED_RATING);
    }

    @Test
    @Transactional
    void patchNonExistingMovies() throws Exception {
        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();
        movies.setId(count.incrementAndGet());

        // Create the Movies
        MoviesDTO moviesDTO = moviesMapper.toDto(movies);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMoviesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, moviesDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(moviesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMovies() throws Exception {
        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();
        movies.setId(count.incrementAndGet());

        // Create the Movies
        MoviesDTO moviesDTO = moviesMapper.toDto(movies);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMoviesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(moviesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMovies() throws Exception {
        int databaseSizeBeforeUpdate = moviesRepository.findAll().size();
        movies.setId(count.incrementAndGet());

        // Create the Movies
        MoviesDTO moviesDTO = moviesMapper.toDto(movies);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMoviesMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(moviesDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Movies in the database
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMovies() throws Exception {
        // Initialize the database
        moviesRepository.saveAndFlush(movies);

        int databaseSizeBeforeDelete = moviesRepository.findAll().size();

        // Delete the movies
        restMoviesMockMvc
            .perform(delete(ENTITY_API_URL_ID, movies.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Movies> moviesList = moviesRepository.findAll();
        assertThat(moviesList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
