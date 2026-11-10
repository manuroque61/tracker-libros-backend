package ies.belgrano.tracker_libros.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comunidad_mensajes")
public class MensajeComunidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    public Usuario autor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id")
    public Usuario destinatario;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    public Club club;
    @Column(nullable = false, length = 2000)
    public String texto;
    @Column(name = "fecha_envio", nullable = false, updatable = false)
    public LocalDateTime fechaEnvio;

    @PrePersist
    void antesDeGuardar() { if (fechaEnvio == null) fechaEnvio = LocalDateTime.now(); }
}
