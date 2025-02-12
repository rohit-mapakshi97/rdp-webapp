package com.leelab.rdp.utils;

import com.leelab.rdp.RDPForm;
import edu.msu.cme.rdp.classifier.Classifier;
import edu.msu.cme.rdp.classifier.cli.CmdOptions;
import edu.msu.cme.rdp.classifier.io.ClassificationResultFormatter;
import edu.msu.cme.rdp.classifier.utils.ClassifierFactory;
import edu.msu.cme.rdp.classifier.utils.ClassifierSequence;
import edu.msu.cme.rdp.multicompare.MCSample;
import edu.msu.cme.rdp.multicompare.MCSamplePrintUtil;
import edu.msu.cme.rdp.multicompare.MultiClassifier;
import edu.msu.cme.rdp.multicompare.MultiClassifierResult;
import edu.msu.cme.rdp.multicompare.visitors.DefaultPrintVisitor;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ClassifierMain {
    public static byte[] classify(RDPForm rdpForm) throws Exception {
        ByteArrayOutputStream hier_info = new ByteArrayOutputStream();
        PrintStream hier_out = new PrintStream(hier_info);

        StringWriter classification_output = new StringWriter();
        PrintWriter assign_out = new PrintWriter(new BufferedWriter(classification_output));

        ByteArrayOutputStream bootstrap_info = new ByteArrayOutputStream();
        PrintStream bootstrap_out = new PrintStream(bootstrap_info);

        String propFile = null;
        File biomFile = null;
        File metadataFile = null;

        StringWriter shortseq_writer = new StringWriter();
        PrintWriter shortseq_out = new PrintWriter(new BufferedWriter(shortseq_writer));

        List<MCSample> samples = new ArrayList();
        ClassificationResultFormatter.FORMAT format = ClassificationResultFormatter.FORMAT.allRank;
        float conf = CmdOptions.DEFAULT_CONF;
        String gene = null;
        int min_bootstrap_words = Classifier.MIN_BOOTSTRSP_WORDS;

        try {
            if (!Objects.equals(rdpForm.getFormat(), "")) {
                String f = rdpForm.getFormat();
                if (f.equalsIgnoreCase("allrank")) {
                    format = ClassificationResultFormatter.FORMAT.allRank;
                } else if (f.equalsIgnoreCase("fixrank")) {
                    format = ClassificationResultFormatter.FORMAT.fixRank;
                } else if (f.equalsIgnoreCase("filterbyconf")) {
                    format = ClassificationResultFormatter.FORMAT.filterbyconf;
                } else if (f.equalsIgnoreCase("db")) {
                    format = ClassificationResultFormatter.FORMAT.dbformat;
                } else if (f.equalsIgnoreCase("biom")) {
                    format = ClassificationResultFormatter.FORMAT.biom;
                }else {
                    throw new IllegalArgumentException("Not an valid output format, only allrank, fixrank, biom, filterbyconf and db allowed");
                }
            }
            if (!Objects.equals(rdpForm.getGene(), "")) {

                gene = rdpForm.getGene().toLowerCase();

                if (!gene.equals(ClassifierFactory.RRNA_16S_GENE) && !gene.equals(ClassifierFactory.FUNGALLSU_GENE)
                        && !gene.equals(ClassifierFactory.FUNGALITS_warcup_GENE) && !gene.equals(ClassifierFactory.FUNGALITS_unite_GENE) ) {
                    throw new IllegalArgumentException(gene + " not found, choose from" + ClassifierFactory.RRNA_16S_GENE + ", "
                            + ClassifierFactory.FUNGALLSU_GENE + ", " + ClassifierFactory.FUNGALITS_warcup_GENE  + ", " + ClassifierFactory.FUNGALITS_unite_GENE);
                }
            }

                min_bootstrap_words = rdpForm.getMinWords();
                if (min_bootstrap_words < Classifier.MIN_BOOTSTRSP_WORDS) {
                    throw new IllegalArgumentException(CmdOptions.MIN_BOOTSTRAP_WORDS_LONG_OPT + " must be at least " + Classifier.MIN_BOOTSTRSP_WORDS);
                }

                try {
                    conf = Double.valueOf(rdpForm.getConf()).floatValue();
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Confidence must be a decimal number");
                }

                if (conf < 0 || conf > 1) {
                    throw new IllegalArgumentException("Confidence must be in the range [0,1]");
                }


            List<MultipartFile> inputFiles = rdpForm.getFiles();
            if(inputFiles != null && !inputFiles.isEmpty())
            {
                for(int i=0; i < inputFiles.size(); i++)
                {
                    MultipartFile multipartFile = inputFiles.get(i);
                    List<ClassifierSequence> classifierSequences = getClassiferSequences(multipartFile);

                    File idmappingFile = null;
                    MCSample nextSample = new MCSample(classifierSequences, multipartFile.getName(), idmappingFile);
                    samples.add(nextSample);
                }
            }

            if ( propFile == null && gene == null){
                gene = CmdOptions.DEFAULT_GENE;
            }
            if (samples.size() < 1) {
                throw new IllegalArgumentException("Require at least one sample files");
            }
        }catch (Exception e) {
            System.out.println("Command Error: " + e.getMessage());
            return null;
        }

        MultiClassifier multiClassifier = new MultiClassifier(propFile, gene, biomFile, metadataFile);
        MultiClassifierResult result = multiClassifier.multiCompare(samples, conf, assign_out, format, min_bootstrap_words);
        assign_out.close();


        if(hier_out != null){
            DefaultPrintVisitor printVisitor = new DefaultPrintVisitor(hier_out, samples);
            result.getRoot().topDownVisit(printVisitor);
            hier_out.close();
        }

        if (bootstrap_out != null){
            for (MCSample sample : samples) {
                MCSamplePrintUtil.printBootstrapCountTable(bootstrap_out, sample);
            }
            bootstrap_out.close();
        }

        if ( shortseq_out != null){
            for (String id: result.getBadSequences()){
                shortseq_out.write(id +"\n");
            }
            shortseq_out.close();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ZipOutputStream zos = new ZipOutputStream(baos))
        {
            // Classification Report
            ZipEntry classification_entry = new ZipEntry("classification_result.txt");
            zos.putNextEntry(classification_entry);
            zos.write(classification_output.toString().getBytes());
            zos.closeEntry();

            // Heirarchial Info
            ZipEntry hier_entry = new ZipEntry("hierarchy.txt");
            zos.putNextEntry(hier_entry);
            zos.write(hier_info.toString().getBytes());
            zos.closeEntry();

            //Bootstrap Info
            ZipEntry bootstrap_entry = new ZipEntry("bootstrap.txt");
            zos.putNextEntry(bootstrap_entry);
            zos.write(bootstrap_info.toString().getBytes());
            zos.closeEntry();

            //Shortseq Info
            ZipEntry shortseq_entry = new ZipEntry("shortseq.txt");
            zos.putNextEntry(shortseq_entry);
            zos.write(shortseq_writer.toString().getBytes());
            zos.closeEntry();
        }
        byte[] zipBytes = baos.toByteArray();
        return zipBytes;
    }

    private static List<ClassifierSequence> getClassiferSequences(MultipartFile multipartFile) throws IOException {
        List<ClassifierSequence> classifierSequences = new ArrayList<>();
        for(Sequence sequence : SequenceReader.readFully(multipartFile.getInputStream()))
        {
            classifierSequences.add(new ClassifierSequence(sequence));
        }
        return classifierSequences;
    }
}
