package com.wanderson.webscrapingans;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.IOException;
import java.net.URL;

public class AnexoIProcessor {

    public static String extrairTextoDoAnexoI(String urlAnexoI) {
        String textoExtraido = "";
        try {
            URL url = new URL(urlAnexoI);
            PDDocument documento = PDDocument.load(url.openStream());
            PDFTextStripper stripper = new PDFTextStripper();
            textoExtraido = stripper.getText(documento);
            documento.close();
            return textoExtraido;
        } catch (IOException e) {
            System.err.println("Erro ao extrair texto do Anexo I: " + e.getMessage());
            return null; // Ou outra forma de indicar erro
        }
    }

    // Outros métodos para processar o texto e extrair a tabela virão aqui...

    public static void main(String[] args) {
        // URL atualizada do Anexo I
        String urlAnexoI = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos/Anexo_I_Rol_2021RN_465.2021_RN627L.2024.pdf";
        String textoExtraido = AnexoIProcessor.extrairTextoDoAnexoI(urlAnexoI);

        if (textoExtraido != null) {
            System.out.println("Texto extraído com sucesso. Todo o texto é:");
            System.out.println(textoExtraido); // Imprime todo o texto extraído

            // Código para dividir o texto em linhas e encontrar o cabeçalho:
            String[] linhas = textoExtraido.split("\n");
            String linhaCabecalho = "VIGÊNCIA OD AMB HCO HSO REF PAC DUT SUBGRUPO GRUPO CAPÍTULO";
            int indiceCabecalho = -1;

            for (int i = 0; i < linhas.length; i++) {
                if (linhas[i].trim().equals(linhaCabecalho.trim())) {
                    indiceCabecalho = i;
                    break; // Encontramos o cabeçalho, podemos sair do loop
                }
            }

            if (indiceCabecalho != -1) {
                System.out.println("Linha de cabeçalho encontrada no índice: " + indiceCabecalho);

                // Imprimindo as primeiras linhas de dados (código anterior)
                System.out.println("\n--- Primeiras linhas de dados da tabela ---");
                for (int i = indiceCabecalho + 1; i < Math.min(indiceCabecalho + 10, linhas.length); i++) {
                    System.out.println("Linha " + i + ": " + linhas[i]);
                }
                System.out.println("-------------------------------------------\n");

                // Nova lógica refinada para identificar procedimentos (considerando CAPS LOCK e número de palavras)
                System.out.println("\n--- Procedimentos e suas informações de cobertura (CAPS LOCK e número de palavras) ---");
                String[] palavrasDeCobertura = {"OD", "AMB", "HCO", "HSO", "REF", "PAC", "DUT"};
                String[] inicioCobertura = {"OD", "AMB", "HCO", "HSO", "REF"}; // Palavras que geralmente iniciam a linha de cobertura
                for (int i = indiceCabecalho + 1; i < linhas.length; i++) {
                    String linha = linhas[i].trim();
                    boolean linhaDeCobertura = false;
                    for (String inicio : inicioCobertura) {
                        if (linha.startsWith(inicio)) {
                            int contadorCobertura = 0;
                            for (String palavra : palavrasDeCobertura) {
                                if (linha.contains(palavra)) {
                                    contadorCobertura++;
                                }
                            }
                            if (contadorCobertura >= 2) { // Requer pelo menos duas palavras de cobertura se começar com uma das principais
                                linhaDeCobertura = true;
                                break;
                            }
                        }
                    }

                    if (linhaDeCobertura) {
                        StringBuilder nomeProcedimento = new StringBuilder();
                        boolean encontrouNome = false;
                        // Olhar para as até 5 linhas anteriores
                        for (int j = i - 1; j > indiceCabecalho; j--) {
                            String linhaAnterior = linhas[j].trim();
                            boolean contemCoberturaAnterior = false;
                            int contadorCoberturaAnterior = 0;
                            for (String palavraCobertura : palavrasDeCobertura) {
                                if (linhaAnterior.contains(palavraCobertura)) {
                                    contemCoberturaAnterior = true;
                                    contadorCoberturaAnterior++;
                                }
                            }

                            // Condições de parada aprimoradas (considerando a nova informação)
                            if (linhaAnterior.startsWith("Rol de Procedimentos") && nomeProcedimento.length() > 0) {
                                break; // Parar se encontrar o início do título e já tivermos algo no nome
                            } else if (linhaAnterior.equals(linhaAnterior.toUpperCase()) && linhaAnterior.split("\\s+").length <= 6 && nomeProcedimento.length() > 0) {
                                // Aumentei um pouco o limite de palavras para linhas em CAPS LOCK
                                break; // Parar se encontrar uma linha curta em maiúsculo (provável título/categoria)
                            } else if (contadorCoberturaAnterior >= 2 && nomeProcedimento.length() > 0) {
                                break; // Parar se encontrar outra linha que parece ser de cobertura
                            } else if (linhaAnterior.isEmpty() && nomeProcedimento.length() > 0) {
                                break; // Parar se encontrar uma linha vazia
                            } else if (!linhaAnterior.isEmpty() && !contemCoberturaAnterior && !linhaAnterior.equals(linhaAnterior.toUpperCase())) {
                                nomeProcedimento.insert(0, linhaAnterior + " ");
                                encontrouNome = true; // Marcamos que encontramos parte do nome
                            } else if (j <= indiceCabecalho) {
                                break; // Parar se alcançar a linha do cabeçalho
                            }
                        }
                        if (encontrouNome) { // Só imprimimos se encontramos algo que parece ser o nome
                            System.out.println("Procedimento (potencial): " + nomeProcedimento.toString().trim());
                            System.out.println("Cobertura: " + linha);
                            System.out.println("--------------------------------------------------");
                        }
                    }
                }
                System.out.println("--------------------------------------------------------------------------------------\n");

                // Próximo passo: processar as linhas abaixo do cabeçalho
            } else {
                System.out.println("Linha de cabeçalho não encontrada.");
            }
        } else {
            System.out.println("Ocorreu um erro ao extrair o texto do PDF.");
        }
    }
}