package br.ufpr.tads.manutencao.util;

public final class CpfUtils {

    private CpfUtils() {}

    /**
     * Remove pontuações e caracteres não numéricos do CPF.
     */
    public static String limpar(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    /**
     * Valida o formato e os dois dígitos verificadores do CPF.
     */
    public static boolean isValido(String cpf) {
        String apenasDigitos = limpar(cpf);
        if (apenasDigitos == null || apenasDigitos.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (ex.: 00000000000, 11111111111)
        if (apenasDigitos.chars().distinct().count() == 1) {
            return false;
        }

        try {
            // Primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (apenasDigitos.charAt(i) - '0') * (10 - i);
            }
            int resto = soma % 11;
            int digito1 = resto < 2 ? 0 : 11 - resto;
            if (digito1 != (apenasDigitos.charAt(9) - '0')) {
                return false;
            }

            // Segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (apenasDigitos.charAt(i) - '0') * (11 - i);
            }
            resto = soma % 11;
            int digito2 = resto < 2 ? 0 : 11 - resto;
            return digito2 == (apenasDigitos.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}
