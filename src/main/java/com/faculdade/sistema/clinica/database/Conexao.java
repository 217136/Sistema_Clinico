package com.faculdade.sistema.clinica.database;

import com.faculdade.sistema.clinica.util.ConfiguradorBanco;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica conexões com o banco de dados PostgreSQL remoto (Supabase).
 * As credenciais são carregadas dinamicamente via arquivo banco.properties.
 */
public class Conexao {

    /**
     * Abre e retorna uma conexão ativa com o banco de dados.
     * @return java.sql.Connection
     * @throws SQLException Caso ocorram falhas de rede, host ou credenciais.
     */
    public static Connection conectar() throws SQLException {
        try {
            // Força o carregamento do Driver em ambientes Swing antigos se necessário
            Class.forName("org.postgresql.Driver");
            
            // Busca os dados configurados no arquivo externo
            String url = ConfiguradorBanco.getUrl();
            String usuario = ConfiguradorBanco.getUser();
            String senha = ConfiguradorBanco.getPassword();
            
            return DriverManager.getConnection(url, usuario, senha);
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC do PostgreSQL não foi localizado no projeto. Verifique o pom.xml.", e);
        }
    }
}