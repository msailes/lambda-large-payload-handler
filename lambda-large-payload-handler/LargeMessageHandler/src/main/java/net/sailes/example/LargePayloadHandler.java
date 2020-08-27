package net.sailes.example;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import software.amazon.payloadoffloading.PayloadS3Pointer;

import java.io.IOException;
import java.util.List;

/**
 * This handler should cope with the case where the SQSExtendedClient is being used
 * to store large payloads in S3.
 */
public class LargePayloadHandler implements RequestHandler<SQSEvent, String> {

    private static final Log LOG = LogFactory.getLog(LargePayloadHandler.class);
    private AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();

    public LargePayloadHandler() {
    }

    public LargePayloadHandler(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        for (SQSEvent.SQSMessage record : records) {
            processSingleRecord(record);
        }

        return "ok";
    }

    private void processSingleRecord(SQSEvent.SQSMessage record) {
        String body = record.getBody();
        if (isBodyLargeMessagePointer(body)) {
            PayloadS3Pointer s3Pointer = PayloadS3Pointer.fromJson(body);
            S3Object object = amazonS3.getObject(s3Pointer.getS3BucketName(), s3Pointer.getS3Key());
            String s3Body = readStringFromS3Object(object);
            doImportantWork(s3Body);
            deleteMessageFromS3(s3Pointer);
        } else {
            doImportantWork(body);
        }
    }

    private void deleteMessageFromS3(PayloadS3Pointer s3Pointer) {
        try {
            amazonS3.deleteObject(s3Pointer.getS3BucketName(), s3Pointer.getS3Key());
            LOG.info("Message deleted from S3: " + s3Pointer.toJson());
        } catch (AmazonServiceException e) {
            LOG.error("A service exception", e);
        } catch (SdkClientException e) {
            LOG.error("Some sort of client exception", e);
        }
    }

    private void doImportantWork(String body) {
        LOG.info("Doing important work on: " + body);
    }

    private String readStringFromS3Object(S3Object object) {
        S3ObjectInputStream is = object.getObjectContent();
        String s3Body = null;
        try {
            s3Body = IOUtils.toString(is);
        } catch (IOException e) {
            LOG.error("eek", e);
        } finally {
            IOUtils.closeQuietly(is, LOG);
        }
        return s3Body;
    }

    private boolean isBodyLargeMessagePointer(String record) {
        return record.startsWith("[\"software.amazon.payloadoffloading.PayloadS3Pointer\"");
    }
}
