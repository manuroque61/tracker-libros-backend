package ies.belgrano.tracker_libros.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clubes_comentarios_lectura")
public class ComentarioClub {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    public Club club;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id")
    public ComentarioClub tema;
    @Column(name = "book_key", nullable = false, length = 512)
    public String bookKey;
    @Column(nullable = false, length = 200)
    public String tituloLibro;
    @Column(nullable = false, length = 2000)
    public String texto;
    @Column(name = "porcentaje_posicion", nullable = false)
    public Double porcentajePosicion;
    @Column(length = 1000)
    public String ubicacion;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    public LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() { if (fechaCreacion == null) fechaCreacion = LocalDateTime.now(); }
}
