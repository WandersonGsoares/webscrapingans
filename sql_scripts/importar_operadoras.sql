-- Arquivo: sql_scripts/importar_operadoras.sql

LOAD DATA INFILE 'C:\Users\wande\OneDrive\Área de Trabalho\webscrapingans\data\Relatorio_cadop.csv'
INTO TABLE operadoras_ativas
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;