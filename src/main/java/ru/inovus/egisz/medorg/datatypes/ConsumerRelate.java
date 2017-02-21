package ru.inovus.egisz.medorg.datatypes;

public class ConsumerRelate {

    private final String informationSystemName;

    private final String pemCertificate;

    private final String pemPrivateKey;

    private final String authorizedUserName;

    public ConsumerRelate(String authorizedUserName, String informationSystemName, String pemCertificate, String pemPrivateKey){
        this.authorizedUserName=authorizedUserName;
        this.informationSystemName=informationSystemName;
        this.pemCertificate=pemCertificate;
        this.pemPrivateKey=pemPrivateKey;
    }

    public String getInformationSystemName() {
        return informationSystemName;
    }

    public String getPemCertificate() {
        return pemCertificate;
    }

    public String getPemPrivateKey() {
        return pemPrivateKey;
    }

    public String getAuthorizedUserName() {
        return authorizedUserName;
    }
}
