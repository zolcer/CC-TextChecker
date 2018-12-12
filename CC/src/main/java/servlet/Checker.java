package main.java.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.text.similarity.FuzzyScore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * A runnable instance used in the TextComparer class. It implements the
 * checking logic
 *
 */
public class Checker implements Runnable {

	private static final String BUCKET_NAME = "elasticbeanstalk-us-east-2-092644405248";

	private volatile MultiValuedMap<Integer, String> threadResult = new ArrayListValuedHashMap<>();
	private List<String> fileNames;
	private AmazonS3 client;
	private String textToCheck;

	// ALgorithm used to compare two strings
	private FuzzyScore algorithm = new FuzzyScore(Locale.ENGLISH);

	public Checker(List<String> fileNames, String textToCheck, AmazonS3 client) {
		this.fileNames = fileNames;
		this.textToCheck = textToCheck;
		this.client = client;
	}

	@Override
	public void run() {
		for (String fileName : fileNames) {
			S3Object s3Object = client.getObject(BUCKET_NAME, fileName);
			S3ObjectInputStream inputStream = s3Object.getObjectContent();
			String fileContent = "";
			try {
				fileContent = com.amazonaws.util.IOUtils.toString(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			threadResult.put(getSimilarityScore(fileContent, textToCheck), fileName);
		}
	}

	public MultiValuedMap<Integer, String> getThreadResult() {
		return threadResult;
	}

	private int getSimilarityScore(String fileContent, String textToCheck) {
		return new Integer(algorithm.fuzzyScore(fileContent, textToCheck));
	}
}
