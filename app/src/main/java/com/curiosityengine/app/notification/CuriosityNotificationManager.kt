package com.curiosityengine.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.curiosityengine.app.MainActivity
import com.curiosityengine.app.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CuriosityNotificationManager @Inject constructor() {

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                NotificationChannels.LESSON_READY,
                "Daily Lesson",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when your daily lesson is ready"
            },
            NotificationChannel(
                NotificationChannels.STREAK_RISK,
                "Streak Alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you when your streak is at risk"
            },
            NotificationChannel(
                NotificationChannels.WEEKLY_REVIEW,
                "Weekly Review",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when your weekly review quiz is ready"
            },
            NotificationChannel(
                NotificationChannels.MILESTONE,
                "Milestones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Celebrates streak milestones and achievements"
            },
            NotificationChannel(
                NotificationChannels.KEEP_LEARNING,
                "Keep Learning",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Motivational nudges to keep learning"
                enableLights(false)
                enableVibration(false)
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    fun showLessonReadyNotification(context: Context, topic: String, category: String) {
        val deepLinkUri = Uri.parse("curiosityengine://lesson/today")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_LESSON_READY,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.LESSON_READY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Your lesson is ready")
            .setContentText("Your lesson is ready: $topic")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Your lesson is ready: $topic"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_LESSON_READY, notification)
    }

    fun showStreakRiskNotification(context: Context, streakDays: Int) {
        val deepLinkUri = Uri.parse("curiosityengine://lesson/today")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_STREAK_RISK,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.STREAK_RISK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Streak at risk!")
            .setContentText("Don't break your $streakDays-day streak! Read today's lesson.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_STREAK_RISK, notification)
    }

    fun showWeeklyReviewNotification(context: Context) {
        val deepLinkUri = Uri.parse("curiosityengine://quiz/weekly")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_WEEKLY_REVIEW,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.WEEKLY_REVIEW)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Weekly Review Ready")
            .setContentText("Your weekly review is ready. Test what you learned!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_WEEKLY_REVIEW, notification)
    }

    fun showMilestoneNotification(context: Context, milestone: Int) {
        val deepLinkUri = Uri.parse("curiosityengine://streak")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_MILESTONE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.MILESTONE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Milestone reached!")
            .setContentText("\uD83D\uDD25 $milestone-day streak milestone reached!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MILESTONE, notification)
    }

    fun showKeepLearningNotification(context: Context) {
        val deepLinkUri = Uri.parse("curiosityengine://lesson/today")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_KEEP_LEARNING,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification =
            NotificationCompat.Builder(context, NotificationChannels.KEEP_LEARNING)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Keep learning!")
                .setContentText("You're on a roll! Keep the momentum going.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_KEEP_LEARNING, notification)
    }

    companion object {
        private const val NOTIFICATION_ID_LESSON_READY = 1001
        private const val NOTIFICATION_ID_STREAK_RISK = 1002
        private const val NOTIFICATION_ID_WEEKLY_REVIEW = 1003
        private const val NOTIFICATION_ID_MILESTONE = 1004
        private const val NOTIFICATION_ID_KEEP_LEARNING = 1005
    }
}
