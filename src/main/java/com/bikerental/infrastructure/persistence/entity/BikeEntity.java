package com.bikerental.infrastructure.persistence.entity;

import com.bikerental.domain.model.BikeStatus;
import com.bikerental.domain.model.BikeType;
import jakarta.persistence.*;

@Entity
@Table(name = "bikes")
public class BikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BikeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BikeStatus status;

    public BikeEntity() {}

    public BikeEntity(String code, BikeType type, BikeStatus status) {
        this.code = code;
        this.type = type;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public BikeType getType() { return type; }
    public void setType(BikeType type) { this.type = type; }

    public BikeStatus getStatus() { return status; }
    public void setStatus(BikeStatus status) { this.status = status; }
}
