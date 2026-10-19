package ies.belgrano.tracker_libros.entity;

import ies.belgrano.tracker_libros.enums.EstadoLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecturas")
public class Lectura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormatoLectura formato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLectura estado = EstadoLectura.PENDIENTE;

    @Column(name = "pagina_actual")
    private Integer paginaActual;

    @Column(name = "porcentaje_progreso")
    private Double porcentajeProgreso = 0.0;

    @Column(name = "ubicacion_digital", length = 1000)
    private String ubicacionDigital;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDate fechaFinalizacion;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    private LocalDateTime fechaAgregado;

    public Lectura() {
    }

    @PrePersist
    protected void antesDeGuardar() {
        if (fechaAgregado == null) {
            fechaAgregado = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoLectura.PENDIENTE;
        }

        if (porcentajeProgreso == null) {
            porcentajeProgreso = 0.0;
        }
    }

    //getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public FormatoLectura getFormato() {
        return formato;
    }

    public void setFormato(FormatoLectura formato) {
        this.formato = formato;
    }

    public EstadoLectura getEstado() {
        return estado;
    }

    public void setEstado(EstadoLectura estado) {
        this.estado = estado;
    }

    public Integer getPaginaActual() {
        return paginaActual;
    }

    public void setPaginaActual(Integer paginaActual) {
        this.paginaActual = paginaActual;
    }

    public Double getPorcentajeProgreso() {
        return porcentajeProgreso;
    }

    public void setPorcentajeProgreso(Double porcentajeProgreso) {
        this.porcentajeProgreso = porcentajeProgreso;
    }

    public String getUbicacionDigital() {
        return ubicacionDigital;
    }

    public void setUbicacionDigital(String ubicacionDigital) {
        this.ubicacionDigital = ubicacionDigital;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDate fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }

    

}
