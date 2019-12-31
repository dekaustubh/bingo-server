package com.dekaustubh.repositories

import com.dekaustubh.constants.Db.LIMIT
import com.dekaustubh.constants.Db.OFFSET
import com.dekaustubh.extensions.toLeaderboard
import com.dekaustubh.models.Leaderboard
import com.dekaustubh.models.Leaderboards
import com.dekaustubh.utils.TimeUtil
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface LeaderboardRepository {
    fun getLeaderboardForRoom(roomId: Long, limit: Int = LIMIT, offset: Int = OFFSET): List<Leaderboard>

    fun updateLeaderboardForRoom(roomId: Long, userId: Long, points: Int): Leaderboard?

    fun getUserInLeaderboard(roomId: Long, userId: Long): Leaderboard?
}

class LeaderboardRepositoryImpl() : LeaderboardRepository {
    override fun getLeaderboardForRoom(roomId: Long, limit: Int, offset: Int): List<Leaderboard> {
        val leaders = mutableListOf<Leaderboard>()
        transaction {
            Leaderboards.select { Leaderboards.room_id eq roomId }
                .limit(limit, offset)
                .forEach { leaders.add(it.toLeaderboard()) }
        }
        return leaders
    }

    override fun updateLeaderboardForRoom(roomId: Long, userId: Long, points: Int): Leaderboard? {
        transaction {
            Leaderboards.update({ (Leaderboards.room_id eq roomId) and (Leaderboards.user_id eq userId) }) {
                it[Leaderboards.points] = points
                it[updated_at] = TimeUtil.getCurrentUtcMillis()
            }
        }
        return getUserInLeaderboard(roomId, userId)
    }

    override fun getUserInLeaderboard(roomId: Long, userId: Long): Leaderboard? {
        var leaderboard : Leaderboard? = null
        transaction {
            leaderboard = Leaderboards.select { (Leaderboards.room_id eq roomId) and (Leaderboards.user_id eq userId) }
                .mapNotNull { it.toLeaderboard() }
                .singleOrNull()
        }
        return leaderboard
    }

}