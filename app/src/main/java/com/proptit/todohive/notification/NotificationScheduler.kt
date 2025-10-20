package com.proptit.todohive.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.proptit.todohive.data.local.entity.TaskEntity

object NotificationScheduler {

    fun scheduleTaskReminder(context: Context, task: TaskEntity) {
        if (task.is_deleted || task.is_completed) return
        val eventAt = task.even_at.toEpochMilli()
        val triggerAt = task.even_at.minusSeconds(15 * 60).toEpochMilli()
        val now = System.currentTimeMillis()


        if (now >= eventAt) return

        if (triggerAt <= now && now < eventAt) {
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra("title", task.title)
                putExtra("description", task.description ?: "")
            }

             val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
             val pendingIntent = PendingIntent.getBroadcast(context, task.task_id.toInt(), intent,
                 PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
             try {
                 alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + 1000L, pendingIntent)
             } catch (_: SecurityException) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                     val showpendingIntent = PendingIntent.getActivity(
                         context, task.task_id.toInt(),
                         Intent(context, com.proptit.todohive.ui.HomeActivity::class.java),
                         PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                     )
                     val info = AlarmManager.AlarmClockInfo(now + 1000L, showpendingIntent)
                     alarmManager.setAlarmClock(info, pendingIntent)
                 } else {
                     alarmManager.set(AlarmManager.RTC_WAKEUP, now + 1000L, pendingIntent)
                 }
             }
            return
        }

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("title", task.title)
            putExtra("description", task.description ?: "")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.task_id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (se: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val showpendingIntent = PendingIntent.getActivity(
                        context, task.task_id.toInt(),
                        Intent(context, com.proptit.todohive.ui.HomeActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val info = AlarmManager.AlarmClockInfo(triggerAt, showpendingIntent)
                    alarmManager.setAlarmClock(info, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            }
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}