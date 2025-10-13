package com.proptit.todohive.ui.home.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.dao.UserDao
import com.proptit.todohive.data.local.entity.UserEntity
import com.proptit.todohive.data.local.model.TaskWithCategory
import com.proptit.todohive.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.time.Instant

data class ProfileUiState(
    val displayName: String = "Guest",
    val avatarUrl: String? = null,
    val tasksLeft: Int = 0,
    val tasksDone: Int = 0
)

class ProfileViewModel(
    private val userDao: UserDao,
    private val taskRepo: TaskRepository?,
    private val userId: Long?
) : ViewModel() {

    private val userLive: LiveData<UserEntity?> =
        if (userId != null) userDao.observeById(userId) else MutableLiveData(null)

    private val tasksLive: LiveData<List<TaskWithCategory>> =
        taskRepo?.observeAll() ?: MutableLiveData(emptyList())

    val uiState: LiveData<ProfileUiState> = MediatorLiveData<ProfileUiState>().apply {
        var currentUser: UserEntity? = null
        var currentTasks: List<TaskWithCategory> = emptyList()

        fun update() {
            val leftCount = currentTasks.count { !it.task.is_completed }
            val doneCount = currentTasks.count { it.task.is_completed }

            val state = ProfileUiState(
                displayName = currentUser?.username ?: "Guest",
                avatarUrl = currentUser?.avatar_url,
                tasksLeft = leftCount,
                tasksDone = doneCount
            )

            Log.d("ProfileVM", "UI STATE => $state")
            value = state
        }

        addSource(userLive) { u ->
            currentUser = u
            Log.d("ProfileVM", "🔄 userLive → ${u?.username ?: "null"} (id=${u?.user_id})")
            update()
        }

        addSource(tasksLive) { ts ->
            currentTasks = ts
            Log.d("ProfileVM", "🔄 tasksLive → ${ts.size} task(s)")
            update()
        }
    }

    fun updateDisplayName(newName: String) {
        val id = userId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userDao.updateNameById(id, newName, Instant.now())
        }
    }

    fun updatePasswordHash(oldPlain: String, newPlain: String, onResult: (Boolean, String) -> Unit) {
        val id = userId ?: return onResult(false, "No user logged in")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userDao.getById(id)
                if (user == null) {
                    withContext(Dispatchers.Main) { onResult(false, "User not found") }
                    return@launch
                }

                val oldHash = sha256(oldPlain)
                if (user.password_hash != oldHash) {
                    withContext(Dispatchers.Main) { onResult(false, "Old password incorrect") }
                    return@launch
                }

                val newHash = sha256(newPlain)
                userDao.updatePasswordHashById(id, newHash, Instant.now())
                withContext(Dispatchers.Main) { onResult(true, "Password updated successfully") }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, e.message ?: "Error updating password")
                }
            }
        }
    }

    fun updateAvatar(url: String?) {
        val id = userId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userDao.updateAvatarById(id, url, Instant.now())
        }
    }

    private fun sha256(s: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

class ProfileViewModelFactory(private val ctx: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val prefs = ctx.getSharedPreferences("app", Context.MODE_PRIVATE)
        val userId = prefs.getLong("current_user_id", -1L).takeIf { it > 0L }

        Log.d("ProfileFactory", "🏁 current_user_id=$userId")

        val db = AppDatabase.get(ctx)
        val userDao = db.userDao()
        val taskRepo = userId?.let { TaskRepository(db, it) }

        return ProfileViewModel(userDao, taskRepo, userId) as T
    }
}
