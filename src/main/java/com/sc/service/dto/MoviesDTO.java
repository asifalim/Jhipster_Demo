package com.sc.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.sc.domain.Movies} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MoviesDTO implements Serializable {

    private Long id;

    private String name;

    private String category;

    private Double rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoviesDTO)) {
            return false;
        }

        MoviesDTO moviesDTO = (MoviesDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, moviesDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MoviesDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", category='" + getCategory() + "'" +
            ", rating=" + getRating() +
            "}";
    }
}
