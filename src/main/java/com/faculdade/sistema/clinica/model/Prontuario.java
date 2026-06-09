/**
 *
 * @author Amauri
 */
package com.faculdade.sistema.clinica.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa o Prontuário Clínico do Paciente.
 * Centraliza o histórico de evolução médica aplicando conceitos rígidos de encapsulamento.
 */
public class Prontuario {
    private int id;
    private Paciente paciente; // Agregação: O prontuário pertence a um Paciente
    private List<Evolucao> historicoEvolucoes; // Composição: Evoluções pertencem ao Prontuário

    public Prontuario(int id, Paciente paciente) {
        this.id = id;
        this.paciente = paciente;
        this.historicoEvolucoes = new ArrayList<>();
    }

    /**
     * Adiciona uma nova evolução ao histórico do paciente.
     */
    public void adicionarEvolucao(Evolucao evolucao) {
        this.historicoEvolucoes.add(evolucao);
    }

    /**
     * Retorna uma lista não modificável do histórico.
     * Boa prática de POO: Impede que classes externas limpem ou alterem a lista original sem passar por métodos validados.
     */
    public List<Evolucao> getHistoricoEvolucoes() {
        return Collections.unmodifiableList(historicoEvolucoes);
    }

    // Getters e Setters remanescentes
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
}