package com.faculdade.sistema.clinica.view;

/**
 *
 * @author Amauri
 */

import com.faculdade.sistema.clinica.dao.AgendamentoDAO;
import com.faculdade.sistema.clinica.model.Agendamento;
import com.faculdade.sistema.clinica.model.Usuario;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.DayOfWeek;

/**
 * Interface Gráfica da Agenda Global (Visão Mensal/Semanal).
 * Utiliza o componente especializado LGoodDatePicker para navegação fluida.
 */
public class TelaAgendaGlobal extends JDialog {

    private Usuario usuarioLogado;
    private DatePicker seletorData;
    private JComboBox<String> cbFiltroPeriodo;
    private JTable tabelaAgendamentos;
    private DefaultTableModel modeloTabela;
    private List<Agendamento> listaAtualDeAgendamentos; // NOVA VARIÁVEL
    
    private final Color COR_PRIMARIA = new Color(41, 128, 185);
    private final Color COR_FUNDO = Color.WHITE;

    public TelaAgendaGlobal(Window parent, Usuario usuario) {
        super(parent, "Agenda Global de Consultas", ModalityType.APPLICATION_MODAL);
        this.usuarioLogado = usuario;

        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COR_FUNDO);

        initComponents();
        
        // Define a data atual como padrão e carrega os dados inicialmente
        seletorData.setDate(LocalDate.now());
        carregarAgendamentos();
    }

    private void initComponents() {
        // --- CABEÇALHO COM FILTROS ---
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        painelTopo.setBackground(COR_PRIMARIA);

        JLabel lblTitulo = new JLabel("  Calendário Clínico");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        painelTopo.add(lblTitulo);

        // Configuração do DatePicker (Calendário Especializado)
        DatePickerSettings configData = new DatePickerSettings();
        configData.setFormatForDatesCommonEra("dd/MM/yyyy");
        configData.setFontValidDate(new Font("Segoe UI", Font.PLAIN, 14));
        
        seletorData = new DatePicker(configData);
        
        // Filtro de Período
        cbFiltroPeriodo = new JComboBox<>(new String[]{"Visualizar Dia Selecionado", "Visualizar Semana", "Visualizar Mês Inteiro"});
        cbFiltroPeriodo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnBuscar = new JButton("Buscar Agendamentos");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuscar.setBackground(new Color(46, 204, 113));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        painelTopo.add(new JLabel("  ")); // Espaçador
        painelTopo.add(seletorData);
        painelTopo.add(cbFiltroPeriodo);
        painelTopo.add(btnBuscar);

        add(painelTopo, BorderLayout.NORTH);

        // --- TABELA DE DADOS (DATA TABLE) ---
        String[] colunas = {"Data", "Horário", "Paciente", "Status", "Operador"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaAgendamentos = new JTable(modeloTabela);
        tabelaAgendamentos.setRowHeight(30);
        tabelaAgendamentos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabelaAgendamentos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaAgendamentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tabelaAgendamentos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        scrollPane.setBackground(COR_FUNDO);
        add(scrollPane, BorderLayout.CENTER);

        // --- PAINEL DE AÇÕES ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        painelBotoes.setBackground(COR_FUNDO);

        JButton btnNovoAgendamento = new JButton("Agendar Nova Consulta");
        btnNovoAgendamento.setBackground(COR_PRIMARIA);
        btnNovoAgendamento.setForeground(Color.WHITE);
        btnNovoAgendamento.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnCancelarConsulta = new JButton("Cancelar Atendimento");
        btnCancelarConsulta.setForeground(new Color(231, 76, 60));

        painelBotoes.add(btnCancelarConsulta);
        painelBotoes.add(btnNovoAgendamento);
        add(painelBotoes, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnBuscar.addActionListener(e -> carregarAgendamentos());
        
        btnNovoAgendamento.addActionListener(e -> {
            new TelaAgendamento(this, usuarioLogado).setVisible(true);
            carregarAgendamentos(); // Atualiza a tabela após fechar a marcação
        });
        
        btnCancelarConsulta.addActionListener(e -> cancelarAgendamentoSelecionado());
    }

    private void carregarAgendamentos() {
        LocalDate dataBase = seletorData.getDate();
        if (dataBase == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione uma data no calendário.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int indexFiltro = cbFiltroPeriodo.getSelectedIndex();
        modeloTabela.setRowCount(0);

        try {
            AgendamentoDAO dao = new AgendamentoDAO();
            List<Agendamento> lista;

            // Roteamento inteligente baseado no filtro selecionado
            // Como era: List<Agendamento> lista;
            // Substitua por:
            
            if (indexFiltro == 0) {
                listaAtualDeAgendamentos = dao.buscarAgendamentosDoDia(usuarioLogado.getId(), dataBase);
            } else if (indexFiltro == 1) {
                LocalDate inicioSemana = dataBase.with(DayOfWeek.MONDAY);
                LocalDate fimSemana = dataBase.with(DayOfWeek.SUNDAY);
                listaAtualDeAgendamentos = dao.buscarAgendamentosPorPeriodo(usuarioLogado.getId(), inicioSemana, fimSemana);
            } else {
                LocalDate inicioMes = dataBase.withDayOfMonth(1);
                LocalDate fimMes = dataBase.withDayOfMonth(dataBase.lengthOfMonth());
                listaAtualDeAgendamentos = dao.buscarAgendamentosPorPeriodo(usuarioLogado.getId(), inicioMes, fimMes);
            }

            DateTimeFormatter fmtData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");

            // Mude o 'for' para ler da nova variável global
            for (Agendamento a : listaAtualDeAgendamentos) { 
                // ... resto do seu código for
                String nomeOperador = (a.getOperador() != null) ? a.getOperador().getNome() : "-";
                
                modeloTabela.addRow(new Object[]{
                    a.getDataHora().format(fmtData),
                    a.getDataHora().format(fmtHora),
                    a.getPaciente().getNome(),
                    a.getStatus(),
                    nomeOperador
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao consultar agendamentos: " + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelarAgendamentoSelecionado() {
        int linhaSelecionada = tabelaAgendamentos.getSelectedRow();
        
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um agendamento na tabela para cancelar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pega o agendamento exato da nossa lista em memória, baseado na linha clicada
        Agendamento agendamento = listaAtualDeAgendamentos.get(linhaSelecionada);

        if ("CANCELADO".equals(agendamento.getStatus())) {
            JOptionPane.showMessageDialog(this, "Esta consulta já se encontra cancelada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(this, 
            "Deseja realmente cancelar a consulta de " + agendamento.getPaciente().getNome() + "?\nEsta ação liberará o horário na agenda.", 
            "Confirmar Cancelamento", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                new AgendamentoDAO().cancelar(agendamento.getId());
                JOptionPane.showMessageDialog(this, "Consulta cancelada com sucesso!");
                carregarAgendamentos(); // Recarrega a tabela para atualizar o status visual
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao cancelar consulta: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}