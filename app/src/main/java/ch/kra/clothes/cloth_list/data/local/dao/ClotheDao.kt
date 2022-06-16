package ch.kra.clothes.cloth_list.data.local.dao

import androidx.room.*
import ch.kra.clothes.cloth_list.data.local.entitiy.ClotheEntity
import ch.kra.clothes.cloth_list.domain.model.Clothe
import kotlinx.coroutines.flow.Flow

@Dao
interface ClotheDao {
    @Query("SELECT * FROM ClotheEntity WHERE userListId = :userId")
    fun getClotheList(userId: Int): Flow<List<ClotheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothes(clothes: List<ClotheEntity>): List<Long>

    @Delete
    suspend fun deleteClothes(clothes: List<ClotheEntity>)

    @Query("DELETE FROM ClotheEntity WHERE userListId = :userId")
    suspend fun deleteClothes(userId: Int)
}