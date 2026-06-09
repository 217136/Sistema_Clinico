/**
 *
 * @author Amauri
 */
package com.faculdade.sistema.clinica.model;

/**
 * Representa o profissional da recepção. Gerencia agenda e cadastros base.
 */
public class Recepcionista extends Usuario {

    public Recepcionista(int id, String nome, String login, String senha) {
        super(id, nome, login, senha);
    }

    @Override
    public String getPerfil() {
        return "RECEPCAO_ADMINISTRATIVA";
    }
}