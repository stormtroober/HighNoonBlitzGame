package com.ds.highnoonblitz.messages

import android.util.Log
import com.ds.highnoonblitz.leaderelection.BullyElectionManager

class MessageFactory private constructor() {
    companion object {
        fun createReadyMessage(isReady: Boolean): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .addMessageParameter("ready", isReady)
                .setPurpose(Purposes.READY.value)
                .build()

        fun createConfigurationMessage(devicesMac: List<String> = emptyList()): MessageComposed {
            Log.i("MessageFactory", "Creating configuration message with devices: $devicesMac")
            return MessageComposed.Companion
                .MessageBuilder()
                .addMessageParameter("devices", devicesMac)
                .setPurpose(Purposes.CONFIGURATION.value)
                .build()
        }

        fun createElectionMessage(): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .addMessageParameter("ID", BullyElectionManager.sessionID)
                .setPurpose(Purposes.ELECTION_REQUEST.value)
                .build()

        fun createInfoSharingMessage(
            masterAddress: String,
            isReady: Boolean,
        ): MessageComposed {
            Log.i(
                "MessageFactory",
                "Creating info sharing message with masterAddress: $masterAddress and isReady: $isReady UUID: ${BullyElectionManager.sessionID}",
            )
            return MessageComposed.Companion
                .MessageBuilder()
                .addMessageParameter("masterAddress", masterAddress)
                .addMessageParameter("UUID", BullyElectionManager.sessionID)
                .addMessageParameter("status", isReady)
                .setPurpose(Purposes.INFO_SHARING.value)
                .build()
        }

        fun createElectionAcknowledgeMessage(): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .addMessageParameter("ACK", "ACK")
                .setPurpose(Purposes.ELECTION_ACK.value)
                .build()

        fun createConsistencyCheckRequestMessage(): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .setPurpose(Purposes.CONSISTENCY_CHECK_REQUEST.value)
                .build()

        fun createConsistencyCheckReplyMessage(macAddresses: List<String>): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .addMessageParameter("macAddresses", macAddresses)
                .setPurpose(Purposes.CONSISTENCY_CHECK_REPLY.value)
                .build()

        fun createGameStartMessage(): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .setPurpose(Purposes.GAME_START.value)
                .build()

        fun createBackToLobbyGameListMessage(): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .setPurpose(Purposes.BACK_TO_LOBBY_GAMELIST.value)
                .build()

        fun createElectionInGameFinishedMessage(): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .setPurpose(Purposes.ELECTION_INGAME_FINISHED.value)
                .build()

        fun createEndGameMessage(timeDifference: Long): MessageComposed =
            MessageComposed.Companion
                .MessageBuilder()
                .setPurpose(Purposes.END_GAME.value)
                .addMessageParameter("timeDifference", timeDifference)
                .build()
    }
}
