/**
 *
 * @author Amauri
 */
package com.faculdade.sistema.clinica.dao;

import com.faculdade.sistema.clinica.database.Conexao;
import com.faculdade.sistema.clinica.model.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia o CRUD da entidade Paciente no Supabase.
 * Garante a criação automática do Prontuário vinculado via transação JDBC.
 */
public class PacienteDAO {

    /**
     * Insere um paciente e cria seu prontuário na mesma transação.
     * Aplica o conceito de rollback em caso de falha de infraestrutura.
     */
    public void inserir(Paciente paciente) throws SQLException {
        String sqlPaciente = "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone) VALUES (?, ?, ?, ?)";
        String sqlProntuario = "INSERT INTO prontuarios (paciente_id) VALUES (?)";

        Connection conn = null;
        PreparedStatement stmtPaciente = null;
        PreparedStatement stmtProntuario = null;
        ResultSet rsKeys = null;

        try {
            conn = Conexao.conectar();
            conn.setAutoCommit(false); // INICIA A TRANSAÇÃO (Garante atomicidade do MVP)

            // 1. Inserir o Paciente pedindo o ID gerado de volta
            stmtPaciente = conn.prepareStatement(sqlPaciente, Statement.RETURN_GENERATED_KEYS);
            stmtPaciente.setString(1, paciente.getNome());
            stmtPaciente.setString(2, paciente.getCpf());
            stmtPaciente.setDate(3, java.sql.Date.valueOf(paciente.getDataNascimento()));
            stmtPaciente.setString(4, paciente.getTelefone());
            stmtPaciente.executeUpdate();

            // Recupera o ID gerado pelo IDENTITY do Supabase
            rsKeys = stmtPaciente.getGeneratedKeys();
            long idPacienteGerado = -1;
            if (rsKeys.next()) {
                idPacienteGerado = rsKeys.getLong(1);
            } else {
                throw new SQLException("Falha ao obter o ID do paciente gerado pelo banco.");
            }

            // 2. Criar automaticamente o Prontuário Clínico usando o ID recuperado
            stmtProntuario = conn.prepareStatement(sqlProntuario);
            stmtProntuario.setLong(1, idPacienteGerado);
            stmtProntuario.executeUpdate();

            conn.commit(); // CONFIRMA AS DUAS INSERÇÕES NO BANCO REMOTO
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // DESFAZ TUDO se houver qualquer erro (Evita dados órfãos)
            }
            throw e; // Propaga a exceção para tratamento visual futuro [cite: 239]
        } finally {
            // Fechamento manual e seguro dos recursos (necessário por não usar try-with-resources aninhado aqui)
            if (rsKeys != null) rsKeys.close();
            if (stmtProntuario != null) stmtProntuario.close();
            if (stmtPaciente != null) stmtPaciente.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Recupera todos os pacientes cadastrados para preenchimento de tabelas visuais.
     */
    public List<Paciente> listar() throws SQLException {
        String sql = "SELECT id, nome, cpf, data_nascimento, telefone FROM pacientes ORDER BY nome";
        List<Paciente> lista = new ArrayList<>();

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Paciente p = new Paciente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("cpf"),
                    rs.getDate("data_nascimento").toLocalDate(),
                    rs.getString("telefone")
                );
                lista.add(p);
            }
        }
        return lista;
    }
    /**
     * Atualiza os dados cadastrais do paciente.
     * O CPF não é alterado para garantir a integridade do histórico clínico.
     */
    public void atualizar(Paciente paciente) throws SQLException {
        String sql = "UPDATE pacientes SET nome = ?, data_nascimento = ?, telefone = ? WHERE id = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, paciente.getNome());
            stmt.setDate(2, java.sql.Date.valueOf(paciente.getDataNascimento()));
            stmt.setString(3, paciente.getTelefone());
            stmt.setInt(4, paciente.getId());
            
            stmt.executeUpdate();
        }
    }
}