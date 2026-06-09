package com.faculdade.sistema.clinica; // A declaração do pacote DEVE ser a primeira linha

import com.faculdade.sistema.clinica.dao.UsuarioDAO;
import com.faculdade.sistema.clinica.model.Psicologo;
import com.faculdade.sistema.clinica.model.Usuario;
import com.faculdade.sistema.clinica.dao.PacienteDAO;
import com.faculdade.sistema.clinica.model.Paciente;
import java.time.LocalDate;
import java.util.List;

/**
 * Classe de teste automatizado para validação do fluxo clínico.
 * @author Amauri
 */
public class TestadorConexaoDAO {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO TESTE DO FLUXO CLÍNICO (DIAS 3 e 4) ===");
        
        PacienteDAO dao = new PacienteDAO();
        
        // Instanciando o modelo de Paciente
        Paciente novoPaciente = new Paciente(0, "Ana Nise da Silveira", "123.456.789-00", LocalDate.of(1995, 5, 20), "(11) 98888-7777");
        
        try {
            System.out.println("Tentando cadastrar paciente e gerar prontuário automático...");
            dao.inserir(novoPaciente);
            System.out.println("✅ Sucesso! Paciente e Prontuário criados no Supabase.");
            
            System.out.println("\nBuscando lista de pacientes cadastrados...");
            List<Paciente> pacientes = dao.listar();
            
            for (Paciente p : pacientes) {
                System.out.println("-> ID: " + p.getId() + " | Nome: " + p.getNome() + " | CPF: " + p.getCpf());
            }
            
        } catch (Exception e) {
            System.out.println("❌ FALHA NO TESTE DO FLUXO CLÍNICO!");
            System.out.println("Causa: " + e.getMessage());
            e.printStackTrace();
        }
    }
}