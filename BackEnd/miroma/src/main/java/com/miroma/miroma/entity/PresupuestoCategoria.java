package com.miroma.miroma.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "presupuesto_categoria")
public class PresupuestoCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "presupuesto_id", nullable = false)
    private Integer presupuestoId;

    @Column(name = "categoria_id", nullable = false)
    private Integer categoriaId;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    public PresupuestoCategoria() {
    }

    public PresupuestoCategoria(Integer presupuestoId, Integer categoriaId, BigDecimal porcentaje) {
        this.presupuestoId = presupuestoId;
        this.categoriaId = categoriaId;
        this.porcentaje = porcentaje;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(Integer presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }
}

