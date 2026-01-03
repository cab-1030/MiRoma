package com.miroma.miroma.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "egreso_participaciones")
public class EgresoParticipacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "egreso_id", nullable = false)
    private Integer egresoId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "monto_asignado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoAsignado;

    public EgresoParticipacion() {
    }

    public EgresoParticipacion(Integer egresoId, Integer usuarioId, BigDecimal porcentaje, BigDecimal montoAsignado) {
        this.egresoId = egresoId;
        this.usuarioId = usuarioId;
        this.porcentaje = porcentaje;
        this.montoAsignado = montoAsignado;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEgresoId() {
        return egresoId;
    }

    public void setEgresoId(Integer egresoId) {
        this.egresoId = egresoId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public BigDecimal getMontoAsignado() {
        return montoAsignado;
    }

    public void setMontoAsignado(BigDecimal montoAsignado) {
        this.montoAsignado = montoAsignado;
    }
}

