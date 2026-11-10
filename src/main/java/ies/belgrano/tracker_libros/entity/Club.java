package ies.belgrano.tracker_libros.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clubes")
public class Club {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, length = 100)
    public String nombre;
    @Column(length = 500)
    public String descripcion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creador_id", nullable = false)
    public Usuario creador;
    @Column(name = "es_publico", nullable = false, columnDefinition = "boolean not null default false")
    public boolean esPublico;
    @Column(name = "book_key", length = 512)
    public String bookKey;
    @Column(name = "titulo_libro", length = 200)
    public String tituloLibro;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    public LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() { if (fechaCreacion == null) fechaCreacion = LocalDateTime.now(); }
}
