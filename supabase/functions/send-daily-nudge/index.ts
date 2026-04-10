import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
// FCM calls would require a service account JWT — stub for now
// const FCM_SERVER_KEY = Deno.env.get("FCM_SERVER_KEY")!;

serve(async (_req) => {
  try {
    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);
    const today = new Date().toISOString().split("T")[0];

    // Find users who:
    // 1. Have daily nudge email enabled
    // 2. Have NOT completed their daily target today
    const { data: users } = await supabase
      .from("users")
      .select("id, email, display_name, daily_target")
      .eq("email_daily_nudge", true)
      .not("email", "is", null);

    if (!users) {
      return new Response(JSON.stringify({ nudged: 0 }), {
        headers: { "Content-Type": "application/json" },
      });
    }

    let nudgedCount = 0;

    for (const user of users) {
      try {
        // Check if user met today's target
        const { count } = await supabase
          .from("lessons")
          .select("*", { count: "exact", head: true })
          .eq("user_id", user.id)
          .eq("date", today)
          .eq("is_bonus", false)
          .eq("is_read", true);

        if ((count ?? 0) >= user.daily_target) continue; // Already met target

        // Fetch today's lesson topic (if generated)
        const { data: todayLesson } = await supabase
          .from("lessons")
          .select("topic, emoji, category")
          .eq("user_id", user.id)
          .eq("date", today)
          .eq("is_bonus", false)
          .limit(1)
          .single();

        const topic = todayLesson?.topic ?? "your daily lesson";
        const emoji = todayLesson?.emoji ?? "📚";

        // In production: send FCM push notification
        // For now: log the nudge (FCM integration added in Agent M)
        console.log(
          `Would nudge user ${user.id}: "${emoji} ${topic}"`
        );

        nudgedCount++;
      } catch (e) {
        console.error(`Failed to process nudge for ${user.id}:`, e);
      }
    }

    return new Response(JSON.stringify({ nudged: nudgedCount, total: users.length }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error("send-daily-nudge error:", error);
    return new Response(
      JSON.stringify({ error: error instanceof Error ? error.message : "Unknown error" }),
      { status: 500 }
    );
  }
});
