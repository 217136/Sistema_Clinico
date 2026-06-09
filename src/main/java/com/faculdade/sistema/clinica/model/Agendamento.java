/**
 *
 * @author Amauri
 */
package com.faculdade.sistema.clinica.model;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma consulta agendada na clínica.
 * Demonstra forte associação entre as classes do modelo.
 */
public class Agendamento {
    private int id;
    private LocalDateTime dataHora;
    private String status; // "AGENDADO", "REALIZADO", "CANCELADO"
    private Paciente paciente; // Associação: O agendamento precisa de um Paciente
    private Usuario operador;   // Agregação: Quem realizou o agendamento (Psicologo ou Recepcionista)

    public Agendamento(int id, LocalDateTime dataHora, String status, Paciente paciente, Usuario operador) {
        this.id = id;
        this.dataHora = dataHora;
        this.status = status;
        this.paciente = paciente;
        this.operador = operador;
    }

    // Getters e Setters (Encapsulamento)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Usuario getOperador() { return operador; }
    public void setOperador(Usuario operador) { this.operador = operador; }
}