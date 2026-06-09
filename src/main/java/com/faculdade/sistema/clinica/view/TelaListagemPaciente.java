package com.faculdade.sistema.clinica.view;

/**
 *
 * @author Amauri
 */

import com.faculdade.sistema.clinica.dao.PacienteDAO;
import com.faculdade.sistema.clinica.model.Paciente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Interface Gráfica para Gestão e Listagem de Pacientes.
 */
public class TelaListagemPaciente extends JFrame {

    private JTable tabelaPacientes;
    private DefaultTableModel modeloTabela;
    private List<Paciente> listaCache;

    public TelaListagemPaciente() {
        setTitle("Módulo de Recepção - Listagem de Pacientes");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();
        carregarDados();
    }

    private void initComponents() {
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTopo.setBackground(new Color(41, 128, 185));
        
        JLabel lblTitulo = new JLabel("  Gestão de Pacientes");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        painelTopo.add(lblTitulo);
        add(painelTopo, BorderLayout.NORTH);

        String[] colunas = {"ID", "Nome Completo", "CPF", "Data Nasc.", "Telefone"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabelaPacientes = new JTable(modeloTabela);
        tabelaPacientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaPacientes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabelaPacientes.setRowHeight(25);

        add(new JScrollPane(tabelaPacientes), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnNovo = new JButton("Novo Paciente");
        btnNovo.setBackground(new Color(46, 204, 113));
        btnNovo.setForeground(Color.WHITE);
        
        JButton btnEditar = new JButton("Editar Selecionado");

        painelBotoes.add(btnNovo);
        painelBotoes.add(btnEditar);
        add(painelBotoes, BorderLayout.SOUTH);

        btnNovo.addActionListener(e -> {
            new TelaCadastroPaciente(this).setVisible(true); // O 'this' ativa o congelamento modal
            carregarDados(); // Agora só executa DEPOIS que o formulário fechar!
        });

        btnEditar.addActionListener(e -> {
            int linha = tabelaPacientes.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um paciente na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Paciente paciente = listaCache.get(linha);
            new TelaCadastroPaciente(this, paciente).setVisible(true); // O 'this' ativa o congelamento modal
            carregarDados(); // Recarrega automaticamente mostrando a edição
        });
    }

    private void carregarDados() {
        modeloTabela.setRowCount(0);
        try {
            listaCache = new PacienteDAO().listar();
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (Paciente p : listaCache) {
                String cpf = p.getCpf();
                String cpfFormatado = (cpf != null && cpf.length() == 11) ? 
                        cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11) : cpf;
                        
                modeloTabela.addRow(new Object[]{
                    p.getId(), p.getNome(), cpfFormatado, p.getDataNascimento().format(formatador), p.getTelefone()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}