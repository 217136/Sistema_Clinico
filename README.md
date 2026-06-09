# 🏥 Sistema Clínico - Gestão de Prontuários Médicos

Este projeto é um Sistema de Gestão Clínica desenvolvido em **Java**, focado na segurança de dados, modularidade e usabilidade para o setor de saúde. O software foi estruturado aplicando rigorosas metodologias de Engenharia de Software para garantir a integridade dos registros médicos e o controle de acesso de perfis.

## 💻 Tecnologias e Arquitetura

O sistema foi concebido sob o paradigma da Programação Orientada a Objetos (POO) e utiliza os seguintes recursos:

* **Linguagem:** Java SE
* **Interface Gráfica (GUI):** Java Swing (Programação Orientada a Eventos)
* **Banco de Dados:** PostgreSQL
* **Padrões de Projeto:** * **MVC (Model-View-Controller):** Para segregação estrita entre a lógica de apresentação e as regras de negócios.
  * **DAO (Data Access Object):** Para abstração e encapsulamento das rotinas de acesso à camada de persistência.

## 🛡️ Segurança e Funcionalidades

* **Autenticação Segura:** Rotinas de criptografia (SHA-256) para armazenamento de credenciais.
* **Segregação de Perfis:** Restrições de navegação dinâmicas baseadas no nível de acesso do usuário (ex: Médico, Recepcionista, Administrador).
* **Gestão de Ambientes:** Separação das conexões de banco de dados (Desenvolvimento/Beta) por meio de arquivos de propriedades externos.

## 🚀 Como Configurar e Executar

### Pré-requisitos
* Java Development Kit (JDK) 11 ou superior.
* Banco de dados PostgreSQL rodando localmente ou em servidor.
* IDE de sua preferência (NetBeans fortemente recomendado para compatibilidade nativa da GUI).

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/217136/Sistema_Clinico](https://github.com/217136/Sistema_Clinico)

2. **Modelagem do Banco:**

Antes de rodar a aplicação, acesse o editor de consultas do seu painel do Supabase, copie o conteúdo do arquivo schema.sql disponível na raiz deste repositório e execute-o para gerar a estrutura de tabelas iniciais necessárias.