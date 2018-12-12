package main.java.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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

/**
 * Class contains text checker algorithm as well as S3 bucket communication
 */
public class TextComparer {

	private static final String AWS_SECRET_KEY = "aws.secretKey";
	private static final String AWS_ACCESS = "aws.access";
	private static final String DELIMETER = "/";
	private static final String FOLDER_NAME = "textFiles/";
	private static final String BUCKET_NAME = "elasticbeanstalk-us-east-2-092644405248";
	private static final int NUM_THREADS = 10;

	private final static Logger LOG = Logger.getLogger(TextComparer.class.getName());

	// AWS client used for connecting to S3 storage
	private AmazonS3 client;

	private TransferManager transferManager;

	/**
	 * Initialize an instance of this class (read credentials from properties file
	 * and connect to the S3 storage
	 */
	public TextComparer() {
		LOG.info("Initializing checker...");
		loadProperties();
		// Load the credentials for AWS S3 storage
		AWSCredentials credentials = new BasicAWSCredentials(System.getProperty(AWS_ACCESS),
				System.getProperty(AWS_SECRET_KEY));
		// Build the S3 client
		client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.US_EAST_2).build();
		transferManager = TransferManagerBuilder.standard().withS3Client(client).build();
		LOG.info("TextComparer initialized.");
	}

	/**
	 * Compare the input text with the text files stored in the S3 storage
	 * 
	 * @param textToCheck
	 *            text to compare
	 * @return instance of {@link Pair}, which contains the score of the text
	 *         together with the name of the most similar file
	 * @throws IOException
	 */
	public ImmutablePair<Integer, Collection<String>> checkText(String textToCheck) throws IOException {
		// Get all objects stored in the "textFiles" folder on the S3 storage
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(BUCKET_NAME)
				.withPrefix(FOLDER_NAME).withDelimiter(DELIMETER);
		ObjectListing objects = transferManager.getAmazonS3Client().listObjects(listObjectsRequest);

		List<String> fileNames = new ArrayList<String>();

		for (S3ObjectSummary s : objects.getObjectSummaries()) {
			fileNames.add(s.getKey());
		}

		// Remove the first entry which is the folder
		fileNames.remove(0);

		// The result map
		MultiValuedMap<Integer, String> scoresWithNames = new ArrayListValuedHashMap<>();

		Checker checkers[] = new Checker[NUM_THREADS];
		Thread threads[] = new Thread[NUM_THREADS];

		// Create the threads, each getting a part of the files to check
		for (int i = 0; i < NUM_THREADS; i++) {
			checkers[i] = new Checker(fileNames.subList(0 + i * ((fileNames.size() - 0 + 1) / NUM_THREADS), 0
					+ i * ((fileNames.size() - 0 + 1) / NUM_THREADS) + ((fileNames.size() - 0 + 1) / NUM_THREADS) - 1),
					textToCheck, client);
			LOG.info("Thread with id: " + i + " gets files in rage from "
					+ (0 + i * ((fileNames.size() - 0 + 1) / NUM_THREADS)) + " to "
					+ (0 + i * ((fileNames.size() - 0 + 1) / NUM_THREADS) + ((fileNames.size() - 0 + 1) / NUM_THREADS)
							- 1));
		}

		// Start the threads
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i] = new Thread(checkers[i]);
			threads[i].start();
			LOG.info("Thread with id " + i + " started.");
		}

		for (int i = 0; i < NUM_THREADS; i++) {
			try {
				threads[i].join();
				LOG.info("Join on thread with id " + i);
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "Join error: " + e.getMessage());
			}
			scoresWithNames.putAll(checkers[i].getThreadResult());
		}

		Integer maxScore = Collections.max(scoresWithNames.keySet());
		Collection<String> mostSimiliarFiles = scoresWithNames.get(maxScore);
		return new ImmutablePair<Integer, Collection<String>>(maxScore, mostSimiliarFiles);
	}

	/**
	 * Load the AWS S3 credentials from a properties file into system properties
	 */
	private void loadProperties() {
		InputStream inputStream = getClass().getResourceAsStream("/aws.properties");
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error when loading properties: " + e.getMessage());
		}
		System.setProperty(AWS_ACCESS, properties.getProperty(AWS_ACCESS));
		System.setProperty(AWS_SECRET_KEY, properties.getProperty(AWS_SECRET_KEY));
	}

}
