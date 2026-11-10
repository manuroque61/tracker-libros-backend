package ies.belgrano.tracker_libros.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calificaciones_compartidas", uniqueConstraints =
        @UniqueConstraint(columnNames = {"usuario_id", "book_key"}))
public class CalificacionCompartida {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;
    @Column(name = "book_key", nullable = false, length = 512)
    public String bookKey;
    @Column(nullable = false, length = 200)
    public String titulo;
    @Column(nullable = false, length = 200)
    public String autor;
    @Column(name = "portada_url", length = 1000)
    public String portadaUrl;
    @Column(nullable = false)
    public Integer estrellas;
    @Column(length = 1000)
    public String comentario;
    @Column(name = "fecha_actualizacion", nullable = false)
    public LocalDateTime fechaActualizacion;

    @PrePersist @PreUpdate
    void antesDeGuardar() { fechaActualizacion = LocalDateTime.now(); }
}
