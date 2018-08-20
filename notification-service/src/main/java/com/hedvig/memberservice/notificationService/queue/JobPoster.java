package com.hedvig.memberservice.notificationService.queue;

import static org.springframework.cloud.aws.messaging.core.SqsMessageHeaders.SQS_DELAY_HEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.memberservice.notificationService.queue.jobs.SendActivationAtFutureDateEmail;
import com.hedvig.memberservice.notificationService.queue.jobs.SendActivationDateUpdatedEmail;
import com.hedvig.memberservice.notificationService.queue.jobs.SendActivationEmail;
import com.hedvig.memberservice.notificationService.queue.jobs.SendCancellationEmail;
import com.hedvig.memberservice.notificationService.queue.requests.JobRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationAtFutureDateRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationDateUpdatedRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationEmailRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
import java.util.HashMap;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.SqsMessageHeaders;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class JobPoster {

  private final Logger log = LoggerFactory.getLogger(JobPoster.class);

  private final SendCancellationEmail sendCancellationEmail;
  private final SendActivationDateUpdatedEmail sendActivationDateUpdatedEmail;
  private final SendActivationEmail sendActivationEmail;
  private final SendActivationAtFutureDateEmail sendActivationAtFutureDateEmail;
  private final QueueMessagingTemplate queueMessagingTemplate;
  private final ObjectMapper objectMapper;
  private final String queueName;

  public JobPoster(
      SendCancellationEmail sendCancellationEmail,
      SendActivationDateUpdatedEmail sendActivationDateUpdatedEmail,
      SendActivationEmail sendActivationEmail,
      SendActivationAtFutureDateEmail sendActivationAtFutureDateEmail,
      QueueMessagingTemplate queueMessagingTemplate,
      ObjectMapper objectMapper,
      @Value("${hedvig.notification-service.queueTasklist}") String queueName) {
    this.sendCancellationEmail = sendCancellationEmail;
    this.sendActivationDateUpdatedEmail = sendActivationDateUpdatedEmail;
    this.sendActivationEmail = sendActivationEmail;
    this.queueMessagingTemplate = queueMessagingTemplate;
    this.sendActivationAtFutureDateEmail = sendActivationAtFutureDateEmail;
    this.objectMapper = objectMapper;
    this.queueName = queueName;
  }

  public void startJob(JobRequest request, boolean delay) {

    val headers = new HashMap<String, Object>();
    if (delay) {
      headers.put(SQS_DELAY_HEADER, 600);
    }
    SqsMessageHeaders sqsMessageHeaders = new SqsMessageHeaders(headers);
    try {
      String requestAsJson = objectMapper.writeValueAsString(request);
      log.info("Sending jobrequest to sqs queue: {} ", requestAsJson);
      this.queueMessagingTemplate.convertAndSend(queueName, requestAsJson, sqsMessageHeaders);
    } catch (JsonProcessingException ex) {
      log.error("Could not convert request to json: {}", request, ex);
    }
  }

  @SqsListener("${hedvig.notification-service.queueTasklist}")
  public void jobListener(String requestAsJson) {
    try {
      JobRequest request = objectMapper.readValue(requestAsJson, JobRequest.class);
      log.info("Receiving jobrequest from sqs queue: {} ", requestAsJson);

      try{
        MDC.put("memberId", request.getMemberId());

        if (SendOldInsuranceCancellationEmailRequest.class.isInstance(request)) {
          sendCancellationEmail.run((SendOldInsuranceCancellationEmailRequest) request);
        } else if (SendActivationDateUpdatedRequest.class.isInstance(request)) {
          sendActivationDateUpdatedEmail.run((SendActivationDateUpdatedRequest) request);
        } else if (SendActivationEmailRequest.class.isInstance(request)) {
          sendActivationEmail.run((SendActivationEmailRequest) request);
        } else if (SendActivationAtFutureDateEmail.class.isInstance(request)) {
          sendActivationAtFutureDateEmail.run((SendActivationAtFutureDateRequest) request);
        } else {
          log.error("Could not start job for message: {}", requestAsJson);
        }
      }finally{
        MDC.remove("memberId");
      }

    } catch (Exception e) {
      log.error("Caught exception, {}", e.getMessage(), e);
    }
  }
}
