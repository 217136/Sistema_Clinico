package com.faculdade.sistema.clinica.util;

/**
 *
 * @author Amauri
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfiguradorBanco {
    private static Properties props = new Properties();

    static {
        // Tenta ler o arquivo da raiz do projeto
        try (InputStream input = new FileInputStream("banco.properties")) {
            props.load(input);
        } catch (IOException ex) {
            System.err.println("Erro ao carregar banco.properties: " + ex.getMessage());
        }
    }

    public static String getUrl() { return props.getProperty("db.url"); }
    public static String getUser() { return props.getProperty("db.user"); }
    public static String getPassword() { return props.getProperty("db.password"); }
}
