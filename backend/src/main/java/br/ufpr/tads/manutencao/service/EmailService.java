package br.ufpr.tads.manutencao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Envia a senha temporária gerada para o e-mail do cliente no ato do cadastro.
     * Em ambiente acadêmico/desenvolvimento sem servidor SMTP configurado,
     * registra o e-mail nos logs com destaque formatado.
     *
     * @param destinatario E-mail do cliente
     * @param nome Nome do cliente
     * @param senhaTemporaria Senha aleatória de 4 dígitos gerada
     */
    public void enviarSenhaTemporaria(String destinatario, String nome, String senhaTemporaria) {
        log.info("=================================================================");
        log.info("NOTIFICAÇÃO DE AUTOCADASTRO DE CLIENTE (RF001)");
        log.info("Para: {}", destinatario);
        log.info("Assunto: Bem-vindo! Sua senha de acesso ao sistema");
        log.info("Mensagem:");
        log.info("Olá, {}!", nome);
        log.info("Seu cadastro foi realizado com sucesso.");
        log.info("Seu login de acesso é: {}", destinatario);
        log.info("Sua senha temporária de 4 números é: {}", senhaTemporaria);
        log.info("Recomendamos a alteração de sua senha após o primeiro acesso.");
        log.info("=================================================================");
    }
}
