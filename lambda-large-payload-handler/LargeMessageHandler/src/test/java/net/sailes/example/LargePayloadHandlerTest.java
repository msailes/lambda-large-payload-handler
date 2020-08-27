package net.sailes.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.assertj.core.api.Assertions.assertThat;

class LargePayloadHandlerTest {

    @Mock
    private AmazonS3Client amazonS3Client;

    @Mock
    private Context context;

    private RequestHandler<SQSEvent, String> requestHandler;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        setupContext();
    }

    @Test
    public void testLargeMessage() {
        requestHandler = new LargePayloadHandler(amazonS3Client);

        S3Object mockS3Object = Mockito.mock(S3Object.class);
        when(mockS3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream("newValueFromS3".getBytes()), null));
        when(amazonS3Client.getObject(anyString(), anyString())).thenReturn(mockS3Object);

        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody("[\"software.amazon.payloadoffloading.PayloadS3Pointer\",{\"s3BucketName\":\"bucketName\",\"s3Key\":\"c71eb2ae-37e0-4265-8909-32f4153faddf\"}]");
        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(Arrays.asList(sqsMessage));
        requestHandler.handleRequest(sqsEvent, context);

        verify(amazonS3Client).getObject(anyString(), anyString());
        verify(amazonS3Client).deleteObject(anyString(), anyString());
    }

    private void setupContext() {
        when(context.getFunctionName()).thenReturn("testFunction");
        when(context.getInvokedFunctionArn()).thenReturn("testArn");
        when(context.getFunctionVersion()).thenReturn("1");
        when(context.getMemoryLimitInMB()).thenReturn(10);
    }

}