/**
 *
 * @author Amauri
 */
package com.faculdade.sistema.clinica.model;

import java.time.LocalDate;

/**
 * Entidade que representa o paciente na clínica.
 * Todos os atributos são privados, garantindo o encapsulamento estrito.
 */
public class Paciente {
    private int id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String telefone;

    public Paciente(int id, String nome, String cpf, LocalDate dataNascimento, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    @Override
    public String toString() {
    // Sobrescrita que define como o objeto paciente se renderiza em componentes como o JComboBox
    return this.getNome() + " (CPF: " + this.getCpf() + ")";
    }
}
