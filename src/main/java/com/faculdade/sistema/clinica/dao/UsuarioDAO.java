package com.faculdade.sistema.clinica.dao;

import com.faculdade.sistema.clinica.database.Conexao;
import com.faculdade.sistema.clinica.model.Usuario;
import com.faculdade.sistema.clinica.model.Psicologo;
import com.faculdade.sistema.clinica.model.Recepcionista;
import com.faculdade.sistema.clinica.util.SegurancaUtil;
import com.faculdade.sistema.clinica.model.Administrador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Concentra as operações de CRUD e autenticação para a entidade Usuario.
 * Garante o encerramento automático de recursos via try-with-resources.
 */
public class UsuarioDAO {

    /**
     * Insere um usuário (Psicólogo ou Recepcionista) no banco remoto.
     * Demonstra polimorfismo ao ler o método getPerfil().
     */
    public void inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, login, senha, crp, perfil) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            
            // INJEÇÃO DA CRIPTOGRAFIA: Converte a senha em texto puro para Hash irreversível
            stmt.setString(3, SegurancaUtil.gerarHash(usuario.getSenha()));

            // Regra polimórfica para o campo CRP
            if (usuario instanceof Psicologo) {
                stmt.setString(4, ((Psicologo) usuario).getCrp());
            } else {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            }

            stmt.setString(5, usuario.getPerfil());
            stmt.executeUpdate(); // Executa o comando de forma segura contra SQL Injection
        }
    }

    /**
     * Realiza a autenticação (Login) buscando as credenciais informadas.
     * Valida a criptografia SHA-256 e o status de exclusão lógica (status_ativo).
     */
    public Usuario autenticar(String login, String senha) throws SQLException {
        String sql = "SELECT id, nome, login, senha, crp, perfil, primeiro_acesso FROM usuarios WHERE login = ? AND senha = ? AND status_ativo = true";
        Usuario usuario = null;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, SegurancaUtil.gerarHash(senha));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String perfil = rs.getString("perfil");
                    int id = rs.getInt("id");
                    String nome = rs.getString("nome");
                    String senhaCriptografada = rs.getString("senha");

                    // Reconstrói o objeto de domínio correto com base no perfil do banco
                    if ("PSICOLOGO_CLINICO".equals(perfil)) {
                        usuario = new Psicologo(id, nome, login, senhaCriptografada, rs.getString("crp"));
                    } else if ("ADMINISTRADOR".equals(perfil)) {
                        usuario = new Administrador(id, nome, login, senhaCriptografada);
                    } else {
                        usuario = new Recepcionista(id, nome, login, senhaCriptografada);
                    }
                    
                    // CORREÇÃO: Mapeia a flag do banco de dados para a memória antes de retornar
                    usuario.setPrimeiroAcesso(rs.getBoolean("primeiro_acesso"));
                }
            }
        }
        return usuario; 
    }

    /**
     * READ: Lista todos os utilizadores do sistema para preenchimento da Data Table.
     */
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, login, senha, crp, perfil, status_ativo FROM usuarios ORDER BY nome";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String perfil = rs.getString("perfil");
                Usuario u;
                
                if ("PSICOLOGO_CLINICO".equals(perfil)) {
                    u = new Psicologo(rs.getInt("id"), rs.getString("nome"), rs.getString("login"), rs.getString("senha"), rs.getString("crp"));
                } else if ("ADMINISTRADOR".equals(perfil)) {
                    u = new Administrador(rs.getInt("id"), rs.getString("nome"), rs.getString("login"), rs.getString("senha"));
                } else {
                    u = new Recepcionista(rs.getInt("id"), rs.getString("nome"), rs.getString("login"), rs.getString("senha"));
                }
                
                u.setStatusAtivo(rs.getBoolean("status_ativo"));
                lista.add(u);
            }
        }
        return lista;
    }

    /**
     * UPDATE: Atualiza os dados cadastrais. A senha só é alterada se for fornecida uma nova.
     */
    public void atualizar(Usuario usuario, boolean atualizarSenha) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE usuarios SET nome = ?, login = ?, perfil = ?, crp = ?");
        if (atualizarSenha) {
            sql.append(", senha = ?");
        }
        sql.append(" WHERE id = ?");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getPerfil());
            
            if (usuario instanceof Psicologo) {
                stmt.setString(4, ((Psicologo) usuario).getCrp());
            } else {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            }

            int index = 5;
            if (atualizarSenha) {
                // Se a senha foi alterada, passa pelo Hash SHA-256 novamente
                stmt.setString(index++, SegurancaUtil.gerarHash(usuario.getSenha()));
            }
            stmt.setInt(index, usuario.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * DELETE (Lógico): Revoga o acesso do utilizador sem quebrar o histórico clínico.
     */
    public void inativar(int id, boolean tornarAtivo) throws SQLException {
        String sql = "UPDATE usuarios SET status_ativo = ? WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, tornarAtivo);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    /**
     * USADO PELO FUNCIONÁRIO: Quando ele próprio altera a sua senha na TelaAlterarSenha.
     * Guarda a nova senha e DESLIGA a trava de primeiro acesso (false).
     */
    public void atualizarSenhaPessoal(int idUsuario, String novaSenha) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ?, primeiro_acesso = false WHERE id = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, SegurancaUtil.gerarHash(novaSenha));
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();
        }
    }

    /**
     * USADO PELO ADMINISTRADOR: Quando ele clica no botão amarelo na TelaListagemUsuario.
     * Define a senha como "mudar123" e LIGA a trava de primeiro acesso (true).
     */
    public void redefinirSenhaAdmin(int idUsuario, String senhaTemporaria) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ?, primeiro_acesso = true WHERE id = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, SegurancaUtil.gerarHash(senhaTemporaria));
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();
        }
    }
}