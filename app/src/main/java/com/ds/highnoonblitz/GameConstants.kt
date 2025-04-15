package com.ds.highnoonblitz

import java.util.UUID

object GameConstants {
    const val BUTTON_TIMER_MIN_DELAY = 2000L
    const val BUTTON_TIMER_MAX_DELAY = 5000L
    const val GAME_TIMEOUT = 30000L
    const val CHECK_END_GAME = 1000L
    const val CONSISTENCY_CHECK_TIMEOUT = 5000L
    const val ELECTION_TIMER = 3500L
    const val DISCOVERABLE_TIME = 60L
    val BT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
}
