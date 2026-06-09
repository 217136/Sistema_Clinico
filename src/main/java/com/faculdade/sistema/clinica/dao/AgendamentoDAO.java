package com.faculdade.sistema.clinica.dao;

import com.faculdade.sistema.clinica.database.Conexao;
import com.faculdade.sistema.clinica.model.Agendamento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por gerenciar a persistência da agenda médica.
 */
public class AgendamentoDAO {

    /**
     * Insere um novo agendamento no Supabase.
     */
    public void inserir(Agendamento agendamento) throws SQLException {
        String sql = "INSERT INTO agendamento (data_hora, status, paciente_id, usuario_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(agendamento.getDataHora()));
            stmt.setString(2, agendamento.getStatus());
            stmt.setInt(3, agendamento.getPaciente().getId());
            stmt.setInt(4, agendamento.getOperador().getId());
            
            stmt.executeUpdate();
            System.out.println("LOG: Agendamento inserido com sucesso!");
        }
    }

    /**
     * Trava reativa de segurança (Mantida como dupla camada de proteção).
     */
    public boolean isHorarioOcupado(LocalDateTime dataHora) throws SQLException {
        String sql = "SELECT COUNT(*) FROM agendamento WHERE data_hora = ? AND status != 'CANCELADO'";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(dataHora));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * NOVO: Busca todos os horários que já estão ocupados em uma data específica.
     * Utilizado para calcular a Disponibilidade Proativa na tela.
     */
    public List<LocalTime> buscarHorariosOcupados(LocalDate data) throws SQLException {
        List<LocalTime> horariosOcupados = new ArrayList<>();
        
        // Pede ao PostgreSQL apenas os agendamentos que batem com o dia informado
        String sql = "SELECT data_hora FROM agendamento WHERE DATE(data_hora) = ? AND status != 'CANCELADO'";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Converte o LocalDate do Java para a Data do SQL
            stmt.setDate(1, java.sql.Date.valueOf(data));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("data_hora");
                    // Extrai apenas a hora do timestamp e adiciona na lista
                    horariosOcupados.add(ts.toLocalDateTime().toLocalTime());
                }
            }
        }
        return horariosOcupados;
    }
    /**
     * DASHBOARD: Busca os agendamentos do dia atual para um profissional específico.
     * Realiza um JOIN com a tabela de pacientes para exibir o nome na interface.
     */
    public List<Agendamento> buscarAgendamentosDoDia(int idPsicologo, LocalDate data) throws SQLException {
        List<Agendamento> lista = new ArrayList<>();
        
        String sql = "SELECT a.id, a.data_hora, a.status, p.id AS paciente_id, p.nome AS paciente_nome " +
                     "FROM agendamento a " +
                     "INNER JOIN pacientes p ON a.paciente_id = p.id " +
                     "WHERE a.usuario_id = ? AND DATE(a.data_hora) = ? " +
                     "ORDER BY a.data_hora ASC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, idPsicologo);
            stmt.setDate(2, java.sql.Date.valueOf(data));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Monta um objeto Paciente parcial apenas para exibição visual
                    com.faculdade.sistema.clinica.model.Paciente paciente = new com.faculdade.sistema.clinica.model.Paciente(
                        rs.getInt("paciente_id"), 
                        rs.getString("paciente_nome"), 
                        "", LocalDate.now(), ""
                    );
                    
                    Agendamento agendamento = new Agendamento(
                        rs.getInt("id"),
                        rs.getTimestamp("data_hora").toLocalDateTime(),
                        rs.getString("status"),
                        paciente,
                        null // O operador não é necessário para esta visualização
                    );
                    
                    lista.add(agendamento);
                }
            }
        }
        return lista;
    }
    /**
     * AGENDA GLOBAL: Busca todos os agendamentos dentro de um intervalo de datas.
     * Ideal para visualizações semanais e mensais.
     */
    public List<Agendamento> buscarAgendamentosPorPeriodo(int idPsicologo, LocalDate dataInicio, LocalDate dataFim) throws SQLException {
        List<Agendamento> lista = new ArrayList<>();
        
        String sql = "SELECT a.id, a.data_hora, a.status, p.id AS paciente_id, p.nome AS paciente_nome " +
                     "FROM agendamento a " +
                     "INNER JOIN pacientes p ON a.paciente_id = p.id " +
                     "WHERE a.usuario_id = ? AND DATE(a.data_hora) BETWEEN ? AND ? " +
                     "ORDER BY a.data_hora ASC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, idPsicologo);
            stmt.setDate(2, java.sql.Date.valueOf(dataInicio));
            stmt.setDate(3, java.sql.Date.valueOf(dataFim));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    com.faculdade.sistema.clinica.model.Paciente paciente = new com.faculdade.sistema.clinica.model.Paciente(
                        rs.getInt("paciente_id"), 
                        rs.getString("paciente_nome"), 
                        "", LocalDate.now(), ""
                    );
                    
                    Agendamento agendamento = new Agendamento(
                        rs.getInt("id"),
                        rs.getTimestamp("data_hora").toLocalDateTime(),
                        rs.getString("status"),
                        paciente,
                        null 
                    );
                    
                    lista.add(agendamento);
                }
            }
        }
        return lista;
    }
    /**
     * CANCELAMENTO: Altera o status do agendamento para 'CANCELADO'.
     * Mantemos o registro no banco para histórico clínico, realizando apenas exclusão lógica.
     */
    public void cancelar(int idAgendamento) throws SQLException {
        String sql = "UPDATE agendamento SET status = 'CANCELADO' WHERE id = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, idAgendamento);
            stmt.executeUpdate();
        }
    }

    /**
     * RECORRÊNCIA: Gera consultas semanais automáticas a partir de uma data inicial.
     * @param agendamento O objeto base contendo o paciente, operador e horário.
     * @param quantidadeSemanas O número de semanas que o sistema deve projetar.
     */
    public void inserirLoteRecorrente(Agendamento agendamento, int quantidadeSemanas) throws SQLException {
        String sql = "INSERT INTO agendamento (data_hora, status, paciente_id, usuario_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            // Desativa o auto-commit para garantir que ou todas as sessões são salvas, ou nenhuma é (Transação ACID)
            conn.setAutoCommit(false);
            
            try {
                LocalDateTime dataHoraAtual = agendamento.getDataHora();
                
                for (int i = 0; i < quantidadeSemanas; i++) {
                    // Soma as semanas à data base (a iteração 0 salva a data original)
                    LocalDateTime dataSessao = dataHoraAtual.plusWeeks(i);
                    
                    stmt.setTimestamp(1, Timestamp.valueOf(dataSessao));
                    stmt.setString(2, agendamento.getStatus());
                    stmt.setInt(3, agendamento.getPaciente().getId());
                    stmt.setInt(4, agendamento.getOperador().getId());
                    
                    // Adiciona o comando ao lote (batch) em vez de executar um por vez na rede
                    stmt.addBatch(); 
                }
                
                stmt.executeBatch(); // Executa o lote inteiro
                conn.commit();       // Confirma a transação
                
            } catch (SQLException ex) {
                conn.rollback(); // Se falhar no meio, desfaz tudo para não gerar agenda duplicada/quebrada
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}