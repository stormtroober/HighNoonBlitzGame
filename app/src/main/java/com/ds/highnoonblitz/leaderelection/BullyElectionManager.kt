package com.ds.highnoonblitz.leaderelection

import OneShotTimer
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.ds.highnoonblitz.GameConstants
import com.ds.highnoonblitz.MainThreadUtil
import java.math.BigInteger
import java.util.UUID

class BullyElectionManager(private val electionFunction: () -> Unit) {
    companion object {
        val sessionID: String = UUID.randomUUID().toString()
    }

    private var electionList = mutableStateListOf<Pair<String, String>>()
    private var electionTimer = OneShotTimer(GameConstants.ELECTION_TIMER) {
        becomeCoordinator()
    }

    fun addElectionListMember(deviceAddress: String, UUID: String) {
        if (electionList.none { it.first == deviceAddress && it.second == UUID }) {
            electionList.add(Pair(deviceAddress, UUID))
        }
    }

    fun removeElectionListMember(deviceAddress: String) {
        Log.i("BullyElectionManager", "Removing from electionList:"+electionList.removeIf { it.first == deviceAddress })
    }

    fun getDevicesToElect(): List<String> {
        val myUUID = BigInteger(sessionID.replace("-", ""), 16)
        return electionList.filter { BigInteger(it.second.replace("-", ""), 16) > myUUID }
            .map { it.first }
    }

    fun startElectionTimer() {

        if(!electionTimer.isRunning()){
            Log.i("BullyElectionManager", "Starting election timer")
            Log.i("BullyElectionManager", sessionID)
            //MainThreadUtil.makeToast("Starting election timer")
            electionTimer.start()
        }else{
            Log.i("BullyElectionManager", "Election timer already running")
        }

    }

    fun stopElectionTimer() {
        Log.i("BullyElectionManager", "Stopping election timer")
        electionTimer.stop()
        //MainThreadUtil.makeToast("Election timer stopped")
    }

    private fun becomeCoordinator() {
        electionFunction()
        Log.i("BullyElectionManager", "Im the new coordinator!")
        MainThreadUtil.makeToast("Im the new coordinator!")
    }

    fun clearElectionList(){
        electionList.clear()
    }
}