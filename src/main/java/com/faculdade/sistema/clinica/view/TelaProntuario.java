package com.faculdade.sistema.clinica.view;

/**
 *
 * @author Amauri
 */

import com.faculdade.sistema.clinica.dao.PacienteDAO;
import com.faculdade.sistema.clinica.model.Paciente;
import com.faculdade.sistema.clinica.model.Usuario;
import com.formdev.flatlaf.FlatClientProperties;
import com.faculdade.sistema.clinica.dao.EvolucaoDAO;
import com.faculdade.sistema.clinica.dao.ProntuarioDAO;
import com.faculdade.sistema.clinica.model.Evolucao;
import com.faculdade.sistema.clinica.model.Prontuario;
import com.faculdade.sistema.clinica.model.Psicologo;
import com.faculdade.sistema.clinica.util.GeradorPDF;

import javax.swing.JFileChooser;
import java.io.File;
import java.util.List;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface Gráfica para Gestão de Prontuários e Evoluções Clínicas.
 * Desenvolvida com o Design System corporativo da clínica.
 */
public class TelaProntuario extends JFrame {

    private JComboBox<Paciente> cbPacientes;
    private JTextArea txtHistorico;
    private JTextArea txtNovaEvolucao;
    private JButton btnSalvar;
    private JButton btnCancelar;
    private Usuario profissionalLogado;

    // --- DESIGN SYSTEM ---
    private final Color COR_PRIMARIA = new Color(41, 128, 185);
    private final Color COR_FUNDO = Color.WHITE;
    private final Color COR_TEXTO_ESCURO = new Color(44, 62, 80);
    private final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONTE_PADRAO = new Font("Segoe UI", Font.PLAIN, 14);

    public TelaProntuario(Usuario usuarioLogado) {
        this.profissionalLogado = usuarioLogado;

        setTitle("Sistema Clínico - Prontuário Eletrônico");
        setSize(700, 650); // Janela mais larga para leitura confortável de textos
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painelPrincipal = new JPanel(new BorderLayout(0, 20));
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // --- CABEÇALHO ---
        JPanel painelCabecalho = new JPanel(new GridLayout(2, 1));
        painelCabecalho.setBackground(COR_FUNDO);
        
        JLabel lblTitulo = new JLabel("Prontuário Clínico", SwingConstants.CENTER);
        lblTitulo.setFont(FONTE_TITULO);
        lblTitulo.setForeground(COR_PRIMARIA);
        
        JLabel lblSubtitulo = new JLabel("Profissional: " + profissionalLogado.getNome(), SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);
        
        painelCabecalho.add(lblTitulo);
        painelCabecalho.add(lblSubtitulo);
        painelPrincipal.add(painelCabecalho, BorderLayout.NORTH);

        // --- CORPO DO PRONTUÁRIO ---
        JPanel painelCorpo = new JPanel(new BorderLayout(0, 15));
        painelCorpo.setBackground(COR_FUNDO);

        // 1. Seleção de Paciente
        JPanel painelPaciente = new JPanel(new BorderLayout(10, 0));
        painelPaciente.setBackground(COR_FUNDO);
        JLabel lblPaciente = new JLabel("Selecione o Paciente: ");
        lblPaciente.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPaciente.setForeground(COR_TEXTO_ESCURO);
        
        cbPacientes = new JComboBox<>();
        cbPacientes.setFont(FONTE_PADRAO);
        carregarComboPacientes();
        
        painelPaciente.add(lblPaciente, BorderLayout.WEST);
        painelPaciente.add(cbPacientes, BorderLayout.CENTER);
        painelCorpo.add(painelPaciente, BorderLayout.NORTH);

        // 2. Área de Histórico e Nova Evolução (Dividida)
        JPanel painelTextos = new JPanel(new GridLayout(2, 1, 0, 15));
        painelTextos.setBackground(COR_FUNDO);

        // Histórico (Apenas Leitura)
        JPanel painelHist = new JPanel(new BorderLayout());
        painelHist.setBackground(COR_FUNDO);
        JLabel lblHist = new JLabel("Histórico de Evoluções (Leitura)");
        lblHist.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtHistorico = new JTextArea();
        txtHistorico.setFont(FONTE_PADRAO);
        txtHistorico.setLineWrap(true);
        txtHistorico.setWrapStyleWord(true);
        txtHistorico.setEditable(false); // Impede a alteração de registros antigos (Segurança)
        txtHistorico.setBackground(new Color(248, 249, 250)); // Fundo ligeiramente cinza para indicar que está bloqueado
        txtHistorico.setText("Selecione um paciente para carregar o histórico...\n\n(A integração com a tabela 'evolucoes' será feita na próxima etapa).");
        
        JScrollPane scrollHist = new JScrollPane(txtHistorico);
        painelHist.add(lblHist, BorderLayout.NORTH);
        painelHist.add(scrollHist, BorderLayout.CENTER);

        // Nova Evolução
        JPanel painelNovaEv = new JPanel(new BorderLayout());
        painelNovaEv.setBackground(COR_FUNDO);
        JLabel lblNova = new JLabel("Registrar Nova Evolução / Anamnese");
        lblNova.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtNovaEvolucao = new JTextArea();
        txtNovaEvolucao.setFont(FONTE_PADRAO);
        txtNovaEvolucao.setLineWrap(true);
        txtNovaEvolucao.setWrapStyleWord(true);
        txtNovaEvolucao.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Descreva aqui as observações da sessão de hoje...");
        
        JScrollPane scrollNova = new JScrollPane(txtNovaEvolucao);
        painelNovaEv.add(lblNova, BorderLayout.NORTH);
        painelNovaEv.add(scrollNova, BorderLayout.CENTER);

        painelTextos.add(painelHist);
        painelTextos.add(painelNovaEv);

        painelCorpo.add(painelTextos, BorderLayout.CENTER);
        painelPrincipal.add(painelCorpo, BorderLayout.CENTER);

        // --- BOTÕES DE AÇÃO ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        painelBotoes.setBackground(COR_FUNDO);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setForeground(COR_TEXTO_ESCURO);

        JButton btnExportarPDF = new JButton("Gerar PDF");
        btnExportarPDF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExportarPDF.setBackground(new Color(231, 76, 60)); // Vermelho elegante
        btnExportarPDF.setForeground(Color.WHITE);
        btnExportarPDF.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportarPDF.setFocusPainted(false);

        btnSalvar = new JButton("Salvar Evolução");
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalvar.setBackground(COR_PRIMARIA);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvar.setFocusPainted(false);

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnExportarPDF);
        painelBotoes.add(btnSalvar);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        add(painelPrincipal);

        // --- EVENTOS ---
        btnCancelar.addActionListener(e -> this.dispose());
        
        btnExportarPDF.addActionListener(e -> {
            Paciente pacienteSelecionado = (Paciente) cbPacientes.getSelectedItem();
            if (pacienteSelecionado == null) {
                JOptionPane.showMessageDialog(this, "Selecione um paciente para exportar o prontuário.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Abre a janela do Windows/Linux para o usuário escolher onde salvar
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar Prontuário em PDF");
            fileChooser.setSelectedFile(new File("Prontuario_" + pacienteSelecionado.getNome().replaceAll(" ", "_") + ".pdf"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivoSelecionado = fileChooser.getSelectedFile();
                
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    
                    ProntuarioDAO prontuarioDao = new ProntuarioDAO();
                    EvolucaoDAO evolucaoDao = new EvolucaoDAO();
                    
                    Prontuario prontuario = prontuarioDao.buscarOuCriarPorPaciente(pacienteSelecionado);
                    List<Evolucao> evolucoes = evolucaoDao.buscarPorProntuario(prontuario.getId(), profissionalLogado);
                    
                    GeradorPDF.exportarProntuario(pacienteSelecionado, evolucoes, arquivoSelecionado.getAbsolutePath());
                    
                    JOptionPane.showMessageDialog(this, "PDF gerado com sucesso!\nSalvo em: " + arquivoSelecionado.getAbsolutePath(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        
        btnSalvar.addActionListener(e -> {
            String textoEvolucao = txtNovaEvolucao.getText().trim();
            Paciente pacienteSelecionado = (Paciente) cbPacientes.getSelectedItem();

            if (textoEvolucao.isEmpty() || pacienteSelecionado == null) {
                JOptionPane.showMessageDialog(this, "Selecione um paciente e escreva a evolução.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Regra de Negócio POO: A sua classe Evolucao exige um Psicologo. Bloqueamos outros usuários aqui.
            if (!(profissionalLogado instanceof Psicologo)) {
                JOptionPane.showMessageDialog(this, "Apenas Psicólogos podem assinar evoluções clínicas.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                ProntuarioDAO prontuarioDao = new ProntuarioDAO();
                EvolucaoDAO evolucaoDao = new EvolucaoDAO();
                
                // Padrão Unit of Work simplificado: Garante o Prontuário, depois salva a Evolução
                Prontuario prontuario = prontuarioDao.buscarOuCriarPorPaciente(pacienteSelecionado);
                Evolucao novaEvolucao = new Evolucao(0, LocalDateTime.now(), textoEvolucao, (Psicologo) profissionalLogado);
                
                evolucaoDao.inserir(novaEvolucao, prontuario.getId());
                
                JOptionPane.showMessageDialog(this, "Evolução clínica assinada e registrada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                txtNovaEvolucao.setText(""); 
                carregarHistoricoPaciente(pacienteSelecionado); // Recarrega a tela atualizada

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro de integração com o banco: " + ex.getMessage(), "Erro Banco", JOptionPane.ERROR_MESSAGE);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
        
        // EVENTO NOVO: Gatilho para atualizar a leitura do histórico assim que você troca o paciente no ComboBox.
        cbPacientes.addActionListener(e -> {
            Paciente selecionado = (Paciente) cbPacientes.getSelectedItem();
            if (selecionado != null) carregarHistoricoPaciente(selecionado);
        });
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
    private void carregarHistoricoPaciente(Paciente paciente) {
        try {
            ProntuarioDAO prontuarioDao = new ProntuarioDAO();
            EvolucaoDAO evolucaoDao = new EvolucaoDAO();
            
            Prontuario prontuario = prontuarioDao.buscarOuCriarPorPaciente(paciente);
            java.util.List<Evolucao> evolucoes = evolucaoDao.buscarPorProntuario(prontuario.getId(), profissionalLogado);
            
            if (evolucoes.isEmpty()) {
                txtHistorico.setText("Nenhuma evolução registrada para este paciente.");
            } else {
                StringBuilder sb = new StringBuilder();
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                
                for (Evolucao ev : evolucoes) {
                    sb.append("Data da Sessão: ").append(ev.getDataHora().format(formatador)).append("\n");
                    sb.append("Profissional: ").append(ev.getPsicologoResponsavel().getNome()).append("\n");
                    sb.append("Evolução: \n").append(ev.getTextoRegistro()).append("\n");
                    sb.append("--------------------------------------------------\n");
                }
                txtHistorico.setText(sb.toString());
                txtHistorico.setCaretPosition(0); // Rola o texto de volta para o topo automaticamente
            }
        } catch (SQLException ex) {
            txtHistorico.setText("Falha ao carregar o histórico: " + ex.getMessage());
        }
    }
}