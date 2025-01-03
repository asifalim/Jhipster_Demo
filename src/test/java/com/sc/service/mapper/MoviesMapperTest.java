package com.sc.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoviesMapperTest {

    private MoviesMapper moviesMapper;

    @BeforeEach
    public void setUp() {
        moviesMapper = new MoviesMapperImpl();
    }
}
