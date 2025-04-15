package com.ds.highnoonblitz.messages

enum class Purposes(
    val value: Int,
) {
    NETWORK_ACK(1),
    UPDATE_CONNECTED_LIST(2),
    READY(3),
    CONFIGURATION(4),
    ELECTION_REQUEST(5),
    INFO_SHARING(6),
    ELECTION_ACK(7),
    CONSISTENCY_CHECK_REQUEST(8),
    CONSISTENCY_CHECK_REPLY(9),
    GAME_START(10),
    BACK_TO_LOBBY_GAMELIST(11),
    END_GAME(12),
    ELECTION_INGAME_FINISHED(13),
}
