package ies.belgrano.tracker_libros.entity;

import ies.belgrano.tracker_libros.enums.EstadoClubMiembro;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clubes_miembros", uniqueConstraints =
        @UniqueConstraint(columnNames = {"club_id", "usuario_id"}))
public class ClubMiembro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    public Club club;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoClubMiembro estado = EstadoClubMiembro.INVITADO;
    @Column(length = 20, nullable = false)
    public String rol = "MIEMBRO";
    @Column(name = "fecha_union", nullable = false)
    public LocalDateTime fechaUnion;

    @PrePersist
    void antesDeGuardar() { if (fechaUnion == null) fechaUnion = LocalDateTime.now(); }
}
