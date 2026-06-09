package com.faculdade.sistema.clinica.view;

import com.faculdade.sistema.clinica.dao.AgendamentoDAO;
import com.faculdade.sistema.clinica.dao.PacienteDAO;
import com.faculdade.sistema.clinica.model.Agendamento;
import com.faculdade.sistema.clinica.model.Paciente;
import com.faculdade.sistema.clinica.model.Usuario;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Interface Gráfica para Marcação de Consultas (Com Disponibilidade Proativa).
 * Inclui o Módulo de Agendamentos Recorrentes em Lote.
 */
public class TelaAgendamento extends JDialog {

    private JComboBox<Paciente> cbPacientes;
    private JFormattedTextField txtData;
    private JComboBox<String> cbHorarios;
    private JComboBox<String> cbStatus;
    
    // VARIÁVEIS DO NOVO MÓDULO DE RECORRÊNCIA
    private JCheckBox chkRecorrente;
    private JSpinner spinSemanas;
    
    private JButton btnBuscarHorarios, btnAgendar, btnCancelar;
    
    private Usuario operadorSistema;
    private DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- DESIGN SYSTEM (Corporativo) ---
    private final Color COR_PRIMARIA = new Color(41, 128, 185);
    private final Color COR_FUNDO = Color.WHITE;
    private final Color COR_TEXTO_ESCURO = new Color(44, 62, 80);
    private final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONTE_PADRAO = new Font("Segoe UI", Font.PLAIN, 14);

    public TelaAgendamento(Window parent, Usuario usuarioLogado) {
        super(parent, "Sistema Clínico - Agendamento de Consultas", ModalityType.APPLICATION_MODAL);
        this.operadorSistema = usuarioLogado;
        
        setSize(550, 680); // Altura expandida para acomodar a recorrência
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painelPrincipal = new JPanel(new BorderLayout(0, 20));
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // --- CABEÇALHO ---
        JPanel painelCabecalho = new JPanel(new GridLayout(2, 1));
        painelCabecalho.setBackground(COR_FUNDO);
        
        JLabel lblTitulo = new JLabel("Agendar Consulta", SwingConstants.CENTER);
        lblTitulo.setFont(FONTE_TITULO);
        lblTitulo.setForeground(COR_PRIMARIA);
        
        JLabel lblSubtitulo = new JLabel("Operador: " + operadorSistema.getNome() + " (" + operadorSistema.getClass().getSimpleName() + ")", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);
        
        painelCabecalho.add(lblTitulo);
        painelCabecalho.add(lblSubtitulo);
        painelPrincipal.add(painelCabecalho, BorderLayout.NORTH);

        // --- FORMULÁRIO ---
        JPanel painelFormulario = new JPanel(new GridBagLayout());
        painelFormulario.setBackground(COR_FUNDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 15, 0); 
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // 1. Paciente
        gbc.gridy = 0;
        JLabel lblPaciente = new JLabel("Selecione o Paciente");
        lblPaciente.setFont(FONTE_PADRAO);
        lblPaciente.setForeground(COR_TEXTO_ESCURO);
        painelFormulario.add(lblPaciente, gbc);

        gbc.gridy = 1;
        cbPacientes = new JComboBox<>();
        cbPacientes.setFont(FONTE_PADRAO);
        cbPacientes.setPreferredSize(new Dimension(0, 35));
        carregarComboPacientes();
        painelFormulario.add(cbPacientes, gbc);

        // 2. Data e Botão de Busca
        gbc.gridy = 2;
        JLabel lblData = new JLabel("Data da Consulta");
        lblData.setFont(FONTE_PADRAO);
        lblData.setForeground(COR_TEXTO_ESCURO);
        painelFormulario.add(lblData, gbc);

        gbc.gridy = 3;
        JPanel painelData = new JPanel(new BorderLayout(10, 0));
        painelData.setBackground(COR_FUNDO);
        try {
            MaskFormatter mascaraData = new MaskFormatter("##/##/####");
            mascaraData.setPlaceholderCharacter('_');
            txtData = new JFormattedTextField(mascaraData);
        } catch (Exception e) {
            txtData = new JFormattedTextField();
        }
        txtData.setFont(FONTE_PADRAO);
        txtData.setPreferredSize(new Dimension(0, 35));
        painelData.add(txtData, BorderLayout.CENTER);

        btnBuscarHorarios = new JButton("Ver Horários Livres");
        btnBuscarHorarios.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuscarHorarios.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscarHorarios.setFocusPainted(false);
        painelData.add(btnBuscarHorarios, BorderLayout.EAST);
        painelFormulario.add(painelData, gbc);

        // 3. Horários Disponíveis
        gbc.gridy = 4;
        JLabel lblHorarios = new JLabel("Horários Disponíveis");
        lblHorarios.setFont(FONTE_PADRAO);
        lblHorarios.setForeground(COR_TEXTO_ESCURO);
        painelFormulario.add(lblHorarios, gbc);

        gbc.gridy = 5;
        cbHorarios = new JComboBox<>();
        cbHorarios.setFont(FONTE_PADRAO);
        cbHorarios.setPreferredSize(new Dimension(0, 35));
        cbHorarios.addItem("Digite a data e clique em buscar...");
        painelFormulario.add(cbHorarios, gbc);

        // 4. Status Inicial
        gbc.gridy = 6;
        JLabel lblStatus = new JLabel("Status Inicial");
        lblStatus.setFont(FONTE_PADRAO);
        lblStatus.setForeground(COR_TEXTO_ESCURO);
        painelFormulario.add(lblStatus, gbc);

        gbc.gridy = 7;
        cbStatus = new JComboBox<>(new String[]{"AGENDADO", "CONFIRMADO"});
        cbStatus.setFont(FONTE_PADRAO);
        cbStatus.setPreferredSize(new Dimension(0, 35));
        painelFormulario.add(cbStatus, gbc);

        // 5. MÓDULO DE RECORRÊNCIA
        gbc.gridy = 8;
        chkRecorrente = new JCheckBox("Marcar como Consulta Recorrente (Semanal)");
        chkRecorrente.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkRecorrente.setBackground(COR_FUNDO);
        chkRecorrente.setForeground(COR_PRIMARIA);
        painelFormulario.add(chkRecorrente, gbc);

        gbc.gridy = 9;
        JPanel painelSemanas = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelSemanas.setBackground(COR_FUNDO);
        
        JLabel lblSemanas = new JLabel("Repetir por quantas semanas?");
        lblSemanas.setFont(FONTE_PADRAO);
        
        SpinnerModel modeloSemanas = new SpinnerNumberModel(4, 2, 12, 1);
        spinSemanas = new JSpinner(modeloSemanas);
        spinSemanas.setPreferredSize(new Dimension(60, 30));
        spinSemanas.setEnabled(false); // Inicia desativado
        
        painelSemanas.add(lblSemanas);
        painelSemanas.add(spinSemanas);
        painelFormulario.add(painelSemanas, gbc);

        // Evento que ativa/desativa o seletor numérico de quantidade
        chkRecorrente.addActionListener(e -> spinSemanas.setEnabled(chkRecorrente.isSelected()));

        painelPrincipal.add(painelFormulario, BorderLayout.CENTER);

        // --- BOTÕES DE AÇÃO ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        painelBotoes.setBackground(COR_FUNDO);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setForeground(COR_TEXTO_ESCURO);

        btnAgendar = new JButton("Confirmar Agendamento");
        btnAgendar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAgendar.setBackground(COR_PRIMARIA);
        btnAgendar.setForeground(Color.WHITE);
        btnAgendar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgendar.setFocusPainted(false);

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnAgendar);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        add(painelPrincipal);

        // --- EVENTOS ---
        btnCancelar.addActionListener(e -> this.dispose());
        btnBuscarHorarios.addActionListener(e -> calcularDisponibilidade());
        btnAgendar.addActionListener(e -> executarAgendamento());
    }

    private void carregarComboPacientes() {
        try {
            PacienteDAO dao = new PacienteDAO();
            List<Paciente> lista = dao.listar();
            for (Paciente p : lista) {
                cbPacientes.addItem(p);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pacientes: " + ex.getMessage());
        }
    }

    private void calcularDisponibilidade() {
        String dataStr = txtData.getText().trim();
        
        if (dataStr.contains("_")) {
            JOptionPane.showMessageDialog(this, "Preencha a data completa antes de buscar os horários.");
            return;
        }

        try {
            LocalDate dataSelecionada = LocalDate.parse(dataStr, formatadorData);

            if (dataSelecionada.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Não é possível agendar consultas em datas retroativas.", "Data Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AgendamentoDAO dao = new AgendamentoDAO();
            List<LocalTime> horariosOcupados = dao.buscarHorariosOcupados(dataSelecionada);

            cbHorarios.removeAllItems();

            LocalTime[] gradeClinica = {
                LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
            };

            boolean achouHorarioLivre = false;

            for (LocalTime horarioGrade : gradeClinica) {
                if (dataSelecionada.isEqual(LocalDate.now()) && horarioGrade.isBefore(LocalTime.now())) {
                    continue; 
                }
                
                if (!horariosOcupados.contains(horarioGrade)) {
                    cbHorarios.addItem(horarioGrade.toString()); 
                    achouHorarioLivre = true;
                }
            }

            if (!achouHorarioLivre) {
                cbHorarios.addItem("Nenhum horário livre.");
                JOptionPane.showMessageDialog(this, "Todos os horários desta data já estão agendados.", "Agenda Lotada", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Data inválida ou formato incorreto.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao consultar banco de dados: " + ex.getMessage());
        }
    }

    private void executarAgendamento() {
        String dataStr = txtData.getText().trim();
        String horarioSelecionado = (String) cbHorarios.getSelectedItem();
        Paciente paciente = (Paciente) cbPacientes.getSelectedItem();

        if (dataStr.contains("_") || paciente == null || horarioSelecionado == null || horarioSelecionado.contains("Nenhum") || horarioSelecionado.contains("Digite")) {
            JOptionPane.showMessageDialog(this, "Preencha a data e verifique os horários livres disponíveis.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate data = LocalDate.parse(dataStr, formatadorData);
            LocalTime hora = LocalTime.parse(horarioSelecionado);
            LocalDateTime dataHoraConsulta = LocalDateTime.of(data, hora);

            Agendamento novoAgendamento = new Agendamento(
                    0, dataHoraConsulta, cbStatus.getSelectedItem().toString(), paciente, operadorSistema
            );

            AgendamentoDAO dao = new AgendamentoDAO();
            
            // Trava final de redundância por segurança
            if (dao.isHorarioOcupado(dataHoraConsulta)) {
                JOptionPane.showMessageDialog(this, "Este horário acabou de ser ocupado. Escolha outro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // RAMIFICAÇÃO: Verifica se a caixa de seleção de recorrência foi marcada
            if (chkRecorrente.isSelected()) {
                int qtdSemanas = (Integer) spinSemanas.getValue();
                dao.inserirLoteRecorrente(novoAgendamento, qtdSemanas);
                JOptionPane.showMessageDialog(this, "Série de " + qtdSemanas + " consultas semanais agendadas com sucesso para o paciente:\n" + paciente.getNome());
            } else {
                dao.inserir(novoAgendamento);
                JOptionPane.showMessageDialog(this, "Consulta única agendada com sucesso para o paciente:\n" + paciente.getNome());
            }

            this.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar agendamento: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        com.faculdade.sistema.clinica.model.Usuario usuarioTeste = 
            new com.faculdade.sistema.clinica.model.Psicologo(1, "Dr. Roberto (Teste)", "roberto.psi", "admin123", "CRP-12345");
        
        SwingUtilities.invokeLater(() -> {
            new TelaAgendamento(null, usuarioTeste).setVisible(true);
        });
    }
}