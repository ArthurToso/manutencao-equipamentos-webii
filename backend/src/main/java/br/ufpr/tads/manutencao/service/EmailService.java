package br.ufpr.tads.manutencao.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviarSenhaTemporaria(String destinatario, String nome, String senhaTemporaria) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Sua senha de acesso ao sistema de manutenção");
        mensagem.setText("""
                Olá, %s!

                Seu cadastro foi realizado com sucesso.
                Login: %s
                Senha: %s
                """.formatted(nome, destinatario, senhaTemporaria));

        mailSender.send(mensagem);
    }
}