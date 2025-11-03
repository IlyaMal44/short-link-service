package com.promoit.shortLink.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Data
@Entity

public class UserEntity {
    @Id
    private String id;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<LinkEntity> links = new ArrayList<>();

    public UserEntity() {
        this.id = UUID.randomUUID().toString();
    }
}