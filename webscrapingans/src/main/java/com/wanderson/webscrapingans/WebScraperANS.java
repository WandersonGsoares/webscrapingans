package com.wanderson.webscrapingans;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WebScraperANS {

    public static void main(String[] args) {
        String url = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";
        String nomeArquivoZip = "anexos.zip";
        String nomeArquivoAnexoI = "Anexo_I.pdf";
        String nomeArquivoAnexoII = "Anexo_II.pdf";

        try {
            System.out.println("Iniciando o Web Scraping...");
            System.out.println("Conectando ao site: " + url);
            Document doc = Jsoup.connect(url).get();
            System.out.println("Conexão estabelecida com sucesso!");

            // Encontrar os links para os arquivos PDF
            Elements links = doc.select("li > a");

            String urlAnexoI = null;
            String urlAnexoII = null;

            for (Element link : links) {
                String text = link.text();
                String href = link.attr("href");

                if (text.equals("Anexo I.")) {
                    urlAnexoI = href;
                    System.out.println("Link para o Anexo I encontrado: " + urlAnexoI);
                } else if (text.equals("Anexo II.")) {
                    urlAnexoII = href;
                    System.out.println("Link para o Anexo II encontrado: " + urlAnexoII);
                }
            }

            if (urlAnexoI != null && urlAnexoII != null) {
                System.out.println("Links para os Anexos I e II encontrados com sucesso!");

                // Excluir arquivos existentes (se existirem)
                File arquivoAnexoI = new File(nomeArquivoAnexoI);
                if (arquivoAnexoI.exists()) {
                    arquivoAnexoI.delete();
                    System.out.println("Arquivo Anexo_I.pdf existente excluído.");
                }

                File arquivoAnexoII = new File(nomeArquivoAnexoII);
                if (arquivoAnexoII.exists()) {
                    arquivoAnexoII.delete();
                    System.out.println("Arquivo Anexo_II.pdf existente excluído.");
                }

                // Baixar o Anexo I
                try {
                    System.out.println("Baixando o Anexo I de: " + urlAnexoI);
                    URL urlDoArquivoI = new URL(urlAnexoI);
                    InputStream inputStreamI = urlDoArquivoI.openStream();
                    Files.copy(inputStreamI, Paths.get(nomeArquivoAnexoI));
                    System.out.println("Anexo I baixado com sucesso como: " + nomeArquivoAnexoI);
                } catch (IOException e) {
                    System.err.println("Erro ao baixar o Anexo I: " + e.getMessage());
                    e.printStackTrace();
                }

                // Baixar o Anexo II
                try {
                    System.out.println("Baixando o Anexo II de: " + urlAnexoII);
                    URL urlDoArquivoII = new URL(urlAnexoII);
                    InputStream inputStreamII = urlDoArquivoII.openStream();
                    Files.copy(inputStreamII, Paths.get(nomeArquivoAnexoII));
                    System.out.println("Anexo II baixado com sucesso como: " + nomeArquivoAnexoII);
                } catch (IOException e) {
                    System.err.println("Erro ao baixar o Anexo II: " + e.getMessage());
                    e.printStackTrace();
                }

                // Compactar os arquivos em ZIP
                try (FileOutputStream fos = new FileOutputStream(nomeArquivoZip);
                     ZipOutputStream zos = new ZipOutputStream(fos)) {

                    // Adicionar Anexo I ao ZIP
                    File anexoI = new File(nomeArquivoAnexoI);
                    if (anexoI.exists()) {
                        ZipEntry zipEntryAnexoI = new ZipEntry(anexoI.getName());
                        zos.putNextEntry(zipEntryAnexoI);
                        try (FileInputStream fis = new FileInputStream(anexoI)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }
                        zos.closeEntry();
                        System.out.println("Anexo I adicionado ao ZIP.");
                    } else {
                        System.out.println("Arquivo " + nomeArquivoAnexoI + " não encontrado para compactação.");
                    }

                    // Adicionar Anexo II ao ZIP
                    File anexoII = new File(nomeArquivoAnexoII);
                    if (anexoII.exists()) {
                        ZipEntry zipEntryAnexoII = new ZipEntry(anexoII.getName());
                        zos.putNextEntry(zipEntryAnexoII);
                        try (FileInputStream fis = new FileInputStream(anexoII)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }
                        zos.closeEntry();
                        System.out.println("Anexo II adicionado ao ZIP.");
                    } else {
                        System.out.println("Arquivo " + nomeArquivoAnexoII + " não encontrado para compactação.");
                    }

                    System.out.println("Arquivos compactados com sucesso em: " + nomeArquivoZip);

                } catch (IOException e) {
                    System.err.println("Erro ao compactar os arquivos: " + e.getMessage());
                    e.printStackTrace();
                }

                // Extrair texto do Anexo I
                extrairTextoDoAnexoI(nomeArquivoAnexoI);

            } else {
                System.out.println("Não foi possível compactar os arquivos pois os links não foram encontrados.");
            }

        } catch (IOException e) {
            System.err.println("Ocorreu um erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void extrairTextoDoAnexoI(String caminhoArquivo) {
        System.out.println("\nIniciando a extração de texto do Anexo I...");
        File fileAnexoI = new File(caminhoArquivo);

        if (fileAnexoI.exists()) {
            try (PDDocument document = PDDocument.load(fileAnexoI)) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String text = pdfStripper.getText(document);
                System.out.println("Texto extraído do Anexo I:\n" + text);

                // Próximo passo: identificar e extrair a tabela
                System.out.println("\nPróximo passo: identificar e extrair a tabela 'Rol de Procedimentos e Eventos em Saúde'.");

            } catch (IOException e) {
                System.err.println("Erro ao ler o arquivo PDF do Anexo I: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Arquivo " + caminhoArquivo + " não encontrado. Certifique-se de que o Teste de Web Scraping foi executado com sucesso.");
        }
    }
}