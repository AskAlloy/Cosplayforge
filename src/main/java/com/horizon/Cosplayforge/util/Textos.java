package com.horizon.Cosplayforge.util;

import org.springframework.util.StringUtils;

public final class Textos {
    private Textos() {
    }

    public static String vazioParaNull(String texto) {
        if (StringUtils.hasText(texto)) {
            return texto.trim();
        } else {
            return null;
        }
    }
}
