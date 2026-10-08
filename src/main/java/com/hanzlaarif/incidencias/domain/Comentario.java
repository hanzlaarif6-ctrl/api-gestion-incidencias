package com.hanzlaarif.incidencias.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    protected Comentario() {
        // requerido por JPA
    }

    public Comentario(String texto, Incidencia incidencia, Usuario autor) {
        this.texto = texto;
        this.incidencia = incidencia;
        this.autor = autor;
    }

    @PrePersist
    void alCrear() {
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public Incidencia getIncidencia() {
        return incidencia;
    }

    public Usuario getAutor() {
        return autor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
