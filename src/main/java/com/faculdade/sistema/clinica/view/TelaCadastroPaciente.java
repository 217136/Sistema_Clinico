package com.faculdade.sistema.clinica.view;

import com.faculdade.sistema.clinica.dao.PacienteDAO;
import com.faculdade.sistema.clinica.model.Paciente;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Interface Gráfica para Cadastro e Edição de Pacientes.
 * Padrão UI/UX Corporativo unificado (Side-by-Side JDialog Modal).
 */
public class TelaCadastroPaciente extends JDialog {

    private JTextField txtNome;
    private JFormattedTextField txtCpf;
    private JFormattedTextField txtDataNascimento;
    private JFormattedTextField txtTelefone;
    private JButton btnSalvar;
    private JButton btnCancelar;
    
    private Paciente pacienteEdicao = null;

    private final Color COR_PRIMARIA = new Color(41, 128, 185);
    private final Color COR_FUNDO = new Color(245, 246, 250);

    // Construtor 1: NOVO CADASTRO
    public TelaCadastroPaciente(Window parent) {
        super(parent, "Módulo de Recepção - Novo Paciente", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        inicializarTela();
    }

    // Construtor 2: EDIÇÃO
    public TelaCadastroPaciente(Window parent, Paciente paciente) {
        this(parent);
        this.pacienteEdicao = paciente;
        
        setTitle("Módulo de Recepção - Editar Paciente");
        btnSalvar.setText("Salvar Alterações");
        preencherDadosEdicao();
    }

    private void inicializarTela() {
        setSize(500, 450); // Tamanho ajustado ao novo layout compacto
        setLocationRelativeTo(getParent());
        setResizable(false);

        // --- CABEÇALHO ---
        JPanel painelTopo = new JPanel();
        painelTopo.setBackground(COR_PRIMARIA);
        painelTopo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel lblTitulo = new JLabel(pacienteEdicao == null ? "Cadastro de Novo Paciente" : "Atualização de Dados Clínicos");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        painelTopo.add(lblTitulo);
        add(painelTopo, BorderLayout.NORTH);

        // --- FORMULÁRIO (Padrão Side-by-Side) ---
        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        Font fonteLabel = new Font("Segoe UI", Font.BOLD, 12);

        // 1. Nome
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblNome = new JLabel("Nome Completo:");
        lblNome.setFont(fonteLabel);
        painelPrincipal.add(lblNome, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtNome = new JTextField(20);
        txtNome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: João da Silva");
        txtNome.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        painelPrincipal.add(txtNome, gbc);

        // 2. CPF
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblCpf = new JLabel("Nº do CPF:");
        lblCpf.setFont(fonteLabel);
        painelPrincipal.add(lblCpf, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        try {
            MaskFormatter mascaraCpf = new MaskFormatter("###.###.###-##");
            mascaraCpf.setPlaceholderCharacter('_');
            txtCpf = new JFormattedTextField(mascaraCpf);
        } catch (ParseException e) {
            txtCpf = new JFormattedTextField();
        }
        painelPrincipal.add(txtCpf, gbc);

        // 3. Data de Nascimento
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblData = new JLabel("Data Nascimento:");
        lblData.setFont(fonteLabel);
        painelPrincipal.add(lblData, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        try {
            MaskFormatter mascaraData = new MaskFormatter("##/##/####");
            mascaraData.setPlaceholderCharacter('_');
            txtDataNascimento = new JFormattedTextField(mascaraData);
        } catch (ParseException e) {
            txtDataNascimento = new JFormattedTextField();
        }
        painelPrincipal.add(txtDataNascimento, gbc);

        // 4. Telefone
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblTelefone = new JLabel("Telefone / Celular:");
        lblTelefone.setFont(fonteLabel);
        painelPrincipal.add(lblTelefone, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        try {
            MaskFormatter mascaraTel = new MaskFormatter("(##) #####-####");
            mascaraTel.setPlaceholderCharacter('_');
            txtTelefone = new JFormattedTextField(mascaraTel);
        } catch (ParseException e) {
            txtTelefone = new JFormattedTextField();
        }
        painelPrincipal.add(txtTelefone, gbc);

        // --- BOTÕES ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        painelBotoes.setBackground(COR_FUNDO);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSalvar = new JButton("Registrar Paciente");
        btnSalvar.setBackground(COR_PRIMARIA);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnSalvar);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 10, 10);
        painelPrincipal.add(painelBotoes, gbc);

        add(painelPrincipal, BorderLayout.CENTER);

        // --- EVENTOS ---
        btnCancelar.addActionListener(e -> this.dispose());
        btnSalvar.addActionListener(e -> executarSalvamento());
    }

    private void preencherDadosEdicao() {
        txtNome.setText(pacienteEdicao.getNome());
        
        String cpf = pacienteEdicao.getCpf();
        if (cpf != null && cpf.length() == 11) {
            txtCpf.setText(cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11));
        }
        txtCpf.setEditable(false);
        txtCpf.setBackground(new Color(230, 230, 230)); // Escurece levemente para indicar bloqueio
        
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtDataNascimento.setText(pacienteEdicao.getDataNascimento().format(formatador));
        txtTelefone.setText(pacienteEdicao.getTelefone());
    }

    private boolean isCpfValido(String cpf) {
        String cpfLimpo = cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11 || cpfLimpo.matches("(\\d)\\1{10}")) return false;
        try {
            int calc1 = 0, calc2 = 0;
            for (int i = 0; i < 9; i++) {
                int digito = Character.getNumericValue(cpfLimpo.charAt(i));
                calc1 += digito * (10 - i);
                calc2 += digito * (11 - i);
            }
            int digito1 = 11 - (calc1 % 11);
            if (digito1 > 9) digito1 = 0;
            calc2 += digito1 * 2;
            int digito2 = 11 - (calc2 % 11);
            if (digito2 > 9) digito2 = 0;
            return cpfLimpo.endsWith(String.valueOf(digito1) + String.valueOf(digito2));
        } catch (Exception e) {
            return false;
        }
    }

    private void executarSalvamento() {
        String nome = txtNome.getText().trim();
        String cpfFormatado = txtCpf.getText().trim();
        String dataNascStr = txtDataNascimento.getText().trim();
        String telefone = txtTelefone.getText().trim();

        boolean isInsercao = (pacienteEdicao == null);

        if (nome.isEmpty() || dataNascStr.contains("_") || telefone.contains("_") || (isInsercao && cpfFormatado.contains("_"))) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isInsercao && !isCpfValido(cpfFormatado)) {
            JOptionPane.showMessageDialog(this, "O CPF informado é inválido.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate dataNascimento;
        try {
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
            dataNascimento = LocalDate.parse(dataNascStr, formatador);
            if (dataNascimento.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "A data não pode ser no futuro.", "Data Inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Data de nascimento inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PacienteDAO dao = new PacienteDAO();

            if (isInsercao) {
                String cpfLimpo = cpfFormatado.replaceAll("\\D", "");
                Paciente novoPaciente = new Paciente(0, nome, cpfLimpo, dataNascimento, telefone);
                dao.inserir(novoPaciente);
                JOptionPane.showMessageDialog(this, "Paciente cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                pacienteEdicao.setNome(nome);
                pacienteEdicao.setDataNascimento(dataNascimento);
                pacienteEdicao.setTelefone(telefone);
                dao.atualizar(pacienteEdicao);
                JOptionPane.showMessageDialog(this, "Dados do paciente atualizados com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            this.dispose(); // Fecha o modal e libera a tela de listagem para atualizar
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro de integração:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}