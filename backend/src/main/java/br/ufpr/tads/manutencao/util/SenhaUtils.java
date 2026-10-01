package br.ufpr.tads.manutencao.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class SenhaUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private SenhaUtils() {}

    /**
     * Gera automaticamente uma senha aleatória de 4 dígitos numéricos (0000 a 9999).
     */
    public static String gerarSenhaAleatoria() {
        int numero = RANDOM.nextInt(10000);
        return String.format("%04d", numero);
    }

    /**
     * Gera um Salt aleatório criptográfico de 16 bytes formatado em hexadecimal.
     */
    public static String gerarSalt() {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        return HexFormat.of().formatHex(saltBytes);
    }

    /**
     * Gera o Hash SHA-256 da senha combinada com o Salt.
     */
    public static String gerarHashSha256ComSalt(String senha, String salt) {
        if (senha == null || salt == null) {
            throw new IllegalArgumentException("Senha e salt não podem ser nulos");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combinado = salt + senha;
            byte[] hashBytes = digest.digest(combinado.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 não disponível no ambiente", e);
        }
    }

    /**
     * Verifica se a senha fornecida corresponde ao hash e salt armazenados.
     */
    public static boolean verificarSenha(String senha, String salt, String hashArmazenado) {
        if (senha == null || salt == null || hashArmazenado == null) {
            return false;
        }
        String hashCalculado = gerarHashSha256ComSalt(senha, salt);
        return MessageDigest.isEqual(
                hashCalculado.getBytes(StandardCharsets.UTF_8),
                hashArmazenado.getBytes(StandardCharsets.UTF_8)
        );
    }
}
