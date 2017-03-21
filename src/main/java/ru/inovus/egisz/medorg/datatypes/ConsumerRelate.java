package ru.inovus.egisz.medorg.datatypes;

public class ConsumerRelate {

    private final String informationSystemName;

    private final String pemCertificate;

    private final String pemPrivateKey;

    public ConsumerRelate(String informationSystemName, String pemCertificate, String pemPrivateKey){
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
}
