package ies.belgrano.tracker_libros.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_lectura")
public class SesionLectura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lectura_id", nullable = false)
    private Lectura lectura;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private Integer minutos;

    @Column(name = "paginas_leidas")
    private Integer paginasLeidas;

    @Column(name = "pagina_actual")
    private Integer paginaActual;

    @Column(length = 1000)
    private String nota;

    public SesionLectura() {
    }

    @PrePersist
    protected void antesDeGuardar() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lectura getLectura() {
        return lectura;
    }

    public void setLectura(Lectura lectura) {
        this.lectura = lectura;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getMinutos() {
        return minutos;
    }

    public void setMinutos(Integer minutos) {
        this.minutos = minutos;
    }

    public Integer getPaginasLeidas() {
        return paginasLeidas;
    }

    public void setPaginasLeidas(Integer paginasLeidas) {
        this.paginasLeidas = paginasLeidas;
    }

    public Integer getPaginaActual() {
        return paginaActual;
    }

    public void setPaginaActual(Integer paginaActual) {
        this.paginaActual = paginaActual;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
