package com.faculdade.sistema.clinica.view;

import com.faculdade.sistema.clinica.dao.UsuarioDAO;
import com.faculdade.sistema.clinica.model.Usuario;
import com.faculdade.sistema.clinica.model.Psicologo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface Gráfica para Gestão de Identidade (Listagem e Manutenção).
 */
public class TelaListagemUsuario extends JDialog {

    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private List<Usuario> listaUsuariosCache;

    public TelaListagemUsuario(Window parent) {
        super(parent, "Backoffice - Gestão de Identidade", ModalityType.APPLICATION_MODAL);
        setSize(800, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        carregarDados();
    }

    private void initComponents() {
        // --- CABEÇALHO ---
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTopo.setBackground(new Color(41, 128, 185));
        
        JLabel lblTitulo = new JLabel("  Controle de Funcionários e Acessos");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        painelTopo.add(lblTitulo);
        add(painelTopo, BorderLayout.NORTH);

        // --- TABELA DE DADOS (DATA TABLE) ---
        String[] colunas = {"ID", "Nome Completo", "Login", "Perfil", "CRP", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabelaUsuarios.setRowHeight(25);
        tabelaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // --- PAINEL DE AÇÕES (BOTÕES) ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnNovo = new JButton("Novo Usuário");
        btnNovo.setBackground(new Color(46, 204, 113));
        btnNovo.setForeground(Color.WHITE);
        
        JButton btnEditar = new JButton("Editar Selecionado");
        
        JButton btnResetSenha = new JButton("Redefinir Senha");
        btnResetSenha.setBackground(new Color(241, 196, 15)); 
        btnResetSenha.setForeground(Color.BLACK);
        
        JButton btnStatus = new JButton("Ativar / Inativar Acesso");
        btnStatus.setForeground(new Color(231, 76, 60));

        painelBotoes.add(btnNovo);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnResetSenha);
        painelBotoes.add(btnStatus);
        add(painelBotoes, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnNovo.addActionListener(e -> {
            new TelaCadastroUsuario(this).setVisible(true);
            carregarDados(); 
        });

        btnEditar.addActionListener(e -> {
            int linhaSelecionada = tabelaUsuarios.getSelectedRow();
            if (linhaSelecionada == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um usuário na tabela para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Usuario usuarioSelecionado = listaUsuariosCache.get(linhaSelecionada);
            new TelaCadastroUsuario(this, usuarioSelecionado).setVisible(true);
            carregarDados(); 
        });

        btnResetSenha.addActionListener(e -> {
            int linhaSelecionada = tabelaUsuarios.getSelectedRow();
            if (linhaSelecionada == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para redefinir a senha.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Usuario usuario = listaUsuariosCache.get(linhaSelecionada);
            
            if (usuario.getLogin().equals("admin")) {
                JOptionPane.showMessageDialog(this, "Não é permitido redefinir a senha da conta de sistema (admin).", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String senhaTemporaria = "mudar123";
            int confirmacao = JOptionPane.showConfirmDialog(this, 
                    "Confirma a redefinição de senha para o usuário '" + usuario.getNome() + "'?\n\nA nova senha temporária será: " + senhaTemporaria, 
                    "Auditoria de Segurança", JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    new UsuarioDAO().redefinirSenhaAdmin(usuario.getId(), senhaTemporaria);
                    JOptionPane.showMessageDialog(this, "Senha redefinida com sucesso!\nInstrua o funcionário a entrar com a senha: " + senhaTemporaria, "Operação Concluída", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro de integração ao redefinir senha:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnStatus.addActionListener(e -> inativarUsuarioSelecionado());
    }

    private void carregarDados() {
        modeloTabela.setRowCount(0); 
        try {
            UsuarioDAO dao = new UsuarioDAO();
            listaUsuariosCache = dao.listarTodos();

            for (Usuario u : listaUsuariosCache) {
                String crp = (u instanceof Psicologo) ? ((Psicologo) u).getCrp() : "-";
                String status = u.isStatusAtivo() ? "ATIVO" : "INATIVO (Bloqueado)";

                modeloTabela.addRow(new Object[]{
                        u.getId(),
                        u.getNome(),
                        u.getLogin(),
                        u.getPerfil(),
                        crp,
                        status
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar usuários: " + ex.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inativarUsuarioSelecionado() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para alterar o seu status.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuario = listaUsuariosCache.get(linhaSelecionada);
        
        if (usuario.getLogin().equals("admin")) {
            JOptionPane.showMessageDialog(this, "Não é permitido inativar a conta de super administrador.", "Ação Negada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean novoStatus = !usuario.isStatusAtivo();
        String mensagem = novoStatus ? "Deseja RESTAURAR o acesso deste usuário?" : "Deseja BLOQUEAR o acesso deste usuário?\nEle não poderá fazer login, mas o histórico clínico será mantido.";

        int confirmacao = JOptionPane.showConfirmDialog(this, mensagem, "Confirmação de Status", JOptionPane.YES_NO_OPTION);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                new UsuarioDAO().inativar(usuario.getId(), novoStatus);
                carregarDados(); 
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao alterar status: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}