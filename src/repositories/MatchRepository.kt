package com.dekaustubh.repositories

import com.dekaustubh.extensions.toMatch
import com.dekaustubh.extensions.toPlayers
import com.dekaustubh.extensions.toStringPlayers
import com.dekaustubh.models.Match
import com.dekaustubh.models.Matches
import com.dekaustubh.utils.TimeUtil
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Interface to interact with [Match] data.
 */
interface MatchRepository {
    /**
     * Creates a new match with [createdBy].
     * @return [Match] if successfully created, null otherwise.
     */
    fun createMatch(createdBy: Long, roomId: Long): Match?

    /**
     * Fetches a match with id [matchId].
     * @return Updated [Match] if found, null otherwise.
     */
    fun getMatchById(matchId: Long): Match?

    /**
     * User with [userId] joins a match specified with [matchId].
     * @return Updated [Match] if successfully joined, null otherwise.
     */
    fun joinMatch(matchId: Long, userId: Long): Match?

    /**
     * Update a match specified with [matchId].
     * @return Updated [Match] if successfully joined, null otherwise.
     */
    fun updateMatch(matchId: Long, winnerId: Long, userIds: List<Long> = emptyList(), winnerPoints: Int = 0): Match?
}

class MatchRepositoryImpl() : MatchRepository {
    override fun createMatch(createdBy: Long, roomId: Long): Match? {
        var key = 0L
        transaction {
            key = (Matches.insert {
                it[created_by] = createdBy
                it[room_id] = roomId
                it[players] = createdBy.toString()
                it[created_at] = TimeUtil.getCurrentUtcMillis()
            } get Matches.id)

            commit()
        }
        return getMatchById(key)
    }

    override fun getMatchById(matchId: Long): Match? {
        var match: Match? = null
        transaction {
            match = Matches
                .select { (Matches.id eq matchId) and (Matches.deleted_at eq 0) }
                .mapNotNull { it.toMatch() }
                .singleOrNull()
        }
        return match
    }

    override fun joinMatch(matchId: Long, userId: Long): Match? {
        val match = getMatchById(matchId)
        match?.let {
            val players = match.players
            players.add(userId)
            return updateMatch(matchId, 0, players)
        } ?: return null
    }

    override fun updateMatch(matchId: Long, winnerId: Long, userIds: List<Long>, winnerPoints: Int): Match? {
        transaction {
            Matches.update({ (Matches.id eq matchId) and (Matches.deleted_at eq 0) }) {
                if (winnerId != 0L) it[winner_id] = winnerId
                it[points] = winnerPoints
                it[players] = userIds.toStringPlayers()
                it[created_at] = TimeUtil.getCurrentUtcMillis()
                it[updated_at] = TimeUtil.getCurrentUtcMillis()
            }
            commit()
        }
        return getMatchById(matchId)
    }
}