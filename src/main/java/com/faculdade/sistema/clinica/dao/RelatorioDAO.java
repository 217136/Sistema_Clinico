package com.faculdade.sistema.clinica.dao;

/**
 *
 * @author Amauri
 */

import com.faculdade.sistema.clinica.database.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data Access Object focado em Business Intelligence (BI).
 * Extrai indicadores consolidados diretamente do motor do banco de dados.
 */
public class RelatorioDAO {

    /**
     * KPI 1: Avalia o absenteísmo e o volume geral de consultas na clínica.
     * Retorna um mapa contendo os status e as suas respetivas quantidades.
     */
    public Map<String, Integer> obterEstatisticasAgendamentos() throws SQLException {
        // LinkedHashMap garante que a ordem de inserção seja mantida na impressão do relatório
        Map<String, Integer> estatisticas = new LinkedHashMap<>();
        
        String sql = "SELECT status, COUNT(*) as total FROM agendamento GROUP BY status ORDER BY total DESC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int totalGeral = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                int quantidade = rs.getInt("total");
                
                estatisticas.put("Consultas com status '" + status + "'", quantidade);
                totalGeral += quantidade;
            }
            // Injeta o totalizador no final do mapa
            estatisticas.put("Total Geral de Movimentações", totalGeral);
        }
        return estatisticas;
    }

    /**
     * KPI 2: Avalia a produtividade individual do corpo clínico.
     * Relaciona a tabela de agendamentos com os utilizadores com perfil de Psicólogo.
     */
    public Map<String, Integer> obterProdutividadeProfissionais() throws SQLException {
        Map<String, Integer> produtividade = new LinkedHashMap<>();
        
        // INNER JOIN para buscar o nome do profissional e contar quantas vezes ele aparece na agenda
        String sql = "SELECT u.nome, COUNT(a.id) as total_atendimentos " +
                     "FROM agendamento a " +
                     "INNER JOIN usuarios u ON a.usuario_id = u.id " +
                     "WHERE u.perfil = 'PSICOLOGO_CLINICO' " +
                     "GROUP BY u.nome " +
                     "ORDER BY total_atendimentos DESC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nomeProfissional = rs.getString("nome");
                int total = rs.getInt("total_atendimentos");
                produtividade.put(nomeProfissional, total);
            }
        }
        return produtividade;
    }
}