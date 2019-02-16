package com.l2kt.gameserver.model.petition

enum class PetitionState {
    PENDING,
    RESPONDER_CANCEL,
    RESPONDER_MISSING,
    RESPONDER_REJECT,
    RESPONDER_COMPLETE,
    PETITIONER_CANCEL,
    PETITIONER_MISSING,
    IN_PROCESS,
    COMPLETED
}