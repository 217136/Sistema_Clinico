package com.faculdade.sistema.clinica.dao;

import com.faculdade.sistema.clinica.database.Conexao;
import com.faculdade.sistema.clinica.model.Evolucao;
import com.faculdade.sistema.clinica.model.Psicologo;
import com.faculdade.sistema.clinica.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para a tabela 'evolucoes'.
 * Implementa Isolamento de Dados para garantir o sigilo médico-paciente.
 */
public class EvolucaoDAO {

    public void inserir(Evolucao evolucao, int prontuarioId) throws SQLException {
        String sql = "INSERT INTO evolucoes (prontuario_id, data_hora, texto_registro, usuario_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, prontuarioId);
            stmt.setTimestamp(2, Timestamp.valueOf(evolucao.getDataHora()));
            stmt.setString(3, evolucao.getTextoRegistro());
            stmt.setInt(4, evolucao.getPsicologoResponsavel().getId());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Busca o histórico clínico do paciente aplicando Isolamento de Dados (Row-Level Security).
     * Psicólogos só recebem as suas próprias evoluções. Gestores podem ter visão global (Auditoria).
     */
    public List<Evolucao> buscarPorProntuario(int prontuarioId, Usuario usuarioLogado) throws SQLException {
        List<Evolucao> lista = new ArrayList<>();
        boolean isPsicologo = usuarioLogado instanceof Psicologo;
        
        // Adicionamos 'u.crp' ao SELECT para garantir que o PDF Gerencial saia com a identificação correta
        String sql = "SELECT e.id, e.data_hora, e.texto_registro, e.usuario_id, u.nome, u.crp " +
                     "FROM evolucoes e INNER JOIN usuarios u ON e.usuario_id = u.id " +
                     "WHERE e.prontuario_id = ?";

        // Cláusula de Sigilo Profissional
        if (isPsicologo) {
            sql += " AND e.usuario_id = ?";
        }
        
        sql += " ORDER BY e.data_hora DESC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, prontuarioId);
            
            if (isPsicologo) {
                stmt.setInt(2, usuarioLogado.getId());
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Monta o objeto Psicologo com o CRP correto vindo do banco para suportar a exportação PDF
                    Psicologo psicologo = new Psicologo(
                            rs.getInt("usuario_id"), 
                            rs.getString("nome"), 
                            "", "", 
                            rs.getString("crp")
                    );
                    
                    Evolucao ev = new Evolucao(
                            rs.getInt("id"),
                            rs.getTimestamp("data_hora").toLocalDateTime(),
                            rs.getString("texto_registro"),
                            psicologo
                    );
                    lista.add(ev);
                }
            }
        }
        return lista;
    }
}