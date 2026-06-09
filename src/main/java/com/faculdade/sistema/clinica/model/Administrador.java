package com.faculdade.sistema.clinica.model;

/**
 *
 * @author Amauri
 */

/**
 * Entidade que representa o Gestor da Clínica.
 * Possui acesso global ao Backoffice (Gestão de Identidade) e Relatórios Gerenciais.
 */
public class Administrador extends Usuario {

    public Administrador(int id, String nome, String login, String senha) {
        super(id, nome, login, senha);
    }

    @Override
    public String getPerfil() {
        return "ADMINISTRADOR";
    }
}
