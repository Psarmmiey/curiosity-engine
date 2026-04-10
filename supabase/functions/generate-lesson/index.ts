import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const GEMINI_API_KEY = Deno.env.get("GEMINI_API_KEY")!;
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const YOUTUBE_API_KEY = Deno.env.get("YOUTUBE_API_KEY")!;

const LESSON_PROMPT = (category: string, date: string) => `
You are an expert educator creating a deeply researched daily lesson.

Generate a single educational lesson for the category: "${category}" for date: ${date}.

Return ONLY valid JSON with this exact structure:
{
  "topic": "string — specific, compelling topic title",
  "category": "${category}",
  "emoji": "string — single relevant emoji",
  "hook": "string — 2-3 sentence compelling opener that makes the reader lean in",
  "estimatedReadMinutes": 7,
  "blocks": [
    // Mix of the following block types (minimum 3 blocks, maximum 6):
    // TEXT: { "type": "text", "title": "string", "content": "string — 4-5 sentences of accurate content" }
    // IMAGE: { "type": "image", "imageUrl": "", "caption": "string" }
    // FLASHCARD: { "type": "flashcard", "cards": [{ "term": "string", "definition": "string" }] } (3-6 cards)
    // SLIDE: { "type": "slide", "headline": "string", "points": ["string", "string", "string"], "stat": "string?" }
    // QUOTE: { "type": "quote", "text": "string", "attribution": "string", "subtype": "quote|fact" }
    // VIDEO: omit this type — handled separately via YouTube API
  ],
  "keyTakeaways": ["string", "string", "string"],
  "rememberThis": "string — vivid mnemonic or analogy",
  "references": [
    {
      "title": "string",
      "type": "paper|book|institution|encyclopaedia|journalism|official",
      "author": "string",
      "year": 2024,
      "url": "string or null",
      "description": "string — one sentence on what this source contributes",
      "confidence": "high|medium"
    }
  ],
  "youtube_query": "string — specific search query for a relevant educational video",
  "video_suitable": true
}

Rules:
- Only cite sources you are highly confident are real. Do not fabricate DOIs, URLs, or author names.
- If uncertain about a specific URL, set url to null.
- Include 2-6 references.
- The lesson must be factually accurate and educational, not superficial.
- Vary block types for visual interest.
`;

async function callGemini(prompt: string, model: string): Promise<string> {
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${GEMINI_API_KEY}`;
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      contents: [{ parts: [{ text: prompt }] }],
      generationConfig: {
        temperature: 0.7,
        responseMimeType: "application/json",
      },
    }),
  });

  if (!response.ok) {
    throw new Error(`Gemini API error: ${response.status} ${await response.text()}`);
  }

  const data = await response.json();
  return data.candidates?.[0]?.content?.parts?.[0]?.text ?? "";
}

async function searchYouTube(query: string): Promise<{
  videoId: string;
  title: string;
  channel: string;
  durationSeconds: number;
  thumbnailUrl: string;
} | null> {
  try {
    const url = new URL("https://www.googleapis.com/youtube/v3/search");
    url.searchParams.set("part", "snippet");
    url.searchParams.set("q", query);
    url.searchParams.set("type", "video");
    url.searchParams.set("videoDuration", "medium");
    url.searchParams.set("safeSearch", "strict");
    url.searchParams.set("maxResults", "5");
    url.searchParams.set("key", YOUTUBE_API_KEY);

    const response = await fetch(url.toString());
    if (!response.ok) return null;

    const data = await response.json();
    const items = data.items ?? [];
    if (items.length === 0) return null;

    const first = items[0];
    return {
      videoId: first.id.videoId,
      title: first.snippet.title,
      channel: first.snippet.channelTitle,
      durationSeconds: 0, // Would need /videos endpoint for duration
      thumbnailUrl: first.snippet.thumbnails?.high?.url ?? "",
    };
  } catch {
    return null;
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
    const { userId, category, date, model = "gemini-2.5-pro", isBonus = false } = await req.json();

    if (!userId || !category || !date) {
      return new Response(JSON.stringify({ error: "Missing required fields" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      });
    }

    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

    // Check if lesson already exists (idempotency)
    if (!isBonus) {
      const { data: existing } = await supabase
        .from("lessons")
        .select("id")
        .eq("user_id", userId)
        .eq("date", date)
        .eq("is_bonus", false)
        .limit(1)
        .single();

      if (existing) {
        const { data: full } = await supabase
          .from("lessons")
          .select("*")
          .eq("id", existing.id)
          .single();
        return new Response(JSON.stringify(full), {
          headers: { "Content-Type": "application/json" },
        });
      }
    }

    // Generate lesson with Gemini
    let lessonJson: string;
    try {
      lessonJson = await callGemini(LESSON_PROMPT(category, date), model);
    } catch {
      // Fallback to Flash
      lessonJson = await callGemini(LESSON_PROMPT(category, date), "gemini-2.5-flash");
    }

    let lessonData: Record<string, unknown>;
    try {
      lessonData = JSON.parse(lessonJson);
    } catch {
      throw new Error("Failed to parse Gemini response as JSON");
    }

    // Search YouTube for relevant video
    let videoData = null;
    if (lessonData.video_suitable && lessonData.youtube_query) {
      videoData = await searchYouTube(lessonData.youtube_query as string);
    }

    // Build video block if found
    if (videoData) {
      const blocks = (lessonData.blocks as unknown[]) ?? [];
      blocks.push({
        type: "video",
        youtubeVideoId: videoData.videoId,
        title: videoData.title,
        channel: videoData.channel,
        thumbnailUrl: videoData.thumbnailUrl,
        durationSeconds: videoData.durationSeconds,
      });
      lessonData.blocks = blocks;
    }

    // Generate quiz via Claude (internal call)
    const quizResponse = await supabase.functions.invoke("generate-quiz-internal", {
      body: { lessonContent: lessonData, userId },
    });
    const quizData = quizResponse.data ?? { questions: [] };

    // Persist lesson
    const { data: savedLesson, error: saveError } = await supabase
      .from("lessons")
      .insert({
        user_id: userId,
        date,
        topic: lessonData.topic,
        category: lessonData.category,
        emoji: lessonData.emoji ?? "📚",
        content_json: lessonData.blocks ?? [],
        quiz_json: quizData.questions ?? [],
        references_json: lessonData.references ?? [],
        youtube_video_id: videoData?.videoId ?? null,
        youtube_title: videoData?.title ?? null,
        youtube_channel: videoData?.channel ?? null,
        youtube_duration_s: videoData?.durationSeconds ?? null,
        is_bonus: isBonus,
        generated_by: model,
      })
      .select()
      .single();

    if (saveError) {
      throw new Error(`Failed to save lesson: ${saveError.message}`);
    }

    return new Response(JSON.stringify(savedLesson), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error("generate-lesson error:", error);
    return new Response(
      JSON.stringify({ error: error instanceof Error ? error.message : "Unknown error" }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});
