package net.sailes.example;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class MessageSender {

    public static void main( String[] args ) {
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();
        ExtendedClientConfiguration s3StorageDisabled = new ExtendedClientConfiguration()
                .withPayloadSupportDisabled();
        ExtendedClientConfiguration s3StorageAlwaysEnabled = new ExtendedClientConfiguration()
                .withPayloadSupportEnabled(amazonS3,"ms-extended-sqs-client")
                .withAlwaysThroughS3(true);
        AmazonSQS sqsExtendedWithoutS3Storage =  new AmazonSQSExtendedClient(AmazonSQSClientBuilder.defaultClient(), s3StorageDisabled);
        AmazonSQS sqsExtendedWith3Storage =  new AmazonSQSExtendedClient(AmazonSQSClientBuilder.defaultClient(), s3StorageAlwaysEnabled);

        GetQueueUrlResult queueUrl = sqsExtendedWithoutS3Storage.getQueueUrl("large-payload-handler-LargeMessagesQueue-1NHWQ9486PN1T");

        SendMessageRequest smallMessage = new SendMessageRequest(queueUrl.getQueueUrl(), "a small message");
        sqsExtendedWithoutS3Storage.sendMessage(smallMessage);
        SendMessageRequest bigMessage = new SendMessageRequest(queueUrl.getQueueUrl(), "a really really big message");
        sqsExtendedWith3Storage.sendMessage(bigMessage);
        System.out.println("fin.");
    }
}
