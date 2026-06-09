package com.faculdade.sistema.clinica.util;

/**
 *
 * @author Amauri
 */

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitário de segurança para criptografia de dados sensíveis (Padrão SHA-256).
 */
public class SegurancaUtil {

    /**
     * Gera um hash irreversível a partir de uma senha em texto puro.
     */
    public static String gerarHash(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro crítico: Algoritmo de criptografia não encontrado no sistema.", e);
        }
    }
}
