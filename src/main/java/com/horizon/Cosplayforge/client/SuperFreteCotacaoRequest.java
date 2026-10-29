package com.horizon.Cosplayforge.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record SuperFreteCotacaoRequest(
        Local from,
        Local to,
        String services,
        Opcoes options,
        List<Item> products
) {
    public record Local(@JsonProperty("postal_code") String postalCode){}

    public record Opcoes(
            @JsonProperty("own_hand") boolean ownHand,
            boolean receipt,
            @JsonProperty("insurance_value")BigDecimal insuranceValue,
            @JsonProperty("use_insurance_value") boolean useInsuranceValue){
        public static Opcoes padrao(){
            return new Opcoes(false,false,BigDecimal.ZERO,false);
        }
    }
    public record Item(int quantity,
                       int height,
                       int length,
                       int width,
                       BigDecimal weight){}
}
