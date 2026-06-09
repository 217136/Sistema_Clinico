/**
 *
 * @author Amauri
 */
package com.faculdade.sistema.clinica.model;

import java.time.LocalDateTime;

/**
 * Representa uma evolução clínica individual de uma sessão de atendimento.
 */
public class Evolucao {
    private int id;
    private LocalDateTime dataHora;
    private String textoRegistro;
    private Psicologo psicologoResponsavel; // Associação direta (POO)

    public Evolucao(int id, LocalDateTime dataHora, String textoRegistro, Psicologo psicologoResponsavel) {
        this.id = id;
        this.dataHora = dataHora;
        this.textoRegistro = textoRegistro;
        this.psicologoResponsavel = psicologoResponsavel;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getTextoRegistro() { return textoRegistro; }
    public void setTextoRegistro(String textoRegistro) { this.textoRegistro = textoRegistro; }

    public Psicologo getPsicologoResponsavel() { return psicologoResponsavel; }
    public void setPsicologoResponsavel(Psicologo psicologoResponsavel) { this.psicologoResponsavel = psicologoResponsavel; }
}