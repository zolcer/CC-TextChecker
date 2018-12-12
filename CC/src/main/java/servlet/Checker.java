package main.java.servlet;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.text.similarity.JaccardSimilarity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Checker implements Runnable {

    private static final String BUCKET_NAME = "elasticbeanstalk-us-east-2-092644405248";

    private volatile Map<Double, String> threadResult = new HashMap<>();
    private List<String> fileNames;
    private AmazonS3 client;
    private String textToCheck;
    // ALgorithm used to compare two strings
    private JaccardSimilarity algorithm = new JaccardSimilarity();

    public Checker(List<String> files, String text, AmazonS3 c) {
        fileNames = files;
        textToCheck = text;
        client = c;
    }

    @Override
    public void run() {
        for (String fileName : fileNames) {
            S3Object object = client.getObject(BUCKET_NAME, fileName);
            S3ObjectInputStream inputStream = object.getObjectContent();
            String textContent = "";
            try {
                textContent = com.amazonaws.util.IOUtils.toString(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            threadResult.put(getSimiliariryScore(textToCheck, textContent), fileName);
        }
    }

    public Map<Double, String> getThreadResult() {
        return threadResult;
    }

    /**
     * Text Compare algorithm
     *
     * @param text1 first text to compare
     * @param text2 second text to compare
     * @return score from 0 - 1, where 0 = no similarity, 1 = same
     */
    private double getSimiliariryScore(String text1, String text2) {
        return algorithm.apply(text1, text2);
    }
}
