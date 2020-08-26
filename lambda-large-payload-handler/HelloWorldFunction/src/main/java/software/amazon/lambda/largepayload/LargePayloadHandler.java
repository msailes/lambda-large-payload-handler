package software.amazon.lambda.largepayload;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.UncheckedIOException;

/**
 * This handler should cope with the case where the SQSExtendedClient is storing all
 * messages into S3.
 *
 * It doesn't cope with the case where only messages over the maximum are stored in S3.
 *
 */
public class LargePayloadHandler implements RequestHandler<MessageS3Pointer, String> {

    private S3Client s3Client = S3Client.create();

    @Override
    public String handleRequest(MessageS3Pointer messageS3Pointer, Context context) {
        LambdaLogger logger = context.getLogger();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(messageS3Pointer.getS3BucketName())
                .key(messageS3Pointer.getS3Key())
                .build();
        ResponseBytes<GetObjectResponse> object = null;
        try {
            object = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
        } catch (SdkException e) {
            String errorMessage = "Failed to get the S3 object which contains the message payload. Message was not received.";
            logger.log(errorMessage + " "  + e.getMessage());
            throw SdkException.create(errorMessage, e);
        }

        String embeddedText;
        try {
            embeddedText = object.asUtf8String();
        } catch (UncheckedIOException e) {
            String errorMessage = "Failure when handling the message which was read from S3 object. Message was not received.";
            logger.log(errorMessage + " " + e.getMessage());
            throw SdkClientException.create(errorMessage, e);
        }

        logger.log(embeddedText);


        // if success, delete the object from s3

        return "ok";
    }
}
