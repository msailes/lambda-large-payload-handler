# lambda-large-payload-handler

A Java Lambda request handler for use with SQS Events which have been created with the SQS Extended Client Library.

The [amazon-sqs-java-extended-client-lib](https://github.com/awslabs/amazon-sqs-java-extended-client-lib) allows you to send messages of up to 2GB. It does this by offloading the message into S3 storage. A reference to the S3 bucket and key is then sent with the SQS message instead of the full payload. 

You can then use the same client lib to retreive messages from SQS. When you do this, the client library will automatically download the S3 object and inject it into the SQS message. For a consumer of the message the experience is seamless.

When consuming SQS messages which have been created by the extended client library in AWS Lambda, the Lambda service doesn't do the automatic downloading from S3.

In this example I show you how you can take the S3 reference, download the object and delete the object once processing has been completed.
