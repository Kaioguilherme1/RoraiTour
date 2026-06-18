package com.example.roraitour.utils;

/**
 * Classe centralizada para configurações da aplicação.
 */
public final class AppConfig {

    private AppConfig() { }

    /**
     * COLOQUE SUA CHAVE DA API ABAIXO:
     * Consiga em: https://opentripmap.io/product
     */
    public static final String OPENTRIPMAP_API_KEY = "5ae2e3f221c38a28845f05b6121c4ac09e340eb99ba87e2484b686f4";

    /**
     * URL Base para as requisições do OpenTripMap.
     * Usamos 'en' por ser o endpoint mais estável, as descrições podem ser traduzidas.
     */
    public static final String OPENTRIPMAP_BASE_URL = "https://api.opentripmap.com/0.1/en/places/";

    /**
     * URL Base para as requisições da Wikipédia.
     */
    public static final String WIKIPEDIA_BASE_URL = "https://pt.wikipedia.org/api/rest_v1/";

}
