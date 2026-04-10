import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const ANTHROPIC_API_KEY = Deno.env.get("ANTHROPIC_API_KEY")!;
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response(null, {
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
      },
    });
  }

  try {
    const { userId } = await req.json();
    if (!userId) {
      return new Response(JSON.stringify({ error: "Missing userId" }), { status: 400 });
    }

    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

    // Fetch last 7 days of completed non-bonus lessons
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    const { data: lessons } = await supabase
      .from("lessons")
      .select("topic, category, content_json, date")
      .eq("user_id", userId)
      .eq("is_bonus", false)
      .eq("is_read", true)
      .gte("date", sevenDaysAgo.toISOString().split("T")[0])
      .order("date", { ascending: true });

    if (!lessons || lessons.length === 0) {
      return new Response(JSON.stringify({ error: "No lessons found for this week" }), {
        status: 404,
      });
    }

    const lessonsSummary = lessons.map((l) => ({
      topic: l.topic,
      category: l.category,
      date: l.date,
      content: l.content_json,
    }));

    const prompt = `You are creating a weekly retention quiz for a learning app.

The user completed these lessons this week:
${JSON.stringify(lessonsSummary, null, 2)}

Generate exactly 7 questions — approximately one per lesson (or distribute evenly if fewer lessons).
Each question must include a "lessonTopic" field indicating which lesson it tests.

Requirements:
- Test retention across all covered topics
- Vary difficulty
- Focus on the most important concepts from each lesson

Return ONLY valid JSON:
[
  {
    "id": "wq1",
    "text": "string",
    "options": ["string", "string", "string", "string"],
    "correctIndex": 0,
    "explanation": "string",
    "lessonTopic": "string — the topic this question comes from"
  }
]`;

    const response = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-api-key": ANTHROPIC_API_KEY,
        "anthropic-version": "2023-06-01",
      },
      body: JSON.stringify({
        model: "claude-sonnet-4-6",
        max_tokens: 3000,
        temperature: 0.3,
        messages: [{ role: "user", content: prompt }],
      }),
    });

    const data = await response.json();
    const text = data.content?.[0]?.text ?? "[]";
    let questions;
    try {
      questions = JSON.parse(text);
    } catch {
      const match = text.match(/\[[\s\S]*\]/);
      questions = match ? JSON.parse(match[0]) : [];
    }

    return new Response(JSON.stringify({ questions, lessonCount: lessons.length }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error("generate-weekly-quiz error:", error);
    return new Response(
      JSON.stringify({ error: error instanceof Error ? error.message : "Unknown error" }),
      { status: 500 }
    );
  }
});
