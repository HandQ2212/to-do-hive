package com.proptit.todohive.data.local

import android.content.Context
import androidx.room.*
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.proptit.todohive.data.local.dao.CategoryDao
import com.proptit.todohive.data.local.dao.TaskDao
import com.proptit.todohive.data.local.dao.UserDao
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.data.local.entity.TaskEntity
import com.proptit.todohive.data.local.entity.UserEntity
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Database(
    entities = [UserEntity::class, CategoryEntity::class, TaskEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(InstantConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(context, AppDatabase::class.java, "todo.db")
                    .fallbackToDestructiveMigration(true)
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = db
                db
            }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }
}

fun instantAt(dayOffset: Long, h: Int, m: Int): Instant {
    val today = LocalDate.now().plusDays(dayOffset)
    return today.atTime(LocalTime.of(h, m))
        .atZone(ZoneId.systemDefault()).toInstant()
}

suspend fun seedIfEmpty(db: AppDatabase) {
    val userDao = db.userDao()
    val catDao = db.categoryDao()
    val taskDao = db.taskDao()

    if (userDao.count() > 0) return

    db.withTransaction {
        val existing = userDao.getByEmail("demo@ex.com")
        val meId = existing?.user_id ?: userDao.insert(
            UserEntity(
                username = "demo",
                password_hash = hashPassword("hash"),
                email = "demo@ex.com",
                avatar_url = "https://res.cloudinary.com/da4dr8ghb/image/upload/v1759733914/Unknown_person_pjgtdt.jpg"
            )
        )

        fun hex(s: String) = s
        val homeId = catDao.upsert(
            CategoryEntity(
                name = "Home",
                color_hex = hex("#EF5350"),
                icon = "@drawable/ic_home",
                owner_user_id = null
            )
        )
        val workId = catDao.upsert(
            CategoryEntity(
                name = "Work",
                color_hex = hex("#FBC02D"),
                icon = "@drawable/ic_flag",
                owner_user_id = null
            )
        )
        val sportId = catDao.upsert(
            CategoryEntity(
                name = "Sport",
                color_hex = hex("#76FF03"),
                icon = "@drawable/ic_flag",
                owner_user_id = null
            )
        )
        val universityId = catDao.upsert(
            CategoryEntity(name = "University", color_hex = hex("#6C63FF"), owner_user_id = null)
        )
        val designId = catDao.upsert(
            CategoryEntity(name = "Design", color_hex = hex("#64FFDA"), owner_user_id = null)
        )
        val socialId = catDao.upsert(
            CategoryEntity(name = "Social", color_hex = hex("#FF80AB"), owner_user_id = null)
        )
        val musicId = catDao.upsert(
            CategoryEntity(name = "Music", color_hex = hex("#E040FB"), owner_user_id = null)
        )
        val healthId = catDao.upsert(
            CategoryEntity(name = "Health", color_hex = hex("#69F0AE"), owner_user_id = null)
        )
        val movieId = catDao.upsert(
            CategoryEntity(name = "Movie", color_hex = hex("#82B1FF"), owner_user_id = null)
        )
        val groceryId = catDao.upsert(
            CategoryEntity(name = "Grocery", color_hex = hex("#B2FF59"), owner_user_id = null)
        )
        val mySideProjectId = catDao.upsert(
            CategoryEntity(
                name = "My Side Project",
                color_hex = hex("#9D4EDD"),
                owner_user_id = meId
            )
        )

        taskDao.upsert(
            TaskEntity(
                title = "Do Math Homework",
                even_at = instantAt(0, 16, 45),
                priority = 1,
                user_id = meId,
                category_id = universityId,
                is_completed = false
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Take out dogs",
                even_at = instantAt(0, 18, 20),
                priority = 2,
                user_id = meId,
                category_id = homeId,
                is_completed = false
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Business meeting with CEO",
                even_at = instantAt(0, 8, 15),
                priority = 3,
                user_id = meId,
                category_id = workId,
                is_completed = false
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Buy Grocery",
                even_at = instantAt(-1, 16, 45),
                priority = 2,
                user_id = meId,
                category_id = groceryId,
                is_completed = true
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Prepare Slide Deck",
                even_at = instantAt(+1, 9, 30),
                priority = 1,
                user_id = meId,
                category_id = workId,
                is_completed = false
            )
        )
        taskDao.upsert(
            TaskEntity(
                title = "Ship side project MVP",
                even_at = instantAt(+2, 20, 0),
                priority = 1,
                user_id = meId,
                category_id = mySideProjectId,
                is_completed = false
            )
        )
    }
}

private fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}