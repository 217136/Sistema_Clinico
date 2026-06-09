package com.faculdade.sistema.clinica.util;

import com.faculdade.sistema.clinica.model.Evolucao;
import com.faculdade.sistema.clinica.model.Paciente;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe utilitária responsável por gerar documentos em Portable Document Format (PDF) do sistema.
 * Configurada com padrões médico-legais e adequação à Lei Geral de Proteção de Dados Pessoais (LGPD) (pt-BR).
 */
public class GeradorPDF {

    public static void exportarProntuario(Paciente paciente, List<Evolucao> evolucoes, String caminhoArquivo) throws Exception {
        Document documento = new Document();
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(caminhoArquivo));

        // Acopla o evento que desenha o rodapé legal e a numeração em todas as páginas
        writer.setPageEvent(new RodapeLegalEvento());

        documento.open();

        // --- TIPOGRAFIA CORPORATIVA ---
        Font fonteTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(44, 62, 80));
        Font fonteSubtitulo = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(41, 128, 185));
        Font fonteNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        Font fontePequena = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);

        // --- CABEÇALHO DO DOCUMENTO ---
        Paragraph titulo = new Paragraph("PRONTUÁRIO CLÍNICO ELETRÔNICO", fonteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);
        
        documento.add(new Paragraph(" "));

        // --- DADOS DE IDENTIFICAÇÃO (MÉDICO-LEGAL) ---
        PdfPTable tabelaCabecalho = new PdfPTable(2);
        tabelaCabecalho.setWidthPercentage(100);
        
        PdfPCell celulaPaciente = new PdfPCell(new Phrase("Paciente: " + paciente.getNome() + "\nCadastro de Pessoas Físicas (CPF): " + paciente.getCpf(), fonteSubtitulo));
        celulaPaciente.setBorder(Rectangle.NO_BORDER);
        
        String dataGeracao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss"));
        PdfPCell celulaData = new PdfPCell(new Phrase("Documento gerado em:\n" + dataGeracao, fontePequena));
        celulaData.setBorder(Rectangle.NO_BORDER);
        celulaData.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tabelaCabecalho.addCell(celulaPaciente);
        tabelaCabecalho.addCell(celulaData);
        documento.add(tabelaCabecalho);
        
        documento.add(new Paragraph(" "));
        documento.add(new Paragraph(" "));

        // --- TABELA DE HISTÓRICO CLÍNICO ---
        PdfPTable tabela = new PdfPTable(3);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{2f, 3.5f, 4.5f}); 

        // Estilização dos Cabeçalhos da Tabela
        Font fonteCabecalhoTabela = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        BaseColor corFundoCabecalho = new BaseColor(41, 128, 185); // Azul Primário do Design System

        PdfPCell celulaDataSessao = new PdfPCell(new Phrase("Data da Sessão", fonteCabecalhoTabela));
        celulaDataSessao.setBackgroundColor(corFundoCabecalho);
        celulaDataSessao.setPadding(8f);

        PdfPCell celulaProfissional = new PdfPCell(new Phrase("Profissional Responsável", fonteCabecalhoTabela));
        celulaProfissional.setBackgroundColor(corFundoCabecalho);
        celulaProfissional.setPadding(8f);

        PdfPCell celulaEvolucao = new PdfPCell(new Phrase("Evolução Clínica", fonteCabecalhoTabela));
        celulaEvolucao.setBackgroundColor(corFundoCabecalho);
        celulaEvolucao.setPadding(8f);

        tabela.addCell(celulaDataSessao);
        tabela.addCell(celulaProfissional);
        tabela.addCell(celulaEvolucao);

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Preenchimento Dinâmico
        if (evolucoes == null || evolucoes.isEmpty()) {
            PdfPCell celulaVazia = new PdfPCell(new Phrase("Nenhuma evolução clínica registrada no sistema até a presente data.", fonteNormal));
            celulaVazia.setColspan(3);
            celulaVazia.setPadding(10f);
            celulaVazia.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabela.addCell(celulaVazia);
        } else {
            for (Evolucao ev : evolucoes) {
                PdfPCell cell1 = new PdfPCell(new Phrase(ev.getDataHora().format(formatador), fonteNormal));
                cell1.setPadding(5f);
                
                // Exibe nome e o registro profissional (CRP)
                String dadosProfissional = ev.getPsicologoResponsavel().getNome() + "\nConselho Regional de Psicologia (CRP): " + ev.getPsicologoResponsavel().getCrp();
                PdfPCell cell2 = new PdfPCell(new Phrase(dadosProfissional, fonteNormal));
                cell2.setPadding(5f);
                
                PdfPCell cell3 = new PdfPCell(new Phrase(ev.getTextoRegistro(), fonteNormal));
                cell3.setPadding(5f);

                tabela.addCell(cell1);
                tabela.addCell(cell2);
                tabela.addCell(cell3);
            }
        }

        documento.add(tabela);
        documento.close();
    }

    /**
     * Classe Interna para interceptar o fim de cada página e injetar o rodapé.
     */
    static class RodapeLegalEvento extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font fonteRodape = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
            
            String textoConfidencialidade = "AVISO DE CONFIDENCIALIDADE DA LEI GERAL DE PROTEÇÃO DE DADOS PESSOAIS (LGPD): Este documento contém informações médicas sensíveis e protegidas por lei. Sua visualização, compartilhamento ou reprodução por pessoas não autorizadas é estritamente proibida.";
            
            // 1. Paginação (Canto inferior direito)
            Phrase paginacao = new Phrase("Página " + writer.getPageNumber(), fonteRodape);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, paginacao, document.right(), 20, 0);

            // 2. Texto Confidencialidade com Quebra de Linha (Word Wrap)
            ColumnText ct = new ColumnText(cb);
            
            // CORREÇÃO: Aumentamos o limite superior (Y) de 'document.bottom() - 5' para '50'.
            // Isso cria uma caixa com 35 pontos de altura (50 - 15), espaço de sobra para múltiplas linhas.
            ct.setSimpleColumn(new Rectangle(document.left(), 15, document.right() - 60, 50));

            Paragraph paragrafoRodape = new Paragraph(textoConfidencialidade, fonteRodape);
            paragrafoRodape.setAlignment(Element.ALIGN_CENTER);

            ct.addElement(paragrafoRodape);
            
            try {
                ct.go();
            } catch (Exception e) {
                // Ignora falha de layout apenas no rodapé
            }
        }
    }
    /**
     * Exporta os indicadores de desempenho consolidados para um documento gerencial estruturado.
     * Utiliza formatação visual alinhada ao Design System da clínica.
     */
    public static void exportarRelatorioGerencial(java.util.Map<String, Integer> estatisticas, java.util.Map<String, Integer> produtividade, String caminhoArquivo) throws Exception {
        com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
        com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(caminhoArquivo));
        
        documento.open();
        
        // Definição da Paleta de Cores Red, Green, Blue (RGB)
        com.itextpdf.text.BaseColor corPrimaria = new com.itextpdf.text.BaseColor(41, 128, 185); // Azul corporativo
        com.itextpdf.text.BaseColor corTextoBranco = com.itextpdf.text.BaseColor.WHITE;
        
        // Fontes padronizadas
        com.itextpdf.text.Font fonteTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, corPrimaria);
        com.itextpdf.text.Font fonteCabecalhoTabela = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, corTextoBranco);
        com.itextpdf.text.Font fonteLinhaTabela = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.BLACK);
        
        // Título do Relatório
        com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("Relatório Gerencial de Operações Clínicas", fonteTitulo);
        titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        documento.add(titulo);
        documento.add(new com.itextpdf.text.Paragraph(" "));
        documento.add(new com.itextpdf.text.Paragraph(" "));
        
        // --- SEÇÃO 1: Tabela de Ocupação e Absenteísmo ---
        com.itextpdf.text.pdf.PdfPTable tabelaEstatisticas = new com.itextpdf.text.pdf.PdfPTable(2);
        tabelaEstatisticas.setWidthPercentage(100);
        tabelaEstatisticas.setSpacingAfter(20f);
        
        // Cabeçalho da Tabela 1
        com.itextpdf.text.pdf.PdfPCell celulaCabecalho1 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("1. Indicadores de Ocupação e Absenteísmo", fonteCabecalhoTabela));
        celulaCabecalho1.setColspan(2);
        celulaCabecalho1.setBackgroundColor(corPrimaria);
        celulaCabecalho1.setPadding(8f);
        celulaCabecalho1.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        tabelaEstatisticas.addCell(celulaCabecalho1);
        
        // Preenchimento de Dados (Tabela 1)
        for (java.util.Map.Entry<String, Integer> entry : estatisticas.entrySet()) {
            com.itextpdf.text.pdf.PdfPCell celulaDescricao = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(entry.getKey(), fonteLinhaTabela));
            celulaDescricao.setPadding(6f);
            
            com.itextpdf.text.pdf.PdfPCell celulaValor = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(entry.getValue()), fonteLinhaTabela));
            celulaValor.setPadding(6f);
            celulaValor.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            
            tabelaEstatisticas.addCell(celulaDescricao);
            tabelaEstatisticas.addCell(celulaValor);
        }
        documento.add(tabelaEstatisticas);
        
        // --- SEÇÃO 2: Tabela de Produtividade ---
        com.itextpdf.text.pdf.PdfPTable tabelaProdutividade = new com.itextpdf.text.pdf.PdfPTable(2);
        tabelaProdutividade.setWidthPercentage(100);
        tabelaProdutividade.setSpacingAfter(30f);
        
        // Cabeçalho da Tabela 2
        com.itextpdf.text.pdf.PdfPCell celulaCabecalho2 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("2. Produtividade Operacional por Profissional", fonteCabecalhoTabela));
        celulaCabecalho2.setColspan(2);
        celulaCabecalho2.setBackgroundColor(corPrimaria);
        celulaCabecalho2.setPadding(8f);
        celulaCabecalho2.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        tabelaProdutividade.addCell(celulaCabecalho2);
        
        // Preenchimento de Dados (Tabela 2)
        for (java.util.Map.Entry<String, Integer> entry : produtividade.entrySet()) {
            com.itextpdf.text.pdf.PdfPCell celulaProfissional = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Profissional: " + entry.getKey(), fonteLinhaTabela));
            celulaProfissional.setPadding(6f);
            
            com.itextpdf.text.pdf.PdfPCell celulaAtendimentos = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(entry.getValue()) + " atendimento(s)", fonteLinhaTabela));
            celulaAtendimentos.setPadding(6f);
            celulaAtendimentos.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            
            tabelaProdutividade.addCell(celulaProfissional);
            tabelaProdutividade.addCell(celulaAtendimentos);
        }
        documento.add(tabelaProdutividade);
        
        // --- RODAPÉ ---
        com.itextpdf.text.Font fonteRodape = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC, com.itextpdf.text.BaseColor.GRAY);
        com.itextpdf.text.Paragraph rodape = new com.itextpdf.text.Paragraph("Documento corporativo gerado automaticamente pelo Sistema Clínico em " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fonteRodape);
        rodape.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        documento.add(rodape);
        
        documento.close();
    }
}