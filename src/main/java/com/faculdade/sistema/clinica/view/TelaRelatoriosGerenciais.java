package com.faculdade.sistema.clinica.view;

import com.faculdade.sistema.clinica.dao.RelatorioDAO;
import com.faculdade.sistema.clinica.util.GeradorPDF;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.PieSeries.PieSeriesRenderStyle; // IMPORTAÇÃO CORRIGIDA

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Interface Gráfica de Business Intelligence (BI).
 * Renderiza os KPIs da clínica utilizando a biblioteca XChart.
 */
public class TelaRelatoriosGerenciais extends JDialog {

    private final Color COR_PRIMARIA = new Color(41, 128, 185);
    private final Color COR_FUNDO = Color.WHITE;

    private Map<String, Integer> cacheEstatisticas;
    private Map<String, Integer> cacheProdutividade;

    public TelaRelatoriosGerenciais(Window parent) {
        super(parent, "Dashboard Gerencial - Indicadores Clínicos", ModalityType.APPLICATION_MODAL);
        setSize(1000, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COR_FUNDO);

        // Carrega os dados do banco antes de desenhar a tela
        carregarDadosDoBanco();
        initComponents();
    }

    private void carregarDadosDoBanco() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            RelatorioDAO dao = new RelatorioDAO();
            cacheEstatisticas = dao.obterEstatisticasAgendamentos();
            cacheProdutividade = dao.obterProdutividadeProfissionais();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar indicadores: " + ex.getMessage(), "Erro de BI", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void initComponents() {
        // --- CABEÇALHO ---
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setBackground(COR_PRIMARIA);
        painelTopo.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("Visão Executiva de Operações");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        painelTopo.add(lblTitulo, BorderLayout.WEST);

        // Botão de Exportação transferido para o Dashboard Visual
        JButton btnExportar = new JButton("Exportar Relatório PDF");
        btnExportar.setBackground(new Color(46, 204, 113));
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportar.setFocusPainted(false);
        btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportar.addActionListener(e -> acionarExportacaoPDF());
        
        painelTopo.add(btnExportar, BorderLayout.EAST);
        add(painelTopo, BorderLayout.NORTH);

        // --- ÁREA DOS GRÁFICOS (1 Linha, 2 Colunas) ---
        JPanel painelGraficos = new JPanel(new GridLayout(1, 2, 20, 20));
        painelGraficos.setBackground(COR_FUNDO);
        painelGraficos.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        painelGraficos.add(criarGraficoAbsenteismo());
        painelGraficos.add(criarGraficoProdutividade());

        add(painelGraficos, BorderLayout.CENTER);
    }

    /**
     * Constrói o Gráfico de Rosca para Ocupação e Absenteísmo
     */
    private JPanel criarGraficoAbsenteismo() {
        PieChart grafico = new PieChartBuilder().width(400).height(400)
                .title("Status de Agendamentos")
                .theme(Styler.ChartTheme.GGPlot2).build();

        // Customização visual do XChart
        grafico.getStyler().setLegendVisible(true);
        grafico.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS); // CORREÇÃO AQUI (OutsideS = Sul/Embaixo)
        grafico.getStyler().setDefaultSeriesRenderStyle(PieSeriesRenderStyle.Donut);
        grafico.getStyler().setChartBackgroundColor(COR_FUNDO);
        grafico.getStyler().setPlotBorderVisible(false);

        if (cacheEstatisticas != null && !cacheEstatisticas.isEmpty()) {
            for (Map.Entry<String, Integer> entry : cacheEstatisticas.entrySet()) {
                // Filtra o Total Geral para não distorcer o gráfico de pizza
                if (!entry.getKey().contains("Total")) {
                    grafico.addSeries(entry.getKey().replace("Consultas com status ", ""), entry.getValue());
                }
            }
        } else {
            grafico.addSeries("Sem Dados", 1);
        }

        return new XChartPanel<>(grafico);
    }

    /**
     * Constrói o Gráfico de Barras para Produtividade Médica
     */
    private JPanel criarGraficoProdutividade() {
        CategoryChart grafico = new CategoryChartBuilder().width(400).height(400)
                .title("Atendimentos por Profissional")
                .xAxisTitle("Psicólogos")
                .yAxisTitle("Volume de Consultas")
                .theme(Styler.ChartTheme.GGPlot2).build();

        grafico.getStyler().setLegendVisible(false);
        grafico.getStyler().setChartBackgroundColor(COR_FUNDO);
        grafico.getStyler().setPlotBorderVisible(false);
        // A LINHA DE ANOTAÇÕES FOI REMOVIDA PARA EVITAR CONFLITO DE VERSÃO

        List<String> profissionais = new ArrayList<>();
        List<Integer> atendimentos = new ArrayList<>();

        if (cacheProdutividade != null && !cacheProdutividade.isEmpty()) {
            for (Map.Entry<String, Integer> entry : cacheProdutividade.entrySet()) {
                profissionais.add(entry.getKey());
                atendimentos.add(entry.getValue());
            }
            grafico.addSeries("Produtividade", profissionais, atendimentos);
        } else {
            profissionais.add("Sem Dados");
            atendimentos.add(0);
            grafico.addSeries("Vazio", profissionais, atendimentos);
        }

        return new XChartPanel<>(grafico);
    }

    /**
     * Reaproveita a lógica de exportação legada da V1.0
     */
    private void acionarExportacaoPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório Gerencial");
        fileChooser.setSelectedFile(new File("Relatorio_Gerencial_" + LocalDate.now().toString() + ".pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = fileChooser.getSelectedFile();
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                GeradorPDF.exportarRelatorioGerencial(cacheEstatisticas, cacheProdutividade, arquivoSelecionado.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Relatório exportado com sucesso!\nSalvo em: " + arquivoSelecionado.getAbsolutePath(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar o documento PDF: " + ex.getMessage(), "Erro Crítico", JOptionPane.ERROR_MESSAGE);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
}