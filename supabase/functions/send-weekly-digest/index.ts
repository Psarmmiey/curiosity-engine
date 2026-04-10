import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const RESEND_API_KEY = Deno.env.get("RESEND_API_KEY")!;
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const FROM_EMAIL = "Curiosity Engine <noreply@curiosityengine.app>";

function buildEmailHtml(
  displayName: string,
  stats: {
    lessonsRead: number;
    quizzesTaken: number;
    avgScore: number;
    chainStreak: number;
  },
  lessons: Array<{
    topic: string;
    category: string;
    emoji: string;
    date: string;
    quizScore: number | null;
    isRead: boolean;
  }>
): string {
  const lessonRows = lessons
    .map(
      (l) => `
    <tr>
      <td style="padding: 12px; border-bottom: 1px solid #1E3A5F;">
        <span style="font-size: 20px;">${l.emoji}</span>
        <strong style="color: #E8E4DB; margin-left: 8px;">${l.topic}</strong>
        <br>
        <small style="color: #8899AA;">${l.category} · ${l.date}</small>
      </td>
      <td style="padding: 12px; border-bottom: 1px solid #1E3A5F; text-align: right; color: ${l.isRead ? "#4AC58A" : "#E8A020"};">
        ${
          l.quizScore !== null
            ? `${l.quizScore}/5`
            : l.isRead
            ? "✓ Read"
            : "Not started"
        }
      </td>
    </tr>
  `
    )
    .join("");

  return `
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Your week in learning</title>
</head>
<body style="background: #0F1E35; color: #E8E4DB; font-family: 'Helvetica Neue', Arial, sans-serif; margin: 0; padding: 20px;">
  <div style="max-width: 600px; margin: 0 auto;">

    <!-- Header -->
    <div style="border-bottom: 3px solid #C8932A; padding-bottom: 20px; margin-bottom: 24px;">
      <h1 style="color: #C8932A; font-size: 24px; margin: 0;">Curiosity Engine</h1>
      <p style="color: #8899AA; margin: 8px 0 0;">Weekly Learning Digest</p>
    </div>

    <!-- Greeting -->
    <h2 style="color: #E8E4DB; font-size: 20px;">Your week in learning, ${displayName} 👋</h2>

    <!-- Stats -->
    <div style="background: #1E3A5F; border-radius: 12px; padding: 20px; margin: 20px 0; display: flex; gap: 16px; flex-wrap: wrap;">
      <div style="text-align: center; flex: 1; min-width: 80px;">
        <div style="font-size: 28px; font-weight: bold; color: #C8932A;">${stats.lessonsRead}</div>
        <div style="font-size: 12px; color: #8899AA;">Lessons</div>
      </div>
      <div style="text-align: center; flex: 1; min-width: 80px;">
        <div style="font-size: 28px; font-weight: bold; color: #C8932A;">${stats.quizzesTaken}</div>
        <div style="font-size: 12px; color: #8899AA;">Quizzes</div>
      </div>
      <div style="text-align: center; flex: 1; min-width: 80px;">
        <div style="font-size: 28px; font-weight: bold; color: #C8932A;">${stats.avgScore}%</div>
        <div style="font-size: 12px; color: #8899AA;">Avg Score</div>
      </div>
      <div style="text-align: center; flex: 1; min-width: 80px;">
        <div style="font-size: 28px; font-weight: bold; color: #C8932A;">🔥${stats.chainStreak}</div>
        <div style="font-size: 12px; color: #8899AA;">Streak</div>
      </div>
    </div>

    <!-- Lessons Table -->
    <h3 style="color: #E8E4DB;">This Week's Lessons</h3>
    <table style="width: 100%; border-collapse: collapse; background: #162844; border-radius: 12px; overflow: hidden;">
      ${lessonRows}
    </table>

    <!-- CTA -->
    <div style="text-align: center; margin: 32px 0;">
      <a href="curiosityengine://quiz/weekly"
         style="background: #C8932A; color: #0F1E35; padding: 14px 28px; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 16px;">
        Take Weekly Review Quiz →
      </a>
    </div>

    <!-- Footer -->
    <div style="border-top: 1px solid #1E3A5F; padding-top: 16px; margin-top: 24px; text-align: center; color: #4A5568; font-size: 12px;">
      <p>Curiosity Engine · One topic at a time</p>
      <p><a href="curiosityengine://settings/notifications" style="color: #8899AA;">Manage notifications</a></p>
    </div>
  </div>
</body>
</html>`;
}

serve(async (_req) => {
  try {
    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

    // Get all users with weekly digest enabled
    const { data: users } = await supabase
      .from("users")
      .select("id, email, display_name, timezone")
      .eq("email_weekly_digest", true)
      .not("email", "is", null);

    if (!users || users.length === 0) {
      return new Response(JSON.stringify({ sent: 0 }), {
        headers: { "Content-Type": "application/json" },
      });
    }

    let sentCount = 0;

    for (const user of users) {
      try {
        const sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

        // Fetch week's lessons
        const { data: lessons } = await supabase
          .from("lessons")
          .select("topic, category, emoji, date, is_read")
          .eq("user_id", user.id)
          .eq("is_bonus", false)
          .gte("date", sevenDaysAgo.toISOString().split("T")[0])
          .order("date", { ascending: true });

        // Fetch quiz results
        const { data: quizResults } = await supabase
          .from("quiz_results")
          .select("score, total, lesson_id")
          .eq("user_id", user.id)
          .eq("quiz_type", "daily")
          .gte("completed_at", sevenDaysAgo.toISOString());

        // Fetch streak
        const { data: streak } = await supabase
          .from("streaks")
          .select("chain_streak")
          .eq("user_id", user.id)
          .single();

        const lessonsRead = lessons?.filter((l) => l.is_read).length ?? 0;
        const quizzesTaken = quizResults?.length ?? 0;
        const avgScore =
          quizResults && quizResults.length > 0
            ? Math.round(
                (quizResults.reduce((sum, r) => sum + r.score / r.total, 0) /
                  quizResults.length) *
                  100
              )
            : 0;

        // Build lesson list with quiz scores
        const quizScoreMap = new Map(
          quizResults?.map((r) => [r.lesson_id, r.score]) ?? []
        );

        const lessonList = (lessons ?? []).map((l: Record<string, unknown>) => ({
          topic: l.topic as string,
          category: l.category as string,
          emoji: (l.emoji as string) ?? "📚",
          date: l.date as string,
          isRead: l.is_read as boolean,
          quizScore: quizScoreMap.get(l.id as string) ?? null,
        }));

        const html = buildEmailHtml(
          user.display_name ?? user.email ?? "there",
          {
            lessonsRead,
            quizzesTaken,
            avgScore,
            chainStreak: streak?.chain_streak ?? 0,
          },
          lessonList
        );

        // Send via Resend
        const emailResponse = await fetch("https://api.resend.com/emails", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${RESEND_API_KEY}`,
          },
          body: JSON.stringify({
            from: FROM_EMAIL,
            to: user.email,
            subject: `Your week in learning — ${lessonsRead} topics, ${avgScore}% retention`,
            html,
          }),
        });

        if (emailResponse.ok) sentCount++;
      } catch (e) {
        console.error(`Failed to send digest to ${user.email}:`, e);
      }
    }

    return new Response(JSON.stringify({ sent: sentCount, total: users.length }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error("send-weekly-digest error:", error);
    return new Response(
      JSON.stringify({ error: error instanceof Error ? error.message : "Unknown error" }),
      { status: 500 }
    );
  }
});
