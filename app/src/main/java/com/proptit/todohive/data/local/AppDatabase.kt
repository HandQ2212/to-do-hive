package com.proptit.todohive.data.local

import android.content.Context
import androidx.room.*
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.proptit.todohive.data.local.dao.CategoryDao
import com.proptit.todohive.data.local.dao.TaskDao
import com.proptit.todohive.data.local.dao.UserDao
import com.proptit.todohive.data.local.entity.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Database(
    entities = [UserEntity::class, CategoryEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(InstantConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(context, AppDatabase::class.java, "todo.db")
                    .fallbackToDestructiveMigration(true)
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = db; db
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
            UserEntity(username = "demo", password_hash = "hash", email = "demo@ex.com")
        )

        val universityId = catDao.upsert(CategoryEntity(name = "University", color_hex = "#6C63FF"))
        val homeId       = catDao.upsert(CategoryEntity(name = "Home",       color_hex = "#EF5350"))
        val workId       = catDao.upsert(CategoryEntity(name = "Work",       color_hex = "#FBC02D"))

        taskDao.upsert(TaskEntity(title="Do Math Homework", even_at=instantAt(0,16,45),
            priority=1, user_id=meId, category_id=universityId, is_completed=false))
        taskDao.upsert(TaskEntity(title="Tack out dogs", even_at=instantAt(0,18,20),
            priority=2, user_id=meId, category_id=homeId, is_completed=false))
        taskDao.upsert(TaskEntity(title="Business meeting with CEO", even_at=instantAt(0,8,15),
            priority=3, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="Buy Grocery", even_at=instantAt(-1,16,45),
            priority=2, user_id=meId, category_id=homeId, is_completed=true))
        taskDao.upsert(TaskEntity(title="Prepare Slide Deck", even_at=instantAt(+1,9,30),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="a Slide Deck", even_at=instantAt(+1,9,29),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="b Slide Deck", even_at=instantAt(+1,9,28),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="c Slide Deck", even_at=instantAt(+1,9,27),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="d Slide Deck", even_at=instantAt(+1,9,26),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="e Slide Deck", even_at=instantAt(+1,9,25),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="f Slide Deck", even_at=instantAt(+1,9,24),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="g Slide Deck", even_at=instantAt(+1,9,23),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="h Slide Deck", even_at=instantAt(+1,9,33),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="i Slide Deck", even_at=instantAt(+1,9,22),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="k Slide Deck", even_at=instantAt(+1,9,21),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
        taskDao.upsert(TaskEntity(title="l Slide Deck", even_at=instantAt(+1,9,20),
            priority=1, user_id=meId, category_id=workId, is_completed=false))

        taskDao.upsert(TaskEntity(title="aewr Slide Deck", even_at=instantAt(+1,9,19),
            priority=1, user_id=meId, category_id=workId, is_completed=false))
    }
}