package com.faculdade.sistema.clinica.view;

import com.faculdade.sistema.clinica.dao.UsuarioDAO;
import com.faculdade.sistema.clinica.model.Usuario;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Interface Gráfica de Autenticação.
 * Refatorada com interceptação de segurança para primeiro acesso obrigatório.
 */
public class TelaLogin extends JFrame {
    
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar;

    private final Color COR_PRIMARIA = new Color(41, 128, 185); 
    private final Color COR_TEXTO_ESCURO = new Color(44, 62, 80); 
    private final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONTE_PADRAO = new Font("Segoe UI", Font.PLAIN, 14);

    public TelaLogin() {
        setTitle("Sistema Clínico - Acesso Seguro");
        setSize(450, 380); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); 
        
        JPanel painelPrincipal = new JPanel(new BorderLayout(0, 15));
        painelPrincipal.setBackground(Color.WHITE);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40)); 

        // --- CABEÇALHO ---
        JPanel painelCabecalho = new JPanel(new GridLayout(2, 1));
        painelCabecalho.setBackground(Color.WHITE);
        
        JLabel lblTitulo = new JLabel("Sistema Clínico", SwingConstants.CENTER);
        lblTitulo.setFont(FONTE_TITULO);
        lblTitulo.setForeground(COR_PRIMARIA);
        
        JLabel lblSubtitulo = new JLabel("Faça login para acessar sua conta", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);
        
        painelCabecalho.add(lblTitulo);
        painelCabecalho.add(lblSubtitulo);
        
        painelPrincipal.add(painelCabecalho, BorderLayout.NORTH);

        // --- FORMULÁRIO ---
        JPanel painelFormulario = new JPanel();
        painelFormulario.setLayout(new BoxLayout(painelFormulario, BoxLayout.Y_AXIS));
        painelFormulario.setBackground(Color.WHITE);

        JLabel lblLogin = new JLabel("Usuário");
        lblLogin.setFont(FONTE_PADRAO);
        lblLogin.setForeground(COR_TEXTO_ESCURO);
        lblLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtLogin = new JTextField();
        txtLogin.setFont(FONTE_PADRAO);
        txtLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); 
        txtLogin.setPreferredSize(new Dimension(350, 40));
        txtLogin.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Digite seu nome de usuário");
        txtLogin.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSenha = new JLabel("Senha");
        lblSenha.setFont(FONTE_PADRAO);
        lblSenha.setForeground(COR_TEXTO_ESCURO);
        lblSenha.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSenha = new JPasswordField();
        txtSenha.setFont(FONTE_PADRAO);
        txtSenha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtSenha.setPreferredSize(new Dimension(350, 40));
        txtSenha.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Digite sua senha de acesso");
        txtSenha.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelFormulario.add(lblLogin);
        painelFormulario.add(Box.createRigidArea(new Dimension(0, 5)));
        painelFormulario.add(txtLogin);
        painelFormulario.add(Box.createRigidArea(new Dimension(0, 15)));
        painelFormulario.add(lblSenha);
        painelFormulario.add(Box.createRigidArea(new Dimension(0, 5)));
        painelFormulario.add(txtSenha);

        painelPrincipal.add(painelFormulario, BorderLayout.CENTER);

        // --- BOTÃO DE AÇÃO ---
        btnEntrar = new JButton("Entrar no Sistema");
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEntrar.setBackground(COR_PRIMARIA);
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.setPreferredSize(new Dimension(350, 45)); 
        
        JPanel painelBotao = new JPanel();
        painelBotao.setBackground(Color.WHITE);
        painelBotao.add(btnEntrar);
        
        painelPrincipal.add(painelBotao, BorderLayout.SOUTH);
        add(painelPrincipal);

        // --- EVENTOS ---
        btnEntrar.addActionListener(e -> executarAutenticacao());
        txtSenha.addActionListener(e -> executarAutenticacao());
    }

    private void executarAutenticacao() {
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword()).trim();

        if (login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha o usuário e a senha.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuarioLogado = null;

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // Data Access Object (DAO) para verificação no banco de dados
            UsuarioDAO dao = new UsuarioDAO();
            usuarioLogado = dao.autenticar(login, senha);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Falha ao conectar com o banco de dados:\n" + ex.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            return; 
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }

        if (usuarioLogado != null) {
            // VERIFICAÇÃO DE SEGURANÇA: Interceptação do Primeiro Acesso
            if (usuarioLogado.isPrimeiroAcesso()) {
                JOptionPane.showMessageDialog(this, 
                    "Segurança Corporativa: Detectamos que este é o seu primeiro acesso (ou a sua senha foi redefinida).\n\n" +
                    "É obrigatório registrar uma nova senha pessoal para continuar.", 
                    "Ação Obrigatória", JOptionPane.WARNING_MESSAGE);
                
                // Abre o modal de alteração de senha
                new TelaAlterarSenha(this, usuarioLogado).setVisible(true);
                
                // Limpa o campo de senha da tela de login para forçar o usuário a digitar a nova senha recém-criada
                txtSenha.setText("");
                txtSenha.requestFocus();
            } else {
                final Usuario usuarioConfirmadoParaTela = usuarioLogado;
                SwingUtilities.invokeLater(() -> {
                    new TelaPrincipal(usuarioConfirmadoParaTela).setVisible(true);
                });
                this.dispose(); 
            }
        } else {
            JOptionPane.showMessageDialog(this, "Credenciais inválidas!", "Erro de Autenticação", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Falha ao inicializar o visual moderno.");
        }

        SwingUtilities.invokeLater(() -> {
            new TelaLogin().setVisible(true);
        });
    }
}