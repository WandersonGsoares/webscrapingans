-- Arquivo: sql_scripts/criar_tabelas.sql (Versão MySQL 8.0)

-- Criação da tabela operadoras_ativas
CREATE TABLE IF NOT EXISTS operadoras_ativas (
    RegistroANS VARCHAR(20) PRIMARY KEY,
    CNPJ VARCHAR(20),
    RazaoSocial VARCHAR(255),
    NomeFantasia VARCHAR(255),
    Modalidade VARCHAR(50),
    TipoPessoa VARCHAR(20),
    Logradouro VARCHAR(255),
    Numero VARCHAR(20),
    Complemento VARCHAR(100),
    Bairro VARCHAR(100),
    Municipio VARCHAR(100),
    UF CHAR(2),
    CEP VARCHAR(10),
    DDD VARCHAR(5),
    Telefone VARCHAR(20),
    Fax VARCHAR(20),
    EnderecoEletronico VARCHAR(255),
    DataRegistroANS DATE,
    SituacaoCadastro VARCHAR(50),
    DataSituacaoCadastro DATE,
    MotivoCancelamento VARCHAR(255),
    DataCancelamento DATE,
    NumeroProcesso VARCHAR(50)
);

-- Criação da tabela demonstracoes_contabeis_detalhe
CREATE TABLE IF NOT EXISTS demonstracoes_contabeis_detalhe (
    DATA DATE,
    REG_ANS VARCHAR(20),
    CD_CONTA_CONTABIL VARCHAR(50),
    DESCRICAO TEXT,
    VL_SALDO_INICIAL DECIMAL(18, 2),
    VL_SALDO_FINAL DECIMAL(18, 2),
    FOREIGN KEY (REG_ANS) REFERENCES operadoras_ativas(RegistroANS)
);