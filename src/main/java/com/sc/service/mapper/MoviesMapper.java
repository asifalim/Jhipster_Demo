package com.sc.service.mapper;

import com.sc.domain.Movies;
import com.sc.service.dto.MoviesDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Movies} and its DTO {@link MoviesDTO}.
 */
@Mapper(componentModel = "spring")
public interface MoviesMapper extends EntityMapper<MoviesDTO, Movies> {}
