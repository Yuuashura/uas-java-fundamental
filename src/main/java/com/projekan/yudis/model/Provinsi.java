package com.projekan.yudis.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "provinsi")
public class Provinsi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProvinsi;

    @Column(nullable = false)
    private String namaProvinsi;

    @Column(nullable = false)
    private Integer hargaOngkir;
}