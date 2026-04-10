import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const ANTHROPIC_API_KEY = Deno.env.get("ANTHROPIC_API_KEY")!;
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

interface QuizQuestion {
  id: string;
  text: string;
  options: string[];
  correctIndex: number;
  explanation: string;
}

async function generateQuizWithClaude(lessonContent: unknown): Promise<QuizQuestion[]> {
  const lessonText = JSON.stringify(lessonContent, null, 2);

  const prompt = `You are an expert quiz designer creating questions for a daily learning app.

Based on this lesson content, generate exactly 5 multiple-choice questions.

Lesson content:
${lessonText}

Requirements:
- Each question tests genuine understanding, not just recall
- 4 answer options per question — one correct, three plausible distractors
- Distractors should be common misconceptions or related-but-wrong ideas
- Explanation (1-2 sentences) clarifying why the correct answer is right
- Questions should vary in difficulty (2 easy, 2 medium, 1 hard)

Return ONLY valid JSON array:
[
  {
    "id": "q1",
    "text": "string — the question",
    "options": ["string", "string", "string", "string"],
    "correctIndex": 0,
    "explanation": "string — why this answer is correct"
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
      max_tokens: 2048,
      temperature: 0.3,
      messages: [{ role: "user", content: prompt }],
    }),
  });

  if (!response.ok) {
    throw new Error(`Claude API error: ${response.status}`);
  }

  const data = await response.json();
  const text = data.content?.[0]?.text ?? "[]";

  try {
    return JSON.parse(text);
  } catch {
    // Try to extract JSON from response
    const match = text.match(/\[[\s\S]*\]/);
    if (match) return JSON.parse(match[0]);
    throw new Error("Failed to parse Claude quiz response");
  }
}

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
    const { lessonId, userId } = await req.json();

    if (!lessonId || !userId) {
      return new Response(JSON.stringify({ error: "Missing lessonId or userId" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      });
    }

    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

    // Fetch lesson content
    const { data: lesson, error } = await supabase
      .from("lessons")
      .select("content_json, topic, category")
      .eq("id", lessonId)
      .eq("user_id", userId)
      .single();

    if (error || !lesson) {
      return new Response(JSON.stringify({ error: "Lesson not found" }), {
        status: 404,
        headers: { "Content-Type": "application/json" },
      });
    }

    // Retry logic: 3 attempts with exponential backoff
    let questions: QuizQuestion[] = [];
    for (let attempt = 0; attempt < 3; attempt++) {
      try {
        questions = await generateQuizWithClaude(lesson.content_json);
        break;
      } catch (e) {
        if (attempt === 2) throw e;
        await new Promise((r) => setTimeout(r, Math.pow(2, attempt) * 1000));
      }
    }

    return new Response(JSON.stringify({ questions }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error("generate-quiz error:", error);
    return new Response(
      JSON.stringify({ error: error instanceof Error ? error.message : "Unknown error" }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});
