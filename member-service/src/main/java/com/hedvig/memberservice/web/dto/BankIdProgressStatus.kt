package com.hedvig.memberservice.web.dto

import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse
import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus
import java.lang.IllegalArgumentException

enum class BankIdProgressStatus {
    OUTSTANDING_TRANSACTION,
    NO_CLIENT,
    STARTED,
    USER_SIGN,
    USER_REQ,
    COMPLETE,
    EXPIRED_TRANSACTION,
    CERTIFICATE_ERROR,
    USER_CANCEL,
    CANCELLED,
    START_FAILED;

    companion object {
        fun valueOf(collectResponse: CollectResponse): BankIdProgressStatus =
            when (collectResponse.status) {
                CollectStatus.complete -> {
                    COMPLETE
                }
                CollectStatus.pending -> {
                    when (collectResponse.hintCode) {
                        "outstandingTransaction" -> OUTSTANDING_TRANSACTION
                        "noClient" -> NO_CLIENT
                        "started" -> STARTED
                        "userSign" -> USER_SIGN
                        else -> throw IllegalArgumentException("Unknown collect pending hint code: ${collectResponse.hintCode}")
                    }
                }
                CollectStatus.failed -> {
                    when (collectResponse.hintCode) {
                        "expiredTransaction" -> EXPIRED_TRANSACTION
                        "certificateErr" -> CERTIFICATE_ERROR
                        "userCancel" -> USER_CANCEL
                        "cancelled" -> CANCELLED
                        "startFailed" -> START_FAILED
                        else -> throw IllegalArgumentException("Unknown collect failed hint code: ${collectResponse.hintCode}")
                    }
                }
                else -> {
                    throw RuntimeException("Unhandled collect status ${collectResponse.status.name} hint code: ${collectResponse.hintCode}")
                }
            }
    }
}
