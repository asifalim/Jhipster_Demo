package com.sc.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.sc.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MoviesDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MoviesDTO.class);
        MoviesDTO moviesDTO1 = new MoviesDTO();
        moviesDTO1.setId(1L);
        MoviesDTO moviesDTO2 = new MoviesDTO();
        assertThat(moviesDTO1).isNotEqualTo(moviesDTO2);
        moviesDTO2.setId(moviesDTO1.getId());
        assertThat(moviesDTO1).isEqualTo(moviesDTO2);
        moviesDTO2.setId(2L);
        assertThat(moviesDTO1).isNotEqualTo(moviesDTO2);
        moviesDTO1.setId(null);
        assertThat(moviesDTO1).isNotEqualTo(moviesDTO2);
    }
}
