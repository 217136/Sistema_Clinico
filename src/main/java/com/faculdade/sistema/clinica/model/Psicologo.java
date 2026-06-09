/**
 *
 * @author Amauri
 */
package com.faculdade.sistema.clinica.model;

/**
 * Representa o profissional de Psicologia. Tem acesso total aos prontuários.
 */
public class Psicologo extends Usuario {
    private String crp;

    public Psicologo(int id, String nome, String login, String senha, String crp) {
        super(id, nome, login, senha);
        this.crp = crp;
    }

    @Override
    public String getPerfil() {
        return "PSICOLOGO_CLINICO";
    }

    public String getCrp() { return crp; }
    public void setCrp(String crp) { this.crp = crp; }
}
