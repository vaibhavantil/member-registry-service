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
    private final ScheduledExecutorService xecutor;
    private Consumer<BankAccountRequest> completeAction;

    private int noExecutions = 0;
    private int maxExecutions = 30;

    public BankAccountPoller(String publicId,
                             BillectaApi api,
                             ScheduledExecutorService xecutor, Consumer<BankAccountRequest> completeAction) {
        this.publicId = publicId;
        this.api = api;
        this.xecutor = xecutor;
        this.completeAction = completeAction;
    }

    @Override
    public void run() {

        noExecutions++;

        try {
            ResponseEntity<BankAccountRequest> account = api.getBankAccountNumbers(publicId);

            log.debug("Response from billecta#getBankAccountNumbers: {},  ",
                    account.getStatusCode().toString(),
                    account.getBody() != null ? account.getBody().getStatus().value() : "");

            if (account.getStatusCode().is2xxSuccessful() && account.getBody().getStatus() == BankAccountStatusType.SUCCESS) {
                completeAction.accept(account.getBody());
                return;
            }
        }
        catch(Exception e) {
            log.error("Could not communicate with billecta", e);
        }

        try {
            if (noExecutions < maxExecutions) {
                log.debug("Rescheduling bank-account poll for {}", publicId);
                xecutor.schedule(this, 500, TimeUnit.MILLISECONDS);
            } else {
                log.error("Could not retrieve bank-account numbers for {}", publicId);
            }
        } catch (Exception e) {
            log.error("Could not reschedule publicId: " + this.publicId, e);
        }
    }
}