package com.faculdade.sistema.clinica.model;

/**
 * Entidade abstrata que representa qualquer usuário autenticável no sistema.
 * Atualizada com diretrizes de segurança para interceção de primeiro acesso.
 */
public abstract class Usuario {
    protected int id;
    protected String nome;
    protected String login;
    protected String senha;
    protected boolean statusAtivo;
    protected boolean primeiroAcesso; // Mapeamento da nova coluna de segurança

    public Usuario(int id, String nome, String login, String senha) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.statusAtivo = true; 
        this.primeiroAcesso = true; // Padrão seguro por omissão
    }

    // Método abstrato para demonstrar polimorfismo
    public abstract String getPerfil();

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isStatusAtivo() { return statusAtivo; }
    public void setStatusAtivo(boolean statusAtivo) { this.statusAtivo = statusAtivo; }
    
    public boolean isPrimeiroAcesso() { return primeiroAcesso; }
    public void setPrimeiroAcesso(boolean primeiroAcesso) { this.primeiroAcesso = primeiroAcesso; }
}