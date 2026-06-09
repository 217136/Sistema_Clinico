package com.faculdade.sistema.clinica.view;

/**
 *
 * @author Amauri
 */

import com.faculdade.sistema.clinica.dao.UsuarioDAO;
import com.faculdade.sistema.clinica.model.Usuario;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Interface Gráfica para o próprio funcionário alterar a sua senha de acesso.
 */
public class TelaAlterarSenha extends JDialog {

    private JPasswordField txtSenhaAtual;
    private JPasswordField txtNovaSenha;
    private JPasswordField txtConfirmarSenha;
    private JButton btnSalvar;
    private JButton btnCancelar;
    
    private Usuario usuarioLogado;
    
    private final Color COR_PRIMARIA = new Color(41, 128, 185);
    private final Color COR_FUNDO = new Color(245, 246, 250);

    public TelaAlterarSenha(Window parent, Usuario usuarioLogado) {
        super(parent, "Minha Conta - Alteração de Senha", ModalityType.APPLICATION_MODAL);
        this.usuarioLogado = usuarioLogado;
        
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);
        
        initComponents();
    }

    private void initComponents() {
        // --- CABEÇALHO ---
        JPanel painelTopo = new JPanel();
        painelTopo.setBackground(COR_PRIMARIA);
        painelTopo.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel lblTitulo = new JLabel("Definir Nova Senha de Acesso");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        painelTopo.add(lblTitulo);
        add(painelTopo, BorderLayout.NORTH);

        // --- FORMULÁRIO ---
        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        Font fonteLabel = new Font("Segoe UI", Font.BOLD, 12);

        // 1. Senha Atual
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblAtual = new JLabel("Senha Atual:");
        lblAtual.setFont(fonteLabel);
        painelPrincipal.add(lblAtual, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtSenhaAtual = new JPasswordField(15);
        txtSenhaAtual.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Digite a senha atual");
        txtSenhaAtual.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true"); // Botão de mostrar senha (olho)
        painelPrincipal.add(txtSenhaAtual, gbc);

        // 2. Nova Senha
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblNova = new JLabel("Nova Senha:");
        lblNova.setFont(fonteLabel);
        painelPrincipal.add(lblNova, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtNovaSenha = new JPasswordField(15);
        txtNovaSenha.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        painelPrincipal.add(txtNovaSenha, gbc);

        // 3. Confirmar Nova Senha
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblConfirmar = new JLabel("Confirme a Senha:");
        lblConfirmar.setFont(fonteLabel);
        painelPrincipal.add(lblConfirmar, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtConfirmarSenha = new JPasswordField(15);
        txtConfirmarSenha.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        painelPrincipal.add(txtConfirmarSenha, gbc);

        // --- BOTÕES ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        painelBotoes.setBackground(COR_FUNDO);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSalvar = new JButton("Atualizar Senha");
        btnSalvar.setBackground(COR_PRIMARIA);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnSalvar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 10, 10);
        painelPrincipal.add(painelBotoes, gbc);

        add(painelPrincipal, BorderLayout.CENTER);

        // --- EVENTOS ---
        btnCancelar.addActionListener(e -> this.dispose());
        btnSalvar.addActionListener(e -> executarAlteracao());
    }

    private void executarAlteracao() {
        String senhaAtual = new String(txtSenhaAtual.getPassword());
        String novaSenha = new String(txtNovaSenha.getPassword());
        String confirmarSenha = new String(txtConfirmarSenha.getPassword());

        if (senhaAtual.isEmpty() || novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.", "Campos Vazios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            JOptionPane.showMessageDialog(this, "A nova senha e a confirmação não coincidem.", "Erro de Digitação", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (senhaAtual.equals(novaSenha)) {
            JOptionPane.showMessageDialog(this, "A nova senha deve ser diferente da senha atual.", "Segurança", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            UsuarioDAO dao = new UsuarioDAO();

            // 1. Valida se a senha atual informada está correta (reaproveitando a lógica de Login)
            Usuario validacao = dao.autenticar(usuarioLogado.getLogin(), senhaAtual);
            
            if (validacao == null) {
                JOptionPane.showMessageDialog(this, "A 'Senha Atual' informada está incorreta.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                txtSenhaAtual.requestFocus();
                return;
            }

            // 2. Estando correta, atualiza para a nova senha (aplica o Hash SHA-256 automaticamente)
            dao.atualizarSenhaPessoal(usuarioLogado.getId(), novaSenha);
            
            JOptionPane.showMessageDialog(this, "A sua senha foi alterada com sucesso!\nNa próxima sessão, utilize a nova senha.", "Segurança Atualizada", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao comunicar com o banco de dados:\n" + ex.getMessage(), "Erro Crítico", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}