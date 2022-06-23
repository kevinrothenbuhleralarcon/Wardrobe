package ch.kra.wardrobe.cloth_list.data.repository

import ch.kra.wardrobe.cloth_list.data.local.dao.ClotheDao
import ch.kra.wardrobe.cloth_list.data.local.dao.UserWardrobeDao
import ch.kra.wardrobe.cloth_list.data.local.entitiy.ClotheEntity
import ch.kra.wardrobe.cloth_list.data.local.entitiy.UserWardrobeEntity
import ch.kra.wardrobe.cloth_list.domain.model.UserWardrobe
import ch.kra.wardrobe.cloth_list.domain.model.UserWardrobeWithClothes
import ch.kra.wardrobe.cloth_list.domain.repository.WardrobeRepository
import kotlinx.coroutines.flow.*

class WardrobeRepositoryImpl(
    private val clotheDao: ClotheDao,
    private val userWardrobeDao: UserWardrobeDao
): WardrobeRepository {
    override fun getUsersList(): Flow<List<UserWardrobe>> {
        return userWardrobeDao.getUsersList().map { list -> list.map { it.toUserList() } }
    }

    override fun getUserListWithClothById(id: Int): Flow<UserWardrobeWithClothes> {
        return userWardrobeDao.getUserListWithClothesByUserId(id).map { it.toUserListWithClothes() }
    }

    override suspend fun addUserListWithClothes(userWardrobeWithClothes: UserWardrobeWithClothes) {
        val userWardrobeEntity = UserWardrobeEntity(
            userId = userWardrobeWithClothes.userWardrobe.id ?: 0,
            username = userWardrobeWithClothes.userWardrobe.username,
            location = userWardrobeWithClothes.userWardrobe.location,
            lastUpdated = userWardrobeWithClothes.userWardrobe.lastUpdated
        )
        val id = userWardrobeDao.insertUserList(userWardrobeEntity).toInt()
        if (id != 0) {
            val clotheEntityList = userWardrobeWithClothes.listClothe.map {
               ClotheEntity(
                    clothId = it.id ?: 0,
                    clothe = it.clothe,
                    quantity = it.quantity,
                    typeId = it.typeId,
                    userWardrobeId = id
                )
            }
            clotheDao.insertClothes(clotheEntityList)
        }
    }

    override suspend fun updateUserListWithClothes(userWardrobeWithClothes: UserWardrobeWithClothes) {
        userWardrobeWithClothes.userWardrobe.id?.let { userId ->
            val userWardrobeEntity = UserWardrobeEntity(
                userId = userWardrobeWithClothes.userWardrobe.id,
                username = userWardrobeWithClothes.userWardrobe.username,
                location = userWardrobeWithClothes.userWardrobe.location,
                lastUpdated = userWardrobeWithClothes.userWardrobe.lastUpdated
            )
            userWardrobeDao.updateUserList(userWardrobeEntity)
            val clotheEntityList = userWardrobeWithClothes.listClothe.map {
                ClotheEntity(
                    clothId = it.id ?: 0,
                    clothe = it.clothe,
                    quantity = it.quantity,
                    typeId = it.typeId,
                    userWardrobeId = userId
                )
            }
            // get the currently stored clothes for this userList
            val currentlyStoredClothes = clotheDao.getClotheList(userId).firstOrNull()
            // if the list is not null we remove the elements that are no longer present in the current list
            currentlyStoredClothes?.let {
                val clothesToDelete = it.subtract(clotheEntityList.toSet()).toList()
                clotheDao.deleteClothes(clothesToDelete)
            }

            // we add the new list, if the cloth is already in the table it will be replaced with the new data
            clotheDao.insertClothes(clotheEntityList)
        }
    }

    override suspend fun deleteUserListWithClothes(userListId: Int) {
        clotheDao.deleteClothes(userListId)
        userWardrobeDao.deleteUserList(userListId)
    }
}