package net.sailes.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.PowertoolsLogging;
import software.amazon.lambda.powertools.sqs.LargeMessageHandler;

/**
 * This handler should cope with the case where the SQSExtendedClient is being used
 * to store large payloads in S3.
 */
public class LargePayloadHandler implements RequestHandler<SQSEvent, String> {

    Logger logger = LogManager.getLogger(LargePayloadHandler.class);

    @Override
    @LargeMessageHandler
    @PowertoolsLogging(logEvent = true)
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        sqsEvent.getRecords().forEach(record -> logger.info(record.getBody()));

        return "ok";
    }
}
