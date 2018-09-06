/**
 * 
 */
package com.accenture.starter.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.accenture.starter.model.Input;
import com.accenture.starter.util.AwsClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Service("detectTextService")
public class DetectTextService {

	@Autowired
	private AwsClientBuilder awsClientBuilder;
	
	public Object detectText(Input input) {

		DetectTextResult result = null;
		
		System.out.println(input);
		DetectTextRequest request = new DetectTextRequest()
				.withImage(new Image().withS3Object(new S3Object().
						withName(input.getKeyName()).
						withBucket(input.getBucketName())));

		try {
			result = awsClientBuilder.getRekognitionClient().detectText(request);
			List<TextDetection> textDetections = result.getTextDetections();

			System.out.println("Detected lines and words for " + input.getKeyName());
			for (TextDetection text : textDetections) {
				System.out.println("Detected: " + text.getDetectedText());
				System.out.println("Confidence: "
						+ text.getConfidence().toString());
				System.out.println("Id : " + text.getId());
				System.out.println("Parent Id: " + text.getParentId());
				System.out.println("Type: " + text.getType());
				System.out.println();
			}
		} catch (AmazonRekognitionException e) {
			e.printStackTrace();
		}
		System.out.println(result);
		return result;

	}

}
