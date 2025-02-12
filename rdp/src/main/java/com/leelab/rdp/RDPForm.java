package com.leelab.rdp;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class RDPForm {
    private List<MultipartFile> files;
    private double conf = 0.8;
    private String format = "allrank";
    private String gene = "16srrna";
//    private String outputFile;
//    private String hierOutfile;
//    private String bootstrapOutfile;
//    private String trainPropfile;
//    private String biomFile;
//    private String metadata;
//    private String shortseqOutfile;
    private int minWords = 5;

    // Getters and setters for all properties

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public double getConf() {
        return conf;
    }

    public void setConf(double conf) {
        this.conf = conf;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

//    public String getOutputFile() {
//        return outputFile;
//    }

//    public void setOutputFile(String outputFile) {
//        this.outputFile = outputFile;
//    }

//    public String getHierOutfile() {
//        return hierOutfile;
//    }

//    public void setHierOutfile(String hierOutfile) {
//        this.hierOutfile = hierOutfile;
//    }

//    public String getBootstrapOutfile() {
//        return bootstrapOutfile;
//    }

//    public void setBootstrapOutfile(String bootstrapOutfile) {
//        this.bootstrapOutfile = bootstrapOutfile;
//    }

//    public String getTrainPropfile() {
//        return trainPropfile;
//    }

//    public void setTrainPropfile(String trainPropfile) {
//        this.trainPropfile = trainPropfile;
//    }

//    public String getBiomFile() {
//        return biomFile;
//    }

//    public void setBiomFile(String biomFile) {
//        this.biomFile = biomFile;
//    }

//    public String getMetadata() {
//        return metadata;
//    }

//    public void setMetadata(String metadata) {
//        this.metadata = metadata;
//    }

//    public String getShortseqOutfile() {
//        return shortseqOutfile;
//    }

//    public void setShortseqOutfile(String shortseqOutfile) {
//        this.shortseqOutfile = shortseqOutfile;
//    }

    public int getMinWords() {
        return minWords;
    }

    public void setMinWords(int minWords) {
        this.minWords = minWords;
    }

}
