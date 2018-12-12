package main.java.servlet;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class contains text checker algorithm as well as S3 bucket communication
 */
public class TextSimiliarityChecker {

    private static final String DELIMETER = "/";
    private static final String FOLDER_NAME = "textFiles/";
    private static final String BUCKET_NAME = "elasticbeanstalk-us-east-2-092644405248";
    private static final int NUM_THREADS = 10;

    private final static Logger log = Logger.getLogger(TextSimiliarityChecker.class.getName());

    //private static final ThreadGroup tg = new ThreadGroup("checkers");

    /*// ALgorithm used to compare two strings
    private JaccardSimilarity algorithm;*/

    // AWS client used for connecting to S3 storage
    private AmazonS3 client;

    private TransferManager transferManager;

    /**
     * Construct instance of Text Checker, loads asw keys as properties, initialize algorithm and build a connection with S3 bucked
     */
    public TextSimiliarityChecker() {
        log.info("Starting Checker, loading properties, sign in to aws S3...");
        loadProperties();
        //algorithm = new JaccardSimilarity();
        // Access credentials
        AWSCredentials credentials = new BasicAWSCredentials(System.getProperty("aws.access"), System.getProperty("aws.secretKey"));
        client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_2).build();
        transferManager = TransferManagerBuilder.standard().withS3Client(client).build();
        log.info("All set.");
    }

    /**
     * Checks received text against S3 stored files for similarity
     *
     * @param textToCheck input text
     * @return Pair of max score (most similar) and file name for this score
     * @throws IOException when error reading file occurs
     */
    public Pair<Double, String> checkText(String textToCheck) throws IOException {
        log.info("Checking text: " + textToCheck);
        // Get all objects stored in the "textFiles" folder
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(BUCKET_NAME)
                .withPrefix(FOLDER_NAME).withDelimiter(DELIMETER);
        ObjectListing objects = transferManager.getAmazonS3Client().listObjects(listObjectsRequest);

        List<String> fileNames = new ArrayList<String>();

        for (S3ObjectSummary s : objects.getObjectSummaries()) {
            fileNames.add(s.getKey());
        }

        // Remove the first entry which is the folder
        fileNames.remove(0);

        Map<Double, String> scoresWithNames = new HashMap<Double, String>();

        Checker r[] = new Checker[NUM_THREADS];
        Thread t[] = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            //range comp: start(0) + i * (size - start + 1)/num threads; start + i * ((size - start + 1) / num threads) + ((size - start + 1)/num threads) - 1
            r[i] = new Checker(fileNames.subList(i * ((fileNames.size() + 1) / NUM_THREADS), i * ((fileNames.size() + 1) / NUM_THREADS) + ((fileNames.size() + 1) / NUM_THREADS) - 1), textToCheck, client);
            log.info("Making Checker runnable " + i + " with sublist range: " + (i * ((fileNames.size() + 1) / NUM_THREADS)) + " - " + (i * ((fileNames.size() + 1) / NUM_THREADS) + ((fileNames.size() + 1) / NUM_THREADS) - 1));
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            t[i] = new Thread(r[i]);
            t[i].start();
            log.info("Thread " + i + " started");
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                t[i].join();
                log.info("Join " + i);
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "Join Error: " + e.getMessage());
            }
            scoresWithNames.putAll(r[i].getThreadResult());
        }


        // TODO What happens when 2 files have same score
        Double maxScore = Collections.max(scoresWithNames.keySet());
        String mostSimiliarFile = scoresWithNames.get(maxScore);
        log.info("Max score: " + maxScore + " file name: " + mostSimiliarFile);
        return new ImmutablePair<Double, String>(maxScore, mostSimiliarFile);

    }

    /**
     * Text Compare algorithm
     *
     * @param text1 first text to compare
     * @param text2 second text to compare
     * @return score from 0 - 1, where 0 = no similarity, 1 = same
     *//*
    private double getSimiliariryScore(String text1, String text2) {
        return algorithm.apply(text1, text2);
    }*/

    /**
     * load aws keys as properties from property file, not stored on public git due to security
     */
    private void loadProperties() {
        InputStream in = getClass().getResourceAsStream("/aws.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error when loading properties: " + e.getMessage());
        }
        System.setProperty("aws.access", properties.getProperty("aws.access"));
        System.setProperty("aws.secretKey", properties.getProperty("aws.secretKey"));
    }

}
