package com.projekan.yudis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Import Validasi
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUser;

    @Column(unique = true, nullable = false)
    @NotEmpty(message = "Username wajib diisi")
    @Size(min = 4, max = 20, message = "Username minimal 4 karakter")
    private String username;

    @NotEmpty(message = "Password tidak boleh kosong")
    @Size(min = 6, message = "Password minimal 6 karakter")
    private String password;

    @NotEmpty(message = "Nama Lengkap harus diisi")
    private String namaLengkap;
    
    @NotEmpty(message = "Nomor HP wajib diisi")
    @Pattern(regexp = "^08\\d+$", message = "Nomor HP harus dimulai dengan 08 dan hanya berisi angka")
    @Size(min = 10, max = 15, message = "Nomor HP tidak valid (10-15 angka)")
    private String noHp; 

    @Enumerated(EnumType.STRING)
    private Role role; 

    private String authToken;

    public enum Role { ADMIN, USER }
}