package com.faculdade.sistema.clinica.dao;

/**
 *
 * @author Amauri
 */

import com.faculdade.sistema.clinica.database.Conexao;
import com.faculdade.sistema.clinica.model.Paciente;
import com.faculdade.sistema.clinica.model.Prontuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object para a tabela 'prontuarios'.
 */
public class ProntuarioDAO {

    /**
     * Busca o prontuário do paciente. Se não existir, cria um novo automaticamente.
     */
    public Prontuario buscarOuCriarPorPaciente(Paciente paciente) throws SQLException {
        String sqlBuscar = "SELECT id FROM prontuarios WHERE paciente_id = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sqlBuscar)) {
            stmt.setInt(1, paciente.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Prontuario(rs.getInt("id"), paciente);
                }
            }
        }

        // Se chegou aqui, o paciente ainda não tem prontuário. Vamos criar.
        String sqlInserir = "INSERT INTO prontuarios (paciente_id) VALUES (?)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sqlInserir, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, paciente.getId());
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Prontuario(rs.getInt(1), paciente);
                }
            }
        }
        
        throw new SQLException("Falha ao gerar o prontuário base para o paciente.");
    }
}
