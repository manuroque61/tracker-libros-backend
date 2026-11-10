package ies.belgrano.tracker_libros.entity;

import ies.belgrano.tracker_libros.enums.EstadoAmistad;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "amistades", uniqueConstraints =
        @UniqueConstraint(columnNames = {"solicitante_id", "destinatario_id"}))
public class Amistad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    public Usuario solicitante;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    public Usuario destinatario;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoAmistad estado = EstadoAmistad.PENDIENTE;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    public LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() { if (fechaCreacion == null) fechaCreacion = LocalDateTime.now(); }
}
