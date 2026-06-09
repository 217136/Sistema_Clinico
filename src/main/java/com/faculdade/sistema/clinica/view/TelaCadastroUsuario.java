package com.faculdade.sistema.clinica.view;

import com.faculdade.sistema.clinica.dao.UsuarioDAO;
import com.faculdade.sistema.clinica.model.Administrador;
import com.faculdade.sistema.clinica.model.Psicologo;
import com.faculdade.sistema.clinica.model.Recepcionista;
import com.faculdade.sistema.clinica.model.Usuario;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.sql.SQLException;

public class TelaCadastroUsuario extends JDialog {

    private JTextField txtNome;
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JComboBox<String> cbPerfil;
    private JFormattedTextField txtCrp;
    private JButton btnSalvar;
    private JButton btnCancelar;
    
    // Variável que determina se estamos em modo Inserção ou Edição
    private Usuario usuarioEdicao = null;

    private final Color COR_PRIMARIA = new Color(41, 128, 185);
    private final Color COR_FUNDO = new Color(245, 246, 250);

    // Construtor 1: Modo de NOVO CADASTRO
    public TelaCadastroUsuario(Window parent) {
        super(parent, "Gestão de Identidade - Novo Usuário", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        initComponents();
        configurarEventos();
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    // Construtor 2: Modo de EDIÇÃO (Sobrecarga)
    public TelaCadastroUsuario(Window parent, Usuario usuario) {
        this(parent); // Chama o construtor original para montar a tela
        this.usuarioEdicao = usuario;
        
        setTitle("Gestão de Identidade - Editar Usuário");
        btnSalvar.setText("Salvar Alterações");
        preencherDadosEdicao();
    }

    private void preencherDadosEdicao() {
        txtNome.setText(usuarioEdicao.getNome());
        txtLogin.setText(usuarioEdicao.getLogin());
        
        // Bloqueios de Segurança na Edição
        txtLogin.setEditable(false); // O login não deve ser alterado para evitar falhas de chave única
        cbPerfil.setSelectedItem(usuarioEdicao.getPerfil());
        cbPerfil.setEnabled(false); // O perfil não muda, para não quebrar regras de integridade (ex: apagar o CRP de um psicólogo por acidente)
        
        if (usuarioEdicao instanceof Psicologo) {
            txtCrp.setText(((Psicologo) usuarioEdicao).getCrp());
        }
        
        txtSenha.setToolTipText("Deixe em branco para manter a senha criptografada atual.");
    }

    private void initComponents() {
        JPanel painelTopo = new JPanel();
        painelTopo.setBackground(COR_PRIMARIA);
        painelTopo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel lblTitulo = new JLabel("Formulário de Acesso e Identidade");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        painelTopo.add(lblTitulo);
        add(painelTopo, BorderLayout.NORTH);

        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        Font fonteLabel = new Font("Segoe UI", Font.BOLD, 12);

        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNome = new JLabel("Nome Completo:");
        lblNome.setFont(fonteLabel);
        painelPrincipal.add(lblNome, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtNome = new JTextField(20);
        painelPrincipal.add(txtNome, gbc);

        // Login
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblLogin = new JLabel("Login de Acesso:");
        lblLogin.setFont(fonteLabel);
        painelPrincipal.add(lblLogin, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtLogin = new JTextField(20);
        painelPrincipal.add(txtLogin, gbc);

        // Senha
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblSenha = new JLabel("Senha Padrão:");
        lblSenha.setFont(fonteLabel);
        painelPrincipal.add(lblSenha, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtSenha = new JPasswordField(20);
        painelPrincipal.add(txtSenha, gbc);

        // Perfil
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblPerfil = new JLabel("Perfil de Acesso:");
        lblPerfil.setFont(fonteLabel);
        painelPrincipal.add(lblPerfil, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        cbPerfil = new JComboBox<>(new String[]{"RECEPCIONISTA", "PSICOLOGO_CLINICO", "ADMINISTRADOR"});
        painelPrincipal.add(cbPerfil, gbc);

        // CRP
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        JLabel lblCrp = new JLabel("Nº do CRP:");
        lblCrp.setFont(fonteLabel);
        painelPrincipal.add(lblCrp, gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        try {
            MaskFormatter mascaraCrp = new MaskFormatter("##/######");
            mascaraCrp.setPlaceholderCharacter('_');
            txtCrp = new JFormattedTextField(mascaraCrp);
        } catch (Exception ex) {
            txtCrp = new JFormattedTextField();
        }
        txtCrp.setEnabled(false);
        painelPrincipal.add(txtCrp, gbc);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        painelBotoes.setBackground(COR_FUNDO);
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvar = new JButton("Registrar Usuário");
        btnSalvar.setBackground(COR_PRIMARIA);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnSalvar);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 10, 10);
        painelPrincipal.add(painelBotoes, gbc);

        add(painelPrincipal, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        cbPerfil.addActionListener(e -> {
            boolean isPsicologo = "PSICOLOGO_CLINICO".equals(cbPerfil.getSelectedItem());
            txtCrp.setEnabled(isPsicologo);
            if (!isPsicologo) txtCrp.setValue(null);
        });

        btnCancelar.addActionListener(e -> this.dispose());
        btnSalvar.addActionListener(e -> processarSalvamento());
    }

    private void processarSalvamento() {
        String nome = txtNome.getText().trim();
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword());
        String perfilSelecionado = (String) cbPerfil.getSelectedItem();
        String crp = txtCrp.getText().replace("_", "").trim();

        if (nome.isEmpty() || login.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome e Login são obrigatórios.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Se for modo inserção, a senha é obrigatória. Se for edição, pode ser vazia.
        if (usuarioEdicao == null && senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A senha é obrigatória para um novo usuário.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("PSICOLOGO_CLINICO".equals(perfilSelecionado) && (crp.length() < 9 || crp.equals("/"))) {
            JOptionPane.showMessageDialog(this, "O CRP deve estar completo.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            UsuarioDAO dao = new UsuarioDAO();

            if (usuarioEdicao == null) {
                // MODO: INSERÇÃO (CREATE)
                Usuario novoUsuario;
                if ("PSICOLOGO_CLINICO".equals(perfilSelecionado)) {
                    novoUsuario = new Psicologo(0, nome, login, senha, crp);
                } else if ("ADMINISTRADOR".equals(perfilSelecionado)) {
                    novoUsuario = new Administrador(0, nome, login, senha);
                } else {
                    novoUsuario = new Recepcionista(0, nome, login, senha);
                }
                dao.inserir(novoUsuario);
                JOptionPane.showMessageDialog(this, "Novo usuário cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // MODO: EDIÇÃO (UPDATE)
                usuarioEdicao.setNome(nome);
                boolean atualizarSenha = !senha.isEmpty();
                if (atualizarSenha) {
                    usuarioEdicao.setSenha(senha); // O Hash será gerado dentro do DAO
                }
                if (usuarioEdicao instanceof Psicologo) {
                    ((Psicologo) usuarioEdicao).setCrp(crp);
                }
                
                dao.atualizar(usuarioEdicao, atualizarSenha);
                JOptionPane.showMessageDialog(this, "Dados do usuário atualizados com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            this.dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao processar dados no banco:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}