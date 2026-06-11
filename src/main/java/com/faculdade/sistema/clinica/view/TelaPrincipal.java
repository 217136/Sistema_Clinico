package com.faculdade.sistema.clinica.view;

import com.faculdade.sistema.clinica.dao.AgendamentoDAO;
import com.faculdade.sistema.clinica.model.Agendamento;
import com.faculdade.sistema.clinica.model.Usuario;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Interface Gráfica do Dashboard Principal.
 * Inclui o painel lateral de Agenda Diária dinâmico para o corpo clínico.
 */
public class TelaPrincipal extends JFrame {
    
    private Usuario usuarioLogado;
    private JTable tabelaAgendaDiaria;
    private DefaultTableModel modeloAgenda;

    // --- DESIGN SYSTEM ---
    private final Color COR_PRIMARIA = new Color(41, 128, 185); 
    private final Color COR_FUNDO = Color.WHITE;
    private final Color COR_TEXTO_ESCURO = new Color(44, 62, 80);
    private final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 14);

    public TelaPrincipal(Usuario usuario) {
        this.usuarioLogado = usuario;

        setTitle("Sistema Clínico - Dashboard Principal");
        setSize(1100, 650); // Ligeiramente ampliado para acomodar o painel lateral
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COR_FUNDO);

        // --- CABEÇALHO (Hero Header) ---
        JPanel painelTopo = new JPanel();
        painelTopo.setLayout(new BoxLayout(painelTopo, BoxLayout.Y_AXIS));
        painelTopo.setBackground(COR_PRIMARIA); 
        painelTopo.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0)); 
        
        JLabel lblBoasVindas = new JLabel("Olá, " + usuarioLogado.getNome());
        lblBoasVindas.setForeground(Color.WHITE);
        lblBoasVindas.setFont(FONTE_TITULO);
        lblBoasVindas.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblPerfil = new JLabel("Nível de Acesso: " + usuarioLogado.getClass().getSimpleName().toUpperCase());
        lblPerfil.setForeground(new Color(236, 240, 241)); 
        lblPerfil.setFont(FONTE_SUBTITULO);
        lblPerfil.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelTopo.add(lblBoasVindas);
        painelTopo.add(Box.createRigidArea(new Dimension(0, 10))); 
        painelTopo.add(lblPerfil);
        
        add(painelTopo, BorderLayout.NORTH);

        // --- ÁREA CENTRAL (Divisão entre Botões e Agenda) ---
        JPanel painelAreaDeTrabalho = new JPanel(new BorderLayout());
        painelAreaDeTrabalho.setBackground(COR_FUNDO);

        // 1. MENU DE BOTÕES (Matriz)
        JPanel painelBotoes = new JPanel(new GridLayout(2, 3, 20, 20)); 
        painelBotoes.setBackground(COR_FUNDO);
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40)); 

        JButton btnPacientes = criarBotaoMenu("Gestão de Pacientes", "Cadastrar e gerenciar ficha clínica");
        JButton btnAgendamentos = criarBotaoMenu("Agenda de Consultas", "Marcar e verificar horários livres");
        JButton btnProntuarios = criarBotaoMenu("Prontuários Clínicos", "Registrar evolução e anamnese");
        JButton btnUsuarios = criarBotaoMenu("Gestão de Identidade", "Cadastrar novos funcionários (RH)");
        JButton btnRelatorios = criarBotaoMenu("Relatórios Gerenciais", "Estatísticas e exportação de dados");
        JButton btnSair = criarBotaoMenu("Encerrar Sessão", "Sair do sistema com segurança");
        btnSair.setForeground(new Color(231, 76, 60)); 

        boolean isPsicologo = usuarioLogado instanceof com.faculdade.sistema.clinica.model.Psicologo;
        boolean isAdmin = usuarioLogado instanceof com.faculdade.sistema.clinica.model.Administrador;

        btnProntuarios.setEnabled(isPsicologo); 
        btnUsuarios.setEnabled(isAdmin);        
        btnRelatorios.setEnabled(isAdmin);      

        painelBotoes.add(btnPacientes);
        painelBotoes.add(btnAgendamentos);
        painelBotoes.add(btnProntuarios);
        painelBotoes.add(btnUsuarios);
        painelBotoes.add(btnRelatorios); 
        painelBotoes.add(btnSair);

        painelAreaDeTrabalho.add(painelBotoes, BorderLayout.CENTER);

        // 2. DASHBOARD DIÁRIO (Visível apenas para Psicólogos)
        if (isPsicologo) {
            JPanel painelAgendaLateral = criarPainelAgendaDiaria();
            painelAreaDeTrabalho.add(painelAgendaLateral, BorderLayout.EAST);
            carregarAgendaDoDia(); // Preenche os dados no momento da abertura
        }

        add(painelAreaDeTrabalho, BorderLayout.CENTER);
        
        // --- BARRA DE MENUS ---
        JMenuBar barraMenus = new JMenuBar();
        JMenu menuConta = new JMenu("Minha Conta (" + usuarioLogado.getNome() + ")");
        menuConta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JMenuItem itemAlterarSenha = new JMenuItem("Alterar Minha Senha");
        itemAlterarSenha.addActionListener(e -> new TelaAlterarSenha(this, usuarioLogado).setVisible(true));
        menuConta.add(itemAlterarSenha);
        
        // Futuro link para a Agenda Global
        if(isPsicologo) {
            JMenu menuCalendario = new JMenu("Calendário Clínico");
            menuCalendario.setFont(new Font("Segoe UI", Font.BOLD, 12));
            JMenuItem itemAgendaGlobal = new JMenuItem("Abrir Agenda Global (Mensal/Semanal)");
            itemAgendaGlobal.addActionListener(e -> new TelaAgendaGlobal(this, usuarioLogado).setVisible(true));
            menuCalendario.add(itemAgendaGlobal);
            barraMenus.add(menuCalendario);
        }
        
        barraMenus.add(menuConta);
        setJMenuBar(barraMenus); 

        // --- EVENTOS DE NAVEGAÇÃO ---
        btnPacientes.addActionListener(e -> new TelaListagemPaciente().setVisible(true));
        btnAgendamentos.addActionListener(e -> {
            new TelaAgendamento(this, usuarioLogado).setVisible(true);
            if (isPsicologo) carregarAgendaDoDia(); // Atualiza o painel caso tenha marcado algo novo hoje
        });
        btnProntuarios.addActionListener(e -> new TelaProntuario(usuarioLogado).setVisible(true));
        btnUsuarios.addActionListener(e -> new TelaListagemUsuario(this).setVisible(true));
        
        // Evento Atualizado (V2.0): Abre o Dashboard Gráfico de BI
        btnRelatorios.addActionListener(e -> new TelaRelatoriosGerenciais(this).setVisible(true));
        
        btnSair.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
            this.dispose();
        });
    }

    /**
     * Constrói o layout do painel lateral da Agenda.
     */
    private JPanel criarPainelAgendaDiaria() {
        JPanel painelAgenda = new JPanel(new BorderLayout(0, 10));
        painelAgenda.setPreferredSize(new Dimension(350, 0));
        painelAgenda.setBackground(new Color(248, 249, 250)); // Fundo levemente acinzentado para destacar
        painelAgenda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(223, 228, 234)), // Linha divisória
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTituloAgenda = new JLabel("Atendimentos de Hoje");
        lblTituloAgenda.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloAgenda.setForeground(COR_PRIMARIA);
        
        JLabel lblData = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
        lblData.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblData.setForeground(Color.GRAY);
        
        JPanel painelTitulos = new JPanel(new GridLayout(2, 1));
        painelTitulos.setOpaque(false);
        painelTitulos.add(lblTituloAgenda);
        painelTitulos.add(lblData);
        painelAgenda.add(painelTitulos, BorderLayout.NORTH);

        // Tabela para listar os horários
        String[] colunas = {"Horário", "Paciente", "Status"};
        modeloAgenda = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaAgendaDiaria = new JTable(modeloAgenda);
        tabelaAgendaDiaria.setRowHeight(35);
        tabelaAgendaDiaria.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaAgendaDiaria.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaAgendaDiaria.getColumnModel().getColumn(0).setPreferredWidth(60); // Horário mais estreito
        tabelaAgendaDiaria.getColumnModel().getColumn(1).setPreferredWidth(180); // Nome mais largo
        
        JScrollPane scrollPane = new JScrollPane(tabelaAgendaDiaria);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(223, 228, 234)));
        painelAgenda.add(scrollPane, BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("Atualizar Agenda");
        btnAtualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(e -> carregarAgendaDoDia());
        painelAgenda.add(btnAtualizar, BorderLayout.SOUTH);

        return painelAgenda;
    }

    /**
     * Busca os dados no DAO e preenche a tabela do Dashboard.
     */
    private void carregarAgendaDoDia() {
        modeloAgenda.setRowCount(0);
        try {
            AgendamentoDAO dao = new AgendamentoDAO();
            List<Agendamento> agendamentos = dao.buscarAgendamentosDoDia(usuarioLogado.getId(), LocalDate.now());
            
            DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");
            
            for (Agendamento a : agendamentos) {
                modeloAgenda.addRow(new Object[]{
                    a.getDataHora().format(fmtHora),
                    a.getPaciente().getNome(),
                    a.getStatus()
                });
            }
            
            if (agendamentos.isEmpty()) {
                modeloAgenda.addRow(new Object[]{"-", "Nenhum atendimento", "-"});
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar agenda do dia: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fábrica de botões: Garante que todos os botões do menu tenham exatamente a mesma estética,
     * com margens internas (padding) adequadas e centralização absoluta.
     */
    private JButton criarBotaoMenu(String titulo, String subtitulo) {
        // A tag <center> centraliza o texto no eixo HTML
        JButton botao = new JButton("<html><center><b style='font-size:15px;'>" + titulo + "</b><br><br><span style='font-size:11px; color:#7f8c8d;'>" + subtitulo + "</span></center></html>");
        botao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        botao.setBackground(Color.WHITE);
        botao.setForeground(COR_TEXTO_ESCURO);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setFocusPainted(false); 
        
        // Garante a centralização absoluta do bloco de texto no eixo do componente Java
        botao.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Aplicação do Padding: Borda composta por uma linha externa e um espaço vazio interno
        botao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1), // Borda cinza externa
            BorderFactory.createEmptyBorder(20, 15, 20, 15) // Margem interna: Topo, Esquerda, Baixo, Direita
        )); 
        
        return botao;
    }
}