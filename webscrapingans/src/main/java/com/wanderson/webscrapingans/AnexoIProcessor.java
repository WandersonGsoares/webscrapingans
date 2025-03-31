package com.wanderson.webscrapingans;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.IOException;
import java.net.URL;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public static void main(String[] args) {
        String urlAnexoI = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos/Anexo_I_Rol_2021RN_465.2021_RN627L.2024.pdf";
        String textoExtraido = AnexoIProcessor.extrairTextoDoAnexoI(urlAnexoI);
        String nomeArquivoCsv = "anexo_i.csv";
        String nomeArquivoZip = "Teste_wanderson.zip";

        if (textoExtraido != null) {
            String[] linhas = textoExtraido.split("\n");
            String linhaCabecalho = "VIGÊNCIA OD AMB HCO HSO REF PAC DUT SUBGRUPO GRUPO CAPÍTULO";
            int indiceCabecalho = -1;

            for (int i = 0; i < linhas.length; i++) {
                if (linhas[i].trim().equals(linhaCabecalho.trim())) {
                    indiceCabecalho = i;
                    break;
                }
            }

            if (indiceCabecalho != -1) {
                try (FileWriter writer = new FileWriter(nomeArquivoCsv)) {
                    writer.write("PROCEDIMENTO,OD,AMB,HCO,HSO,REF,PAC,DUT,SUBGRUPO_GRUPO_CAPÍTULO\n");
                    List<String> coberturas = Arrays.asList("OD", "AMB", "HCO", "HSO", "REF", "PAC", "DUT");

                    for (int i = indiceCabecalho + 1; i < linhas.length; i++) {
                        String linha = linhas[i].trim();
                        if (!linha.isEmpty() && !linha.startsWith("RN")) {
                            String procedimento = "";
                            String od = "";
                            String amb = "";
                            String hco = "";
                            String hso = "";
                            String ref = "";
                            String pac = "";
                            String dut = "";
                            String subgrupoGrupoCapitulo = "";

                            int primeiroIndiceCobertura = -1;
                            for (String cobertura : coberturas) {
                                int indice = linha.indexOf(cobertura);
                                if (indice != -1) {
                                    primeiroIndiceCobertura = Math.min(primeiroIndiceCobertura == -1 ? indice : primeiroIndiceCobertura, indice);
                                }
                            }

                            if (primeiroIndiceCobertura != -1) {
                                procedimento = linha.substring(0, primeiroIndiceCobertura).trim();
                                String restante = linha.substring(primeiroIndiceCobertura).trim();

                                for (String cobertura : coberturas) {
                                    if (restante.contains(cobertura)) {
                                        if (cobertura.equals("OD")) od = "Seg. Odontológica";
                                        else if (cobertura.equals("AMB")) amb = "Seg. Ambulatorial";
                                        else if (cobertura.equals("HCO")) hco = "Sim";
                                        else if (cobertura.equals("HSO")) hso = "Sim";
                                        else if (cobertura.equals("REF")) ref = "Sim";
                                        else if (cobertura.equals("PAC")) pac = "Sim";
                                        else if (cobertura.equals("DUT")) dut = "Sim";
                                    }
                                }
                                int indiceUltimaCobertura = -1;
                                for (String cobertura : coberturas) {
                                    int indice = restante.lastIndexOf(cobertura);
                                    if (indice > indiceUltimaCobertura) {
                                        indiceUltimaCobertura = indice + cobertura.length();
                                    }
                                }
                                if (indiceUltimaCobertura > 0 && indiceUltimaCobertura < restante.length()) {
                                    subgrupoGrupoCapitulo = restante.substring(indiceUltimaCobertura).trim();
                                }

                                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                        procedimento, od, amb, hco, hso, ref, pac, dut, subgrupoGrupoCapitulo));

                            } else if (!linha.startsWith("Rol")) {
                                System.out.println("Linha sem cobertura identificada: " + linha);
                            }
                        }
                    }
                    System.out.println("Dados salvos em " + nomeArquivoCsv);

                } catch (IOException e) {
                    System.err.println("Erro ao salvar o arquivo CSV: " + e.getMessage());
                    return; // Importante sair do método em caso de erro no CSV
                }

                // Agora vamos criar o arquivo ZIP
                try (FileOutputStream fos = new FileOutputStream(nomeArquivoZip);
                     ZipOutputStream zipOut = new ZipOutputStream(fos);
                     FileInputStream fis = new FileInputStream(nomeArquivoCsv)) {

                    File arquivoCsv = new File(nomeArquivoCsv);
                    ZipEntry zipEntry = new ZipEntry(arquivoCsv.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    System.out.println("Arquivo CSV compactado em " + nomeArquivoZip);

                } catch (IOException e) {
                    System.err.println("Erro ao criar o arquivo ZIP: " + e.getMessage());
                } finally {
                    File arquivoCsv = new File(nomeArquivoCsv);
                    if (arquivoCsv.exists()) {
                        arquivoCsv.delete();
                        System.out.println("Arquivo CSV temporário apagado.");
                    }
                }

            } else {
                System.out.println("Linha de cabeçalho não encontrada.");
            }
        } else {
            System.out.println("Ocorreu um erro ao extrair o texto do PDF.");
        }
    }
}