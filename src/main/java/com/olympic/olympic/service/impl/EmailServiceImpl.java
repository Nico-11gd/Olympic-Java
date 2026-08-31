package com.olympic.olympic.service.impl;

import com.olympic.olympic.exception.CorreoNoEnviadoException;
import com.olympic.olympic.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.recuperacion-password.remitente}")
    private String remitente;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarCodigoRecuperacion(String destinatario, String nombreUsuario, String codigo,
            int expiracionMinutos) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
            helper.setTo(destinatario);
            helper.setFrom(remitente);
            helper.setSubject("Recupera tu contraseña — OLYMPIC");
            helper.setText(construirHtml(nombreUsuario, codigo, expiracionMinutos), true);
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new CorreoNoEnviadoException("No se pudo enviar el correo de recuperación.", e);
        }
    }

    private String construirHtml(String nombreUsuario, String codigo, int expiracionMinutos) {
        return """
                <div style="font-family:Arial,sans-serif; background-color:#1A1A2E; padding:32px;">
                  <div style="max-width:480px; margin:0 auto; background-color:#ffffff; border-radius:8px; overflow:hidden;">
                    <div style="background-color:#1A1A2E; padding:24px; text-align:center;">
                      <h1 style="color:#D4AF37; margin:0; font-size:24px; letter-spacing:2px;">OLYMPIC</h1>
                    </div>
                    <div style="padding:32px; text-align:center;">
                      <p style="color:#1A1A2E; font-size:16px;">Hola %s,</p>
                      <p style="color:#333333; font-size:15px;">Usa este código para restablecer tu contraseña. Expira en %d minutos.</p>
                      <div style="background-color:#f5f5f5; border:2px dashed #D4AF37; border-radius:8px; padding:16px; margin:24px 0;">
                        <span style="font-size:32px; font-weight:bold; letter-spacing:8px; color:#1A1A2E;">%s</span>
                      </div>
                      <p style="color:#888888; font-size:13px;">Si no solicitaste este código, ignora este correo.</p>
                    </div>
                  </div>
                </div>
                """
                .formatted(nombreUsuario, expiracionMinutos, codigo);
    }
}