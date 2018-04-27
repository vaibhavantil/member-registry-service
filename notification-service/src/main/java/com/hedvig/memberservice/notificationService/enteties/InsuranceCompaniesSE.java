package com.hedvig.memberservice.notificationService.enteties;


public enum InsuranceCompaniesSE {
    LANSFORSAKRINGAR,
    IF,
    FOLKSAM,
    TRYGG_HANSA,
    OTHER,
    MODERNA,
    ICA,
    GJENSIDIGE,
    VARDIA;

    public static InsuranceCompaniesSE create(final String name) {
        switch (name) {
            case "if":
                return IF;
            case "Folksam":
                return FOLKSAM;
            case "Trygg-Hansa":
                return TRYGG_HANSA;
            case "Länsförsäkringar":
                return LANSFORSAKRINGAR;
            default:
                return OTHER;
        }
    }
}
