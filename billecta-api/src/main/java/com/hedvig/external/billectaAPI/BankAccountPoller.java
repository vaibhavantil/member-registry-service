package com.hedvig.external.billectaAPI;

import com.hedvig.external.billectaAPI.api.BankAccountRequest;
import com.hedvig.external.billectaAPI.api.BankAccountStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BankAccountPoller implements Runnable {

    private final Logger log = LoggerFactory.getLogger(BankAccountPoller.class);
    private final String publicId;
    private final BillectaApi api;
    private final ScheduledExecutorService executor;
    private Consumer<BankAccountRequest> onComplete;
    private final Consumer<String> onError;

    private int numberOfExecutions = 0;
    private int maxExecutions = 200;

    public BankAccountPoller(String publicId,
                             BillectaApi api,
                             ScheduledExecutorService executor,
                             Consumer<BankAccountRequest> onComplete,
                             Consumer<String> onError) {
        this.publicId = publicId;
        this.api = api;
        this.executor = executor;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    @Override
    public void run() {

        numberOfExecutions++;

        try {
            ResponseEntity<BankAccountRequest> account = api.getBankAccountNumbers(publicId);

            log.debug("Response from billecta#getBankAccountNumbers: {}, {}",
                    account.getStatusCode().toString(),
                    account.getBody().getStatus());
                    //account.getBody() != null ? account.getBody().getStatus().value() : "....");

            if (account.getStatusCode().is2xxSuccessful() && account.getBody().getStatus() == BankAccountStatusType.SUCCESS) {
                onComplete.accept(account.getBody());
                return;
            }else if(account.getStatusCode().is2xxSuccessful() && account.getBody().getStatus() != BankAccountStatusType.WAITING) {
                log.error("Konstigt state: {}", account.getBody().getStatus());
                onError.accept("Kunde inte hämta bankkonton.");
                //Stop polling
                numberOfExecutions = maxExecutions;
            }
        }
        catch(Exception e) {
            log.error("Could not communicate with billecta", e);
        }

        try {
            if (numberOfExecutions < maxExecutions) {
                log.debug("Rescheduling bank-account poll for {}", publicId);
                executor.schedule(this, 1000, TimeUnit.MILLISECONDS);
            } else {
                log.error("Could not retrieve bank-account numbers for {}", publicId);
            }
        } catch (Exception e) {
            log.error("Could not reschedule publicId: " + this.publicId, e);
        }
    }
}