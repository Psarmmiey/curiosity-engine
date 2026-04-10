# CURIOSITY ENGINE
## Complete Product Requirements Document
### Android Mobile Application — v3.0 (Consolidated)

> **A polished, AI-powered daily knowledge system** · Multi-format learning · Daily & weekly quizzes · Home screen widget · Email digest · References · YouTube · Sharing · Doom Scroll

---

| Field | Detail |
|---|---|
| **Author** | Samuel Oladipupo |
| **Version** | 3.0 — Consolidated (combines PRD v1, v2, and v3 Addendum) |
| **Date** | April 2026 |
| **Platform** | Android (Kotlin + Jetpack Compose + Material You) |
| **Backend** | Supabase (Postgres · Auth · Edge Functions · Realtime) |
| **Lesson AI** | Gemini 2.5 Pro (stable) · Gemini 3.1 Pro Preview (cutting edge) — user-configurable |
| **Quiz AI** | Claude Sonnet 4.6 (`claude-sonnet-4-6`) |
| **Image AI** | Gemini 2.5 Flash Image — "Nano Banana" (stable, ~$0.039/image) |
| **Status** | Final Draft — For Development |

---

## Table of Contents

1. [Vision & Problem Statement](#1-vision--problem-statement)
2. [Architecture Overview](#2-architecture-overview)
3. [User Journeys](#3-user-journeys)
4. [Functional Requirements](#4-functional-requirements)
5. [Rich Learning Formats](#5-rich-learning-formats)
6. [AI Model Specification](#6-ai-model-specification)
7. [References & Source Authentication](#7-references--source-authentication)
8. [Notification System](#8-notification-system)
9. [Dual Streak System](#9-dual-streak-system)
10. [YouTube Video Integration](#10-youtube-video-integration)
11. [Daily Learning Targets & Doom Scroll](#11-daily-learning-targets--doom-scroll)
12. [Sharing System](#12-sharing-system)
13. [Design Language Specification](#13-design-language-specification)
14. [Android App — Screen Inventory](#14-android-app--screen-inventory)
15. [Data Models](#15-data-models)
16. [Tech Stack](#16-tech-stack)
17. [Email Specification](#17-email-specification)
18. [Non-Functional Requirements](#18-non-functional-requirements)
19. [Development Roadmap](#19-development-roadmap)

---

## 1. Vision & Problem Statement

### 1.1 The Core Problem

Most people want to grow intellectually every day but have no system that forces it. News apps give event-driven knowledge. Trivia apps give shallow facts. Books require sustained focus. There is no product that gives you one deep, well-structured topic per day, makes you actively process it, tests your retention, and tracks your growth over time.

### 1.2 Product Vision

Curiosity Engine is a personal knowledge OS that delivers structured daily lessons, forces active recall through quizzes, and tracks your intellectual growth over time — all from your Android home screen.

It is not a trivia app. It is not a news app. It is a deliberate daily learning habit built into the surface you touch most: your phone's home screen widget.

### 1.3 Design Principles

- **One lesson per day (minimum)** — depth over breadth. Not 10 facts, one explored idea. Configurable to more.
- **Active recall over passive reading** — every lesson ends with a quiz.
- **Spaced repetition built in** — weekly cross-topic review tests retention over time.
- **Frictionless habit surface** — widget means you never have to open the app to stay aware.
- **Dual AI personality** — Gemini teaches, Claude tests. Different cognitive modes, different models.
- **Authenticated knowledge** — every lesson cites real, verifiable sources.
- **Email as accountability layer** — weekly digest arrives in your inbox whether you opened the app or not.

### 1.4 Success Metrics

| Metric | Target |
|---|---|
| Daily Active Use | User completes daily target 5+ days per week |
| Retention Rate | Quiz scores improve measurably over 30 days |
| Streak Length | Average streak > 7 days after 3 weeks of use |
| Email Engagement | Weekly digest opened within 24 hours of delivery |
| Topic Breadth | User explores at least 4 distinct categories per month |

---

## 2. Architecture Overview

### 2.1 High-Level Architecture

The system is intentionally lean for a personal-use application. The Android app is the primary client. Supabase serves as the backend-as-a-service layer. All AI orchestration happens through Supabase Edge Functions, meaning API keys never exist on the device.

**Why Supabase:**
- No infrastructure to manage — no server to maintain
- Built-in Google Sign-In auth handles cross-device identity instantly
- Edge Functions handle AI orchestration, keeping API keys off the device entirely
- Realtime subscriptions enable instant cross-device lesson sync
- Already part of the existing stack — zero new tooling to learn
- Free tier is sufficient for personal use indefinitely

### 2.2 Component Map

| Component | Role |
|---|---|
| **Android App** | Kotlin + Jetpack Compose. Primary UI. Handles lesson display, quiz flow, streak tracking, settings. |
| **Home Screen Widget** | Glance API. Shows today's topic, target progress, streak. Tappable to open lesson. |
| **Supabase Postgres** | Stores users, lessons, quiz results, streaks, email preferences. Source of truth. |
| **Supabase Auth** | Google Sign-In. Handles identity. Enables cross-device sync. |
| **Supabase Edge Functions** | Deno-based serverless. Calls Gemini for lessons + images. Calls Claude for quizzes. Calls YouTube API for video matching. Sends emails via Resend. |
| **Gemini 2.5 Pro / 3.1 Pro** | Primary lesson content generator. |
| **Gemini 2.5 Flash Image** | AI image generation for lesson visual blocks. |
| **Claude Sonnet 4.6** | Quiz and assessment generator. |
| **YouTube Data API v3** | Video search and metadata for video lesson blocks. |
| **Resend** | Transactional email delivery. |
| **WorkManager** | Schedules daily lesson pre-fetch and local notifications. |

### 2.3 Data Flow — Daily Lesson

| Step | Actor | Action | Detail |
|---|---|---|---|
| 1 | WorkManager | Morning pre-fetch trigger | At 6 AM, WorkManager wakes the app in background |
| 2 | Android App | Check local cache | If today's lesson exists in Room DB, skip network call |
| 3 | Supabase Edge Fn | Lesson generation request | App calls `/generate-lesson` with user ID, category, date |
| 4 | Edge Function | Check if already generated | Avoid duplicate AI calls; return cached lesson if exists |
| 5 | Gemini 2.5 Pro | Generate lesson content | Returns structured JSON with blocks array |
| 6 | Gemini 2.5 Flash Image | Generate lesson images | Called per image block. Stored to Supabase Storage. |
| 7 | YouTube API | Find matching video | Query using Gemini-suggested search terms |
| 8 | Claude Sonnet 4.6 | Generate quiz | Receives full lesson JSON; returns 5 questions with explanations |
| 9 | Supabase Postgres | Persist lesson | Full lesson + quiz + references stored, associated with user_id and date |
| 10 | Android App | Cache locally | Lesson stored in Room DB for offline access; widget updated |
| 11 | Glance Widget | Update home screen | Widget displays progress, streak, and reading status |

---

## 3. User Journeys

### 3.1 First-Time Onboarding Journey

*Persona: Samuel installs the app for the first time.*

| # | Actor | Action | Experience |
|---|---|---|---|
| 1 | Samuel | Installs & opens the app | Calm dark-themed splash with animated orb and the name "Curiosity Engine" |
| 2 | App | Value proposition screen | One screen. No carousel. "One deep topic per day. Read it. Prove it. Remember it." |
| 3 | Samuel | Taps Get Started | Smooth transition into Google Sign-In. One tap. |
| 4 | Auth | Google Sign-In | Supabase Auth handles it. User record created automatically. |
| 5 | App | Category preference screen | Grid of 8 categories. Multi-select. Samuel picks Science, History, Psychology. |
| 6 | App | Daily target screen | Segmented control: 1 · 2 · 3 · 5 · 10 · Custom. Default 1. |
| 7 | App | Learning time preference | Time picker for daily reminder. Default: 8:00 AM. |
| 8 | App | Widget prompt | Full-screen prompt showing what the widget looks like. "Add to your home screen." |
| 9 | Samuel | Widget placed | Android widget picker opens. Samuel drags widget to home screen. |
| 10 | App | Generate first lesson | Immediately generates today's lesson. Gemini called. Widget updates. |
| 11 | App | Lesson ready | App transitions to lesson screen. First intellectual journey begins. |

### 3.2 Daily Morning Habit Journey

*Persona: Samuel has used the app for 2 weeks. It is 7:58 AM on a Wednesday.*

| # | Actor | Action | Experience |
|---|---|---|---|
| 1 | WorkManager | Pre-fetch at 6 AM | Lesson silently generated and cached. Widget updates while Samuel sleeps. |
| 2 | Notification | Reminder fires at 8 AM | "Today: The Milgram Obedience Experiments — your 14-day streak is on the line 🔥" |
| 3 | Samuel | Glances at widget | Widget shows topic title, target progress (0/1), streak count. |
| 4 | Samuel | Taps the widget | App opens directly to today's lesson — no home screen, no navigation. |
| 5 | App | Lesson screen renders | Hook paragraph loads. Three rich content blocks unfold. Image loads via Coil. |
| 6 | Samuel | Reads through blocks | Text blocks, flashcard deck (swipes through terms), image block. References collapsed at bottom. |
| 7 | Samuel | Taps References | Collapses open. Sees 3 sources. Taps the Wikipedia link — Chrome Custom Tab opens. |
| 8 | App | Quiz prompt appears | Sticky bottom bar: "Ready to prove it? 5 questions." Cannot be permanently dismissed. |
| 9 | Samuel | Starts quiz | Full-screen quiz mode. One question per screen. Progress dots at top. |
| 10 | App | Immediate feedback | Each answer reveals colour feedback + one-sentence explanation. |
| 11 | App | Quiz results | Score as large fraction. Verdict. Wrong questions with correct answers. |
| 12 | App | Streak updated | Chain streak increments to 15. Confetti animation. |
| 13 | Widget | Updates | Home screen widget: 1/1 ✓ in gold. Streak: 15. |

### 3.3 Doom Scroll Journey

*Persona: Samuel has met his daily target (2 lessons) and has some time.*

| # | Actor | Action | Experience |
|---|---|---|---|
| 1 | App | Target met celebration | Confetti. Badge: "✓ Daily goal complete — 2/2". Streak secured. |
| 2 | App | "Keep Exploring" CTA | Card slides up: "Your curiosity doesn't have to stop. Keep going →" |
| 3 | Samuel | Taps Keep Exploring | Transitions to Doom Scroll feed. |
| 4 | App | Generate bonus lesson | Gemini 2.5 Flash generates a lesson instantly (pre-generated in background). |
| 5 | Samuel | Reads lesson | Same rich experience. Optional quiz offered at bottom — not required. |
| 6 | App | "Next →" at bottom | Tapping it loads the next pre-generated lesson instantly. |
| 7 | App | Category nudge (after 3) | "You've read 3 Psychology topics today — want to try History?" |
| 8 | Samuel | Continues or stops | Session ends when Samuel closes the app. All bonus lessons logged in journal. |

### 3.4 Weekly Review Journey

*Persona: Sunday evening. Samuel completed 5 lessons this week.*

| # | Actor | Action | Experience |
|---|---|---|---|
| 1 | Email | Weekly digest at 7 PM | Subject: "Your week in learning — 5 topics, 4 quizzes, 87% retention" |
| 2 | Samuel | Opens email | Beautiful HTML email. Each topic with emoji, date, quiz score. |
| 3 | Email | Missed day flagged | Wednesday marked in amber. "You skipped The Milgram Experiments — tap to read." |
| 4 | Email | Stats block | Avg quiz score: 87%. Best topic: Cell Biology (5/5). Streak: 12 days. |
| 5 | Samuel | Taps Weekly Review Quiz | Deep link opens app at Weekly Review screen. |
| 6 | App | Weekly quiz | 7 questions, one from each lesson this week. Topic label above each question. |
| 7 | Samuel | Completes review | Sees overall retention score. Identifies which topics need revisiting. |
| 8 | App | Suggests next week | Based on lowest scores, recommends categories for next week. |

### 3.5 Cross-Device Sync Journey

| # | Actor | Action | Detail |
|---|---|---|---|
| 1 | Samuel | Reads lesson on phone, takes quiz | Lesson marked complete. Score saved to Supabase in real time. |
| 2 | Samuel | Picks up tablet later | Opens app on tablet. Supabase Realtime subscription fires on app open. |
| 3 | App (tablet) | Syncs state | Today's lesson shows as completed. Quiz score reflected. Streak accurate. |
| 4 | App | No duplicate generation | Edge function checks user_id + date before calling AI. One lesson per user per day. |

### 3.6 Missed Day Recovery Journey

| # | Actor | Action | Detail |
|---|---|---|---|
| 1 | App | Tuesday's lesson preserved | Missed lessons not discarded. Stored indefinitely in journal. |
| 2 | Wednesday morning | Notification acknowledges miss | "You missed yesterday — Tuesday's lesson is waiting. No pressure, just curiosity." |
| 3 | Samuel | Taps missed lesson in Journal | Opens the preserved lesson. Reads it. Takes the quiz. |
| 4 | App | Streak behaviour | Streak does not increment for backdated completion. Honest tracking. |
| 5 | App | Email reflects it | Tuesday shown as completed late with a small "late" badge. Transparent record. |

---

## 4. Functional Requirements

### 4.1 Lesson System

| ID | Feature | Description | Priority | Complexity |
|---|---|---|---|---|
| L-01 | Daily Lesson Generation | One lesson per user per day via Gemini through Supabase Edge Function. Cached in Postgres and Room DB. | P0 | High |
| L-02 | Category Selection | User selects 1–4 preferred categories. Daily lesson drawn from rotation. | P0 | Low |
| L-03 | Surprise Me Mode | Option to receive a completely random topic ignoring category preferences for that day. | P1 | Low |
| L-04 | Block-Based Lesson Structure | Lessons composed from typed blocks: text, image, flashcard, slide, quote, video. | P0 | High |
| L-05 | Reading Progress Tracking | Track scroll depth. Mark lesson as "read" at 90% scroll completion. | P1 | Medium |
| L-06 | Lesson History / Journal | Scrollable journal of all past lessons with date, topic, category, quiz score, completion status. | P0 | Medium |
| L-07 | Offline Lesson Access | Today's lesson and last 7 days cached locally in Room DB. Readable without network. | P1 | Medium |
| L-08 | Lesson Pre-fetch | WorkManager pre-fetches lesson at 6 AM so it is ready before the user wakes. | P1 | Medium |

### 4.2 Quiz System

| ID | Feature | Description | Priority | Complexity |
|---|---|---|---|---|
| Q-01 | Daily Quiz (5 Questions) | Claude Sonnet 4.6 generates 5 multiple-choice questions per lesson. | P0 | High |
| Q-02 | Immediate Explanation Feedback | After each answer, show why correct/wrong in one clear sentence. | P0 | Low |
| Q-03 | Quiz Score Persistence | Score saved to Supabase per lesson per user. Used in weekly stats and email. | P0 | Low |
| Q-04 | Weekly Review Quiz | Claude generates a 7-question cross-topic quiz from past week's lessons. | P0 | High |
| Q-05 | Quiz Retry | User can retake a daily quiz once. Best score recorded. | P2 | Low |
| Q-06 | Quiz Results Review | After completion, show all questions with correct answers and explanations. | P1 | Low |
| Q-07 | Retention Score Tracking | 30-day rolling average quiz score displayed on profile screen. | P1 | Medium |

### 4.3 Home Screen Widget

| ID | Feature | Description | Priority | Complexity |
|---|---|---|---|---|
| W-01 | Glance Widget — 4×2 | Shows: topic title, category emoji, target progress (X/Y), streak, reading status. | P0 | High |
| W-02 | Widget Tap to Lesson | Tapping deep-links directly to today's lesson — bypass home screen. | P0 | Low |
| W-03 | Widget State Transitions | Widget updates in real time when lesson is read, quiz done, or target met. | P1 | Medium |
| W-04 | Small Widget Variant (2×2) | Topic emoji, streak, and read/unread dot for minimal home screens. | P2 | Medium |
| W-05 | Widget Theming | Widget respects Android system light/dark theme via Glance MaterialTheme. | P1 | Low |
| W-06 | Target Progress Display | Shows "X/Y today" and fills progress indicator per lesson completed. | P1 | Low |

---

## 5. Rich Learning Formats

### 5.1 Core Design Principle

Gemini decides the format mix for each lesson. The API response includes a `blocks` array where each block carries a `type` field. The Android app renders each block type using a dedicated Compose component. This makes the lesson experience feel handcrafted, not templated.

A biology lesson about cell division will look different from a history lesson about the Byzantine Empire.

### 5.2 The Six Block Types

#### 📝 TEXT BLOCK — Rich Narrative Prose
- The backbone of every lesson. Up to 5 sentences of accurate, layered educational content.
- Typography: DM Serif Display at 18sp for section headings, Lato Regular 16sp for body.
- When used: For complex ideas needing sustained explanation — mechanisms, causes, timelines.
- Max 3 text blocks per lesson.

#### 🖼️ IMAGE BLOCK — AI-Generated Visual
- Generated by Gemini 2.5 Flash Image ("Nano Banana") based on lesson topic.
- Full-width rounded card at 16:9 or 3:2 ratio. Skeleton loader while generating.
- Caption in italic 13sp. Long-press to expand full screen.
- SynthID watermark applied. Images cached to Supabase Storage.
- When used: Topics with strong visual identity — anatomy, geography, art history, physics.
- Max 2 image blocks per lesson.

#### 🃏 FLASHCARD BLOCK — Swipeable Term Cards
- Horizontal swipeable deck of 3–6 term/definition pairs.
- HorizontalPager with flip animation on tap (front = term, back = definition).
- Coloured top-strip matching the lesson's category colour.
- When used: Vocabulary-heavy topics — medical terms, historical figures, philosophical schools.
- Cards reused as quiz distractors by Claude's quiz generator.

#### 📊 SLIDE BLOCK — Visual Summary Slide
- Single-screen summary card as a stylised infographic.
- Content: one central headline, 3–4 bullet points with icons, optional stat or quote.
- Background uses a subtle gradient from the category's colour palette.
- When used: As a section recap or "At a Glance" summary.

#### 💬 QUOTE / FACT SPOTLIGHT BLOCK
- Single sentence or short quote in large display text with attribution.
- Two sub-types: `QUOTE` (attributed to a person) and `FACT` (standalone verifiable fact).
- Rendering: Large italic text 22sp, attribution in 13sp small caps, accent line on left.
- Maximum 1 per lesson.

#### ▶️ VIDEO BLOCK — YouTube Embed
- YouTube video matched to lesson topic via YouTube Data API v3.
- Rendered as 16:9 thumbnail with play button overlay.
- Tap: player opens in BottomSheet (80% screen height).
- Falls back to YouTube search chip if no suitable video found.
- See [Section 10](#10-youtube-video-integration) for full spec.

### 5.3 Lesson JSON Schema v3 — Block-Based Structure

```json
{
  "topic": "string",
  "category": "string",
  "emoji": "string",
  "hook": "string — 2-3 sentence compelling opener",
  "estimatedReadMinutes": 7,
  "blocks": [
    { "type": "text", "title": "string", "content": "string (4-5 sentences)" },
    { "type": "image", "imageUrl": "string (set by Edge Fn after generation)", "caption": "string" },
    { "type": "flashcards", "cards": [{ "term": "string", "definition": "string" }] },
    { "type": "slide", "headline": "string", "points": ["string"], "stat": "string?" },
    { "type": "quote", "text": "string", "attribution": "string?", "subtype": "quote|fact" },
    { "type": "video", "youtube_id": "string", "title": "string", "channel": "string",
      "duration_seconds": 240, "thumbnail_url": "string", "placement": "intro|mid|summary",
      "intent": "string — why this video was chosen" }
  ],
  "keyTakeaways": ["string", "string", "string"],
  "rememberThis": "string — mnemonic or vivid analogy",
  "references": [
    {
      "title": "string",
      "type": "paper|book|institution|encyclopaedia|journalism|official",
      "author": "string?",
      "year": 2024,
      "url": "string?",
      "doi": "string?",
      "description": "string — one sentence on what this source contributes",
      "confidence": "high|medium"
    }
  ],
  "youtube_query": "string — suggested by Gemini for video search",
  "video_suitable": true
}
```

### 5.4 Kotlin Sealed Class — Block Rendering

```kotlin
sealed class LessonBlock {
  data class TextBlock(val title: String, val content: String) : LessonBlock()
  data class ImageBlock(val imageUrl: String, val caption: String) : LessonBlock()
  data class FlashcardBlock(val cards: List<Flashcard>) : LessonBlock()
  data class SlideBlock(val headline: String, val points: List<String>, val stat: String?) : LessonBlock()
  data class QuoteBlock(val text: String, val attribution: String?, val subtype: QuoteType) : LessonBlock()
  data class VideoBlock(val youtubeId: String, val title: String, val channel: String,
                        val durationSeconds: Int, val thumbnailUrl: String, val intent: String) : LessonBlock()
}

@Composable
fun LessonBlockRenderer(block: LessonBlock) {
  when (block) {
    is TextBlock      -> TextBlockCard(block)
    is ImageBlock     -> ImageBlockCard(block)    // Coil async + skeleton
    is FlashcardBlock -> FlashcardDeck(block)     // HorizontalPager + flip animation
    is SlideBlock     -> SlideCard(block)         // Custom Compose layout
    is QuoteBlock     -> QuoteSpotlight(block)    // Large display text
    is VideoBlock     -> VideoBlockCard(block)    // Thumbnail + BottomSheet player
  }
}
```

---

## 6. AI Model Specification

### 6.1 Current Model Roster

#### Lesson Generation — Gemini

| Setting | Value |
|---|---|
| **Default (Stable)** | `gemini-2.5-pro` — $1.25/$10.00 per 1M tokens · 1M context · 65K output · GA since June 2025 |
| **Cutting Edge (Preview)** | `gemini-3.1-pro-preview` — $2.00–4.00/$12.00–18.00 per 1M tokens · Released Feb 2026 |
| **Fast Fallback** | `gemini-2.5-flash` — $0.30/$2.50 per 1M tokens · Used when Pro is rate-limited or for doom scroll |
| **Image Generation** | `gemini-2.5-flash-image` ("Nano Banana") — ~$0.039/image · Stable |
| **Temperature** | 0.7 for prose · 0.4 for structured section scaffolding |
| **Thinking Budget** | 2,048 tokens (configurable) |

#### Quiz Generation — Claude

| Setting | Value |
|---|---|
| **Model** | `claude-sonnet-4-6` (Claude Sonnet 4.6 — latest Sonnet) |
| **Temperature** | 0.3 — deterministic for consistent quiz quality |
| **Why Claude** | Superior at nuanced question design, calibrated distractors, pedagogically sound explanations |

### 6.2 Configurable Models in Settings

**Settings → AI Models screen:**

```
Section: Lesson Generator (Gemini)
  ○  Gemini 2.5 Pro         [RECOMMENDED]   Best balance of quality and reliability
  ○  Gemini 3.1 Pro Preview  [CUTTING EDGE]  Highest capability, preview model
  ○  Gemini 2.5 Flash        [FAST]          Quicker generation, slightly lighter output

Section: Quiz Generator (Claude)
  ℹ  Claude Sonnet 4.6 — always uses the latest Claude Sonnet  [read-only]

Section: Lesson Images
  Toggle: Generate AI images for lessons  [default ON]
  Sub-label: Uses Gemini 2.5 Flash Image. May increase generation time by ~5 seconds.

Section: Video Integration
  Toggle: Embed YouTube videos in lessons  [default ON]

Section: Doom Scroll Quality
  ○  Gemini 2.5 Flash   [FAST — recommended]
  ○  Gemini 2.5 Pro     [RICHER]

Footer: Preview models may occasionally be unavailable.
        The app falls back to Gemini 2.5 Flash automatically.
```

### 6.3 Edge Functions

| Endpoint | Description |
|---|---|
| `/generate-lesson` | POST. Checks Postgres for existing lesson. Calls Gemini → image API → YouTube API → Claude. Stores full lesson. Returns complete object. |
| `/generate-quiz` | POST (internal). Accepts lesson JSON. Calls Claude Sonnet 4.6. Returns quiz array. |
| `/generate-weekly-quiz` | POST. Fetches last 7 lessons. Calls Claude with all content. Returns 7-question cross-topic quiz. |
| `/send-weekly-digest` | POST (cron-triggered). Runs every Sunday 7 PM UTC. Fetches week's data. Renders HTML email. Calls Resend API. |
| `/send-daily-nudge` | POST (cron-triggered). Runs every morning 8 AM UTC. Opt-in users only. |

### 6.4 Fallback & Retry Strategy

| Scenario | Behaviour |
|---|---|
| Primary lesson model unavailable | Retry 3× with exponential backoff (1s, 2s, 4s). Fall back to `gemini-2.5-flash`. User sees: "Using fast mode today." |
| Image generation failure | Lesson renders without image block. No error shown — graceful degradation. |
| Claude quiz failure | Retry 3×. If all fail, lesson delivered without quiz. User sees: "Quiz unavailable today — try again later." |
| YouTube API quota exhausted | Video block omitted. Lesson renders without it. Zero user-visible error. |
| Offline at generation time | No Edge Function call. Yesterday's lesson shown with "No new lesson yet" state. |

---

## 7. References & Source Authentication

### 7.1 Why This Matters

A learning app that cannot prove its sources is just a trivia app. References transform Curiosity Engine from "interesting" to "trustworthy". They also guard against AI hallucination — Gemini is prompted to only cite verifiable sources, and the app surfaces those citations visibly.

### 7.2 Source Hierarchy

Gemini is prompted to prefer sources in this order:

1. **Primary academic** — peer-reviewed papers (PubMed, JSTOR, arXiv, Google Scholar)
2. **Authoritative institutions** — WHO, NHS, NASA, CERN, national science academies
3. **Quality encyclopaedias** — Wikipedia (well-cited articles), Britannica
4. **Major textbooks** — cited by title, author, edition
5. **Quality journalism** — The Atlantic, Nature News, Scientific American, BBC Science
6. **Official government / regulatory** — for law, economics, health policy topics

> **Never:** blogs, social media, opinion pieces, or sources without editorial oversight.

### 7.3 References UI Block

- **Collapsed by default.** Header: "📚 Sources & Further Reading" with chevron.
- Tap to expand — smooth `AnimatedVisibility` reveal.
- Each reference row: source type icon · title · author + year · one-line description · "Open →" chip
- **Confidence badges:** `Verified` (green) = has URL or DOI. `Cited` (amber) = no direct link.
- Amber-bordered reference: "This source was cited by the AI — we recommend verifying it independently."
- URLs open in **Chrome Custom Tab** (in-app browser). Back returns to lesson.

### 7.4 Hallucination Guard

The Edge Function system prompt instructs Gemini:

> *"Only cite sources you are highly confident are real and accurately describe. If you cannot verify a specific paper or URL, cite the institution or general body of work instead. Do not fabricate DOIs, URLs, or author names. If uncertain, omit."*

Post-generation validation:
- DOI format regex check
- URL domain whitelist against approved source domains
- References with no URL and no DOI labelled `Cited` not `Verified`

### 7.5 Feature Table

| ID | Feature | Priority |
|---|---|---|
| R-01 | References array in lesson JSON (2–6 per lesson, validated) | P0 |
| R-02 | Collapsible references UI block with type icons and confidence badges | P0 |
| R-03 | Chrome Custom Tab for URL sources | P0 |
| R-04 | Verified vs Cited confidence badges | P1 |
| R-05 | Further Reading accessible from Journal for past lessons | P1 |
| R-06 | Hallucination guard prompting + domain validation in Edge Function | P0 |

---

## 8. Notification System

### 8.1 Philosophy

Every notification must earn its place. A notification fires only when the user would *thank* you for it — not resent you. No daily "don't forget to learn!" spam.

### 8.2 Notification Types

#### Daily Lesson Ready (`LESSON_READY`)
| Field | Value |
|---|---|
| Trigger | Lesson pre-fetched and ready (~6 AM) |
| Title | Dynamic: e.g. "The Milgram Obedience Experiments 🧠" |
| Body | Category + estimated read time: "Psychology · ~7 min read" |
| Tap | Deep link to LessonScreen |
| Timing | User-configured reminder time (default 8:00 AM) |
| Frequency | Once per day. Does not fire if daily target already met. |

#### Streak At Risk (`STREAK_RISK`)
| Field | Value |
|---|---|
| Trigger | 8:00 PM and daily target not yet met |
| Title | "🔥 14-day streak at risk" |
| Body | "You haven't read today's lesson yet. 4 hours left to keep your streak alive." |
| Tap | Deep link to LessonScreen |
| Timing | 8:00 PM local — hard-coded |
| Suppress | Does not fire if streak = 0 or if Android DND is active |

#### Weekly Review Ready (`WEEKLY_REVIEW`)
| Field | Value |
|---|---|
| Trigger | Sunday 6:00 PM, if user completed ≥3 lessons that week |
| Title | "Your Weekly Review is ready 📋" |
| Body | "Test your retention across this week's topics. ~5 minutes." |
| Tap | Deep link to WeeklyQuizScreen |

#### Streak Milestone (`MILESTONE`)
| Field | Value |
|---|---|
| Trigger | Chain streak hits 7, 14, 30, 60, or 100 days |
| Title | "🎉 30-day streak! You're unstoppable." |
| Style | BigPictureStyle with generated milestone card image |
| Tap | Opens ProfileScreen |

#### Keep Learning — Doom Scroll Invite (`KEEP_LEARNING`)
| Field | Value |
|---|---|
| Trigger | Daily target met, app unused for 3+ hours |
| Title | "More to explore today 🌍" |
| Channel | **Default OFF — opt-in only** |
| Tap | Opens app in Doom Scroll mode |

### 8.3 Notification Channels

| Channel | Importance | Default |
|---|---|---|
| `LESSON_READY` | High | ON |
| `STREAK_RISK` | High | ON |
| `WEEKLY_REVIEW` | Default | ON |
| `MILESTONE` | Default | ON |
| `KEEP_LEARNING` | Low | **OFF** |

### 8.4 Implementation Notes

- **WorkManager** `PeriodicWorkRequest` fires at 6 AM to pre-fetch + schedule `LESSON_READY`
- **AlarmManager** (exact alarm): `STREAK_RISK` fires at 8 PM only if lesson not completed — checked via Room DB
- `NotificationCompat.Builder` for all — supports Android 7 through 15+
- Channels created on first launch in `Application.onCreate()`
- Deep links via `NavDeepLinkBuilder` for correct back stack
- Requests `SCHEDULE_EXACT_ALARM` permission (Android 12+)
- Respects DND via `NotificationManager.getCurrentInterruptionFilter()`

---

## 9. Dual Streak System

### 9.1 Two Distinct Metrics

| Metric | Description |
|---|---|
| **Chain Streak (All-Time)** | Consecutive calendar days with daily target met. Breaks on any missed day. Stores all-time best. |
| **Weekly Streak** | Count of days within the current Mon–Sun week where the daily target was met. Resets every Monday. Max 7. |

Both streak types gate on the **daily target** (configurable 1–10), not raw lesson count.

### 9.2 Chain Streak Rules

- Increments at midnight local time if daily target was met
- Breaks immediately on any missed day — no forgiveness mechanic
- "Best Streak" is all-time high watermark, updated whenever chain surpasses it
- Stored in both Supabase (source of truth) and Room DB

### 9.3 Weekly Streak Rules

- Counts Mon–Sun by default (configurable to Sun–Sat in Settings)
- Displayed as "5/7 this week" or as a 7-dot row
- Resets automatically every Monday at midnight
- Shown on home screen and included in weekly email digest

### 9.4 Streak UI

| Location | Display |
|---|---|
| Home Screen | Large chain streak badge (🔥 + number). 7-dot weekly grid below. |
| Widget | Fire emoji + streak number always visible. |
| Profile Screen | Chain streak, best streak, weekly grid, 30-day heatmap calendar. |
| 30-Day Heatmap | Calendar grid: gold = target met, grey = partial, empty = missed. |
| Quiz Results | Animate streak increment on completion. Confetti if milestone. |

### 9.5 Feature Table

| ID | Feature | Priority |
|---|---|---|
| ST-01 | Chain Streak (all-time consecutive) | P0 |
| ST-02 | Best Streak all-time record | P0 |
| ST-03 | Weekly Streak counter (Mon–Sun) | P0 |
| ST-04 | 7-dot weekly day grid on home screen | P1 |
| ST-05 | 30-day heatmap on profile | P1 |
| ST-06 | Week start day config (Mon or Sun) | P2 |

---

## 10. YouTube Video Integration

### 10.1 Philosophy

Video appears in lessons where it genuinely deepens understanding — not as filler. A video of Milgram's original experiment footage is more impactful than any description of it. Videos are curated, contextually placed, and short.

### 10.2 How Video Is Sourced

**Step 1 — Gemini suggests search terms in lesson JSON:**
```json
{
  "youtube_query": "Milgram obedience experiment original footage 1962",
  "video_intent": "Show the original experiment setup and subject reactions",
  "video_suitable": true
}
```

**Step 2 — Edge Function queries YouTube Data API v3:**
- Endpoint: `/search?part=snippet&q={query}&type=video&videoDuration=short|medium&maxResults=5`
- Filter: `safeSearch=strict`
- Rank by relevance + `viewCount`. Pick top result above 50K views.

**Step 3 — Store:** `video_id`, title, channel, duration in lesson record.

**Step 4 — Fallback:** If no result with >50K views, video block is omitted.

### 10.3 Video Block UI

- **Thumbnail:** 16:9 ratio via Coil. Play button overlay.
- **Below thumbnail:** Video title (Lato Medium 14sp) · channel name (Lato 12sp) · duration badge
- **Tap:** YouTube player opens in BottomSheet (80% screen height). Back closes it.
- **Auto-play:** OFF. User taps play intentionally.
- **Watched tracking:** >80% viewed = mark as watched in Room DB.
- **Fallback:** Block degrades to "Search on YouTube →" chip if API fails.

### 10.4 API & Cost

| Item | Detail |
|---|---|
| YouTube Data API v3 | Free tier: 10,000 units/day. One `/search` = 100 units. ~100 units/day for personal use. |
| API key | Stored in Supabase Vault. Called only from Edge Function. |
| Caching | `video_id` stored in lessons table. No API call on subsequent lesson views. |
| Quota guard | If quota exhausted, video block omitted silently. |

### 10.5 Feature Table

| ID | Feature | Priority | Complexity |
|---|---|---|---|
| V-01 | Video block type — thumbnail card with BottomSheet player | P1 | High |
| V-02 | YouTube Data API v3 integration in Edge Function | P1 | High |
| V-03 | In-app YouTube player (BottomSheet) | P1 | High |
| V-04 | Video watched tracking (>80%) | P2 | Medium |
| V-05 | Graceful fallback to search chip | P0 | Low |
| V-06 | Video toggle in Settings | P1 | Low |

---

## 11. Daily Learning Targets & Doom Scroll

### 11.1 Two Modes

| Mode | Description |
|---|---|
| **Intentional Mode** | User commits to a daily target. Completing it builds the streak. This is the habit core. |
| **Doom Scroll Mode** | Once target hit, the app shifts gear. Lessons generate on demand, infinitely. Productive doom scrolling. |

### 11.2 Configuring the Daily Target

- **Location:** Settings → Learning → Daily Learning Goal
- **Options:** 1 (default) · 2 · 3 · 5 · 10 · Custom (max 20)
- **UI:** Segmented control for presets. "Custom" opens number picker.
- **Streak impact:** Both chain and weekly streak gate on this target.
- **Widget:** Label changes to "0/3 today" when target is 3.

### 11.3 Doom Scroll Flow

| # | Actor | Action | Detail |
|---|---|---|---|
| 1 | User | Completes daily target | Both lessons read, both quizzes taken. |
| 2 | App | Target met celebration | Confetti. Badge: "✓ Daily goal complete — 2/2" |
| 3 | App | "Keep Exploring" CTA | Sticky card slides up: "Your curiosity doesn't have to stop. Keep going →" |
| 4 | User | Taps Keep Exploring | Transitions to Doom Scroll feed. |
| 5 | App | Generate bonus lesson | Gemini 2.5 Flash (fast model). Pre-generated in background = near-instant. |
| 6 | User | Reads, optionally quizzes | Quiz offered but not required. Scores still tracked. |
| 7 | App | "Next →" button | Loads next pre-generated lesson. Infinite loop. |
| 8 | App | Category breadth nudge | After 3 bonus lessons in same category: "Try History?" |
| 9 | User | Closes app | Session ends. All bonus lessons logged in journal with "bonus" badge. |

### 11.4 Performance Requirement

Doom scroll only works if the next lesson feels instant. Target: under 8 seconds for first render.

**Strategy:** When user taps "Keep Exploring", immediately trigger generation of Lesson N+1 in background. By the time user finishes reading the current lesson, N+1 is ready. Target: 0 seconds perceived wait.

Model for doom scroll: `gemini-2.5-flash` by default. Configurable to Pro in settings.

### 11.5 Bonus Lesson Rules

- Bonus lessons **do NOT** count toward chain streak or weekly streak
- Bonus lessons **ARE** stored in journal with a "bonus" badge
- Quiz is optional — offered, not required
- Bonus quiz scores ARE tracked and included in weekly email's "bonus explorations" section
- Bonus lessons never duplicate the day's target lessons

### 11.6 Feature Table

| ID | Feature | Priority | Complexity |
|---|---|---|---|
| DT-01 | Configurable daily target (1–20) | P0 | Low |
| DT-02 | Target progress on home screen (X/Y, progress ring) | P0 | Low |
| DT-03 | "Keep Exploring" CTA after target met | P0 | Low |
| DT-04 | Doom Scroll feed with background pre-generation | P0 | High |
| DT-05 | Optional quiz in doom scroll | P1 | Low |
| DT-06 | Bonus lessons journaled with badge | P1 | Low |
| DT-07 | Doom scroll model config (Flash / Pro) | P2 | Low |
| DT-08 | Category breadth nudge after 3 same-category bonus | P2 | Low |
| DT-09 | Widget shows X/Y target progress | P1 | Medium |

---

## 12. Sharing System

### 12.1 What Can Be Shared

| Share Type | Content |
|---|---|
| **Lesson Card** | Topic title, category, emoji, one-sentence hook, app name. Curiosity bait. |
| **Quiz Score Card** | Score as fraction, lesson topic, verdict, single takeaway. "5/5 on The Milgram Experiments 🧠" |
| **Streak Milestone Card** | Large streak number, fire emoji, punchy line. "30 days. One topic at a time." |
| **Weekly Stats Card** | Mini infographic: topics count, retention %, streak. |
| **Key Takeaway Quote** | Single takeaway as quote card: "Today I learned that..." format. |

### 12.2 Share Card Visual Spec

Generated via **Android Canvas API**:

```
Canvas: 1080×1080px (optimal for WhatsApp, Instagram, iMessage)
Background: deep navy gradient (#0F1E35 → #1E3A5F) + subtle noise texture
Brand bar: 4px gold line at top edge
Topic emoji: 80px centred in top third
Title: DM Serif Display 42sp white, centred, max 2 lines
Subtitle: category chip + read time in amber
Content: one key insight in Lato 24sp, italic
Bottom: "Curiosity Engine" wordmark in small gold type

Score cards: score fraction at 96sp DM Serif · verdict below
Streak cards: flame emoji 96sp · streak number 80sp DM Serif
```

### 12.3 Share Flow

| # | Actor | Action | Detail |
|---|---|---|---|
| 1 | User | Taps share icon | Top-right of lesson, quiz results, or profile |
| 2 | App | Bottom sheet | 3–4 card type options with visual previews |
| 3 | User | Selects type | Lesson Card / Quiz Score / Takeaway / Streak (if milestone) |
| 4 | App | Canvas renders | Share image generated in memory (~200ms) |
| 5 | App | Android share sheet | `Intent(ACTION_SEND)` with `image/png` · FileProvider for temp file |
| 6 | User | Picks target app | WhatsApp, Instagram, iMessage, copy, save to gallery — all standard |

### 12.4 Share Card Deep Link

Every share includes message text:

- Lesson: *"Today I learned about {topic} on Curiosity Engine. What are you curious about? → curiosityengine.app"*
- Score: *"Scored {score}/{total} on {topic}. Your turn: curiosityengine.app"*
- Streak: *"{n} days of daily learning. One topic at a time. curiosityengine.app"*

### 12.5 Share Placement

| Screen | Share | Card Type |
|---|---|---|
| Lesson screen | Share icon (top-right app bar) | Lesson Card |
| Quiz results | "Share your score" button | Quiz Score Card |
| Profile screen | Share icon next to streak | Streak or Weekly Stats |
| Journal (long-press) | Context menu | Lesson Card for that date |
| Key Takeaways block | Share icon per row | Takeaway Quote Card |

### 12.6 Feature Table

| ID | Feature | Priority | Complexity |
|---|---|---|---|
| SH-01 | Lesson share card (Canvas 1080×1080) | P1 | Medium |
| SH-02 | Quiz score share card | P1 | Medium |
| SH-03 | Streak milestone share card | P2 | Medium |
| SH-04 | Weekly stats share card | P2 | Medium |
| SH-05 | Takeaway quote card | P2 | Low |
| SH-06 | Share bottom sheet with card preview | P1 | Low |
| SH-07 | Android share sheet integration (ACTION_SEND) | P0 | Low |

---

## 13. Design Language Specification

### 13.1 Design Philosophy

**Four Pillars:**

1. **EDITORIAL** — The app should feel like a beautifully designed magazine. Content is king. Every UI element exists to serve the reading experience.
2. **TACTILE** — Every interaction should feel physical. Cards lift. Pages turn. Motion communicates state, not decoration.
3. **CALM** — No aggressive colours, no anxious animations, no cluttered screens. Dark theme default with generous negative space.
4. **TRUSTWORTHY** — This is a knowledge app. Serif type for headings. Structured layouts. References visible.

### 13.2 Typography System

| Role | Typeface | Size | Weight |
|---|---|---|---|
| Display / Lesson Title | DM Serif Display | 32–40sp | Regular |
| Section Headings | DM Serif Display | 20–24sp | Regular |
| Body / Reading Text | Lato | 16sp | Regular |
| UI Labels & Navigation | Lato | 13–15sp | Medium |
| Captions & Metadata | Lato | 12–13sp | Light Italic |
| Flashcard Terms | DM Serif Display | 22sp | Regular |
| Stats & Numbers | Lato | 28–48sp | Bold |
| Quote Spotlight Text | DM Serif Display | 20–26sp | Italic |
| Monospace (model names in Settings) | JetBrains Mono | 13sp | Regular |

**Typography Rules:**
- Line height: 1.75 for body · 1.2 for display headings · 1.5 for UI labels
- Letter spacing: -0.5px for display · +0.5px for small caps / metadata
- Max line width: 660dp (no text stretches full screen on tablets)
- Support Android font scaling up to 150% without layout breakage
- Minimum touch target: 48dp × 48dp

### 13.3 Colour System

**Core Palette:**

| Token | Hex | Use |
|---|---|---|
| Background | `#0F1E35` | All screen backgrounds |
| Surface L1 | `#1E3A5F` | Cards, lesson blocks |
| Surface L2 | `#253F60` | Modals, bottom sheets |
| Surface L3 | `#2A4A72` | Pressed state |
| Brand Gold | `#C8932A` | Primary accent, brand |
| Amber | `#E8A020` | Secondary accent, CTAs |
| Text Primary | `#E8E4DB` | Body text |
| Text Secondary | `#8899AA` | Metadata, labels |
| Text Muted | `#4A5568` | Disabled, placeholder |
| Error | `#E05252` | Errors, wrong answers |
| Success | `#4AC58A` | Correct answers, completion |

**Category Colours:**

| Category | Hex | Rationale |
|---|---|---|
| Science | `#1A7A6E` | Teal-green — labs, biology |
| Nature | `#2D6B2A` | Forest green — earthy |
| History | `#8B5A1C` | Sepia-amber — aged parchment |
| Psychology | `#6B2A8B` | Violet — depth, mystery |
| Technology | `#1A4A8B` | Electric blue — precision |
| Art & Culture | `#8B1A4A` | Crimson — expressive |
| Economics | `#2A6B4A` | Malachite — growth, money |
| Philosophy | `#4A4A8B` | Slate indigo — contemplative |
| Surprise Me | `#8B4A1A` | Burnt sienna — unpredictable |

### 13.4 Elevation & Surface System

Elevation is expressed through background lightness — darker screens, lighter cards. No drop shadows.

| Level | Colour | Used For |
|---|---|---|
| 0 — Background | `#0F1E35` | All screen bases |
| 1 — Cards | `#1E3A5F` | Lesson cards, quiz containers |
| 2 — Modals | `#253F60` | Bottom sheets, dialogs |
| 3 — Pressed | `#2A4A72` | On-press feedback |
| Glow Accents | Brand gold @ 15% opacity | Featured card rim light |

### 13.5 Spacing & Layout Grid

| Token | Value |
|---|---|
| Base unit | 4dp |
| Screen margin | 20dp left/right |
| Card padding | 20dp all sides |
| Section gap (between lesson blocks) | 24dp |
| Component gap (within a component) | 12dp |
| Corner radius — Cards | 16dp |
| Corner radius — Chips / badges | 12dp |
| Corner radius — Buttons | 8dp |
| Corner radius — Bottom sheets | 24dp (top corners) |
| Content max-width | 660dp |

### 13.6 Motion & Animation

| Token | Value |
|---|---|
| Easing — standard | `FastOutSlowIn` |
| Easing — modal enter | `EmphasizedDecelerate` |
| Duration — UI transitions | 280ms |
| Duration — micro-interactions | 180ms |
| Duration — screen transitions | 380ms |
| Card press | Scale 0.97 + elevation increase — 120ms |
| Lesson block entrance | Fade in + slide up 12dp · 60ms stagger between blocks |
| Flashcard flip | 3D Y-axis rotation via `graphicsLayer` · 280ms |
| Quiz option feedback | Background colour transition · 180ms |
| Streak milestone | Confetti Canvas particle system · 1200ms · non-blocking |
| Page transitions | Predictive Back API (Android 14+) + SharedElement |

### 13.7 Component Standards

| Component | Spec |
|---|---|
| Lesson Block Card | `Surface(tonalElevation=1.dp)` · 16dp corners · 20dp padding · Category colour top bar 3dp |
| Flashcard | `ElevatedCard` + HorizontalPager · 3D flip on tap · 5:3 aspect ratio |
| Quiz Option | `OutlinedButton` at rest · Filled green/red on reveal · Full width · 56dp min height |
| Progress Indicator | Custom dot-row — filled = completed, outline = remaining |
| Streak Badge | Chip · gold background · Lato Bold 14sp · 36dp height |
| Category Chip | `FilterChip` · category colour fill · DM Serif 14sp · 32dp height |
| Bottom Sheet | `ModalBottomSheet` · 24dp top corners · Drag handle · 60% scrim |
| Share Bottom Sheet | Custom — shows card preview above Android share sheet |

### 13.8 Home Screen Widget Design

| Element | Spec |
|---|---|
| Background | `#0F1E35` with 16dp corner radius |
| 4×2 Layout | Left: emoji (28sp) + topic title (DM Serif 15sp, max 2 lines) + category chip · Right: streak badge + target progress (X/Y) + read state dot |
| 2×2 Layout | Centre: topic emoji (32sp) · Bottom: streak + read dot |
| Unread state | Gold dot + "Tap to read today's lesson" in Lato 11sp |
| Read state | Gold checkmark → animate to green · "Lesson read ✓" |
| Quiz done | Green double-tick · "Lesson + quiz complete ✓" |
| Loading | Shimmer placeholder while lesson pre-fetches |

### 13.9 Accessibility Standards

| Standard | Target |
|---|---|
| Contrast ratio | WCAG AA minimum (4.5:1 body, 3:1 large text). AAA on primary content. |
| Touch targets | 48dp × 48dp minimum for all interactive elements |
| TalkBack | All interactive elements have `contentDescription`. Logical reading order. |
| Font scaling | Usable and non-overlapping at 150% system font size |
| Reduced motion | Honour "Remove animations" system setting — replace with instant cuts |
| Colour independence | Never use colour alone to convey meaning — always add icon or text label |

---

## 14. Android App — Screen Inventory

### 14.1 Screen Map

| Screen | Description |
|---|---|
| Splash / Auth | App launch. Check auth state. If signed in → Home. If not → Onboarding. |
| Onboarding (4 steps) | 1. Value prop · 2. Category selection · 3. Daily target · 4. Notification time + widget prompt |
| Home Screen | Target progress ring (X/Y). Lesson card. Streak badge. 7-dot weekly grid. Weekly review CTA (if available). |
| Lesson Screen | Full block renderer. Scroll-based reading progress. References block (collapsed). Sticky quiz CTA. |
| Quiz Screen | Full-screen immersive quiz. One question per screen. Progress dots. Explanation reveal after each answer. |
| Quiz Results Screen | Score display. Verdict. Wrong question review. Share button. Return to lesson / home. |
| Weekly Review Screen | Cross-topic quiz. Topic attribution per question. Results with retention breakdown. |
| Doom Scroll Feed | Post-target infinite lesson feed. "Next →" sticky button. Optional quiz. Category breadth nudge. |
| Journal Screen | Chronological lesson list. Filter by category / bonus. Tap to re-read. Shows score, completion, bonus badge. |
| Profile / Settings Screen | Chain + weekly streak. Best streak. 30-day heatmap. Category prefs. Notification time. Email prefs. |
| Settings — AI Models | Gemini model selector. Image toggle. Video toggle. Claude info row. Doom scroll model. |
| Settings — Notifications | Per-channel toggles. Reminder time picker. |

### 14.2 Navigation Structure

Bottom navigation bar: **3 tabs** — Today (home) · Journal · Profile

- Lesson, Quiz, Results, Doom Scroll: pushed modally on top of Today
- Weekly Review: pushed modally from Home or via deep link
- Settings: pushed from Profile

### 14.3 Deep Link Scheme

| Deep Link | Trigger |
|---|---|
| `curiosityengine://lesson/today` | Widget tap, notification tap |
| `curiosityengine://lesson/{date}` | Email missed-day CTA |
| `curiosityengine://quiz/weekly` | Sunday email CTA |
| `curiosityengine://doom-scroll` | Keep Learning notification |
| `curiosityengine://journal` | Widget long-press shortcut |

---

## 15. Data Models

### 15.1 Supabase — users Table

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | Matches Supabase Auth user ID |
| `email` | TEXT | From Google Auth |
| `display_name` | TEXT | |
| `avatar_url` | TEXT | |
| `preferred_categories` | TEXT[] | Array of category IDs |
| `daily_target` | INTEGER DEFAULT 1 | Lessons per day for streak |
| `reminder_time` | TIME | Daily notification time (local) |
| `week_start_day` | INTEGER DEFAULT 1 | 0=Sunday, 1=Monday |
| `gemini_model` | TEXT DEFAULT 'gemini-2.5-pro' | Selected lesson model |
| `doom_scroll_model` | TEXT DEFAULT 'gemini-2.5-flash' | Model for bonus lessons |
| `images_enabled` | BOOLEAN DEFAULT true | AI image blocks |
| `video_enabled` | BOOLEAN DEFAULT true | YouTube video blocks |
| `email_weekly_digest` | BOOLEAN DEFAULT true | |
| `email_daily_nudge` | BOOLEAN DEFAULT false | Opt-in |
| `notif_streak_risk` | BOOLEAN DEFAULT true | 8 PM streak notification |
| `notif_weekly_review` | BOOLEAN DEFAULT true | Sunday notification |
| `notif_keep_learning` | BOOLEAN DEFAULT false | Doom scroll invite |
| `timezone` | TEXT | IANA timezone string |
| `created_at` | TIMESTAMPTZ | |

### 15.2 Supabase — lessons Table

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `user_id` | UUID (FK → users) | |
| `date` | DATE | Calendar date |
| `topic` | TEXT | |
| `category` | TEXT | |
| `emoji` | TEXT | |
| `content_json` | JSONB | Full blocks array |
| `quiz_json` | JSONB | 5 quiz questions |
| `references_json` | JSONB | Array of reference objects |
| `youtube_video_id` | TEXT | If video block exists |
| `youtube_title` | TEXT | |
| `youtube_channel` | TEXT | |
| `youtube_duration_s` | INTEGER | |
| `video_watched` | BOOLEAN DEFAULT false | User watched >80% |
| `is_bonus` | BOOLEAN DEFAULT false | Doom scroll lesson |
| `daily_position` | INTEGER | 1=first lesson, 2=second, etc. |
| `generated_by` | TEXT | Model used (auditability) |
| `share_count` | INTEGER DEFAULT 0 | |
| `created_at` | TIMESTAMPTZ | |

### 15.3 Supabase — quiz_results Table

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `user_id` | UUID (FK → users) | |
| `lesson_id` | UUID (FK → lessons) | |
| `quiz_type` | TEXT | `daily` or `weekly` |
| `score` | INTEGER | |
| `total` | INTEGER | |
| `answers_json` | JSONB | Array of `{ questionIndex, selectedIndex, correct }` |
| `completed_at` | TIMESTAMPTZ | |

### 15.4 Supabase — streaks Table

| Column | Type | Notes |
|---|---|---|
| `user_id` | UUID (PK, FK → users) | |
| `chain_streak` | INTEGER | Current consecutive days |
| `best_streak` | INTEGER | All-time high |
| `last_completed_date` | DATE | |
| `weekly_streak` | INTEGER | Days completed this Mon–Sun |
| `weekly_start_date` | DATE | Monday of current week |
| `week_days_done` | BOOLEAN[7] | Mon=0, Sun=6 |
| `updated_at` | TIMESTAMPTZ | |

### 15.5 Supabase — share_events Table

| Column | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `user_id` | UUID (FK → users) | |
| `lesson_id` | UUID (FK → lessons, nullable) | |
| `share_type` | TEXT | `lesson` \| `quiz_score` \| `streak` \| `weekly_stats` \| `takeaway` |
| `shared_at` | TIMESTAMPTZ | |

### 15.6 Local Room DB — Android

| Entity | Columns |
|---|---|
| `LessonEntity` | lesson_id, date, topic, category, emoji, content_json, quiz_json, references_json, youtube_video_id, is_bonus, is_read, quiz_score, synced_at |
| `StreakEntity` | chain_streak, best_streak, last_completed_date, weekly_streak, week_days_done |
| `UserPrefsEntity` | preferred_categories, daily_target, reminder_time, email_prefs, model_prefs (local cache) |

---

## 16. Tech Stack

### 16.1 Android

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material Design 3 |
| Widget | Jetpack Glance API (Compose-based) |
| Navigation | Jetpack Navigation Compose + Predictive Back API |
| Local DB | Room (with KSP) |
| Background Tasks | WorkManager |
| Network | Ktor Client |
| JSON | kotlinx.serialization |
| DI | Hilt |
| Auth / Sync | Supabase Kotlin SDK |
| Async | Kotlin Coroutines + Flow |
| Image Loading | Coil 3 (async + skeleton shimmer) |
| Video | YouTube Android Player API v2 or IFrame WebView |
| Sharing | Canvas API for card generation · `ACTION_SEND` Intent |
| Typography | DM Serif Display + Lato via Google Fonts downloadable font API |
| Animations | Compose `animate*AsState` · `graphicsLayer` · Predictive Back · SharedElement |
| Min SDK | API 26 (Android 8.0) |

### 16.2 Backend (Supabase)

| Component | Detail |
|---|---|
| Database | Supabase Postgres — managed, free tier sufficient |
| Auth | Supabase Auth with Google OAuth provider |
| Edge Functions | Deno TypeScript — AI orchestration, YouTube API, email |
| Realtime | Supabase Realtime for cross-device lesson/streak sync |
| Storage | Supabase Storage — AI-generated lesson images |
| Email | Resend API via Edge Function — free tier: 3,000 emails/month |
| Cron Jobs | Supabase `pg_cron` for weekly digest and daily nudge |

### 16.3 External APIs

| API | Use | Cost |
|---|---|---|
| Gemini API (`gemini-2.5-pro`) | Lesson generation | $1.25/$10.00 per 1M tokens |
| Gemini API (`gemini-2.5-flash-image`) | Lesson image generation | ~$0.039/image |
| Claude API (`claude-sonnet-4-6`) | Quiz generation | Standard Anthropic pricing |
| YouTube Data API v3 | Video search & metadata | Free (10K units/day) |
| Resend | Email delivery | Free (3K emails/month) |

---

## 17. Email Specification

### 17.1 Weekly Digest — Content Structure

**Delivery:** Every Sunday at 7 PM in the user's local timezone.

1. Header: "Your week in learning, Samuel" — personalised greeting
2. Stats bar: Lessons read · Quizzes completed · Avg score · Current streak
3. Lesson list: Each lesson as a card with emoji, topic, date, quiz score (or "Not taken")
4. Missed days: Highlighted in amber. CTA: "Read it now →"
5. Bonus lessons section: "You also explored X bonus topics this week"
6. Weekly Review CTA: "Test your retention: Take this week's quiz →"
7. Encouragement: "5-lesson week. That's 5 more things you know that you didn't on Monday."
8. Footer: Unsubscribe · Notification preferences link

### 17.2 Subject Line Variants

- `"Your week in learning — 5 topics, 87% retention 🔥"` (high engagement)
- `"You missed 2 days last week — the lessons are still waiting 📚"` (missed days)
- `"14-day streak! Your weekly review is ready 🎯"` (milestone week)
- `"Slow week? 3 lessons are waiting to catch up on"` (low engagement)

### 17.3 Daily Nudge Email (Opt-in)

- Delivery: Every morning at user's chosen reminder time. Opt-in only.
- Subject: "Today: The Milgram Obedience Experiments 🧠"
- Body: Topic, category, one-line hook, single CTA button: "Start learning →"
- No images, fast to load, renders in Gmail mobile.

---

## 18. Non-Functional Requirements

| ID | Requirement | Target | Priority |
|---|---|---|---|
| NF-01 | Lesson Load Time | Pre-fetched lesson available within 500ms of app open | P0 |
| NF-02 | Doom Scroll Next Lesson | Next bonus lesson ready within 8 seconds of tap | P0 |
| NF-03 | Offline Mode | Today's lesson + last 7 days fully readable offline. Quiz completable offline. | P1 |
| NF-04 | Widget Refresh | Reflects lesson state changes within 60 seconds | P1 |
| NF-05 | API Key Security | Gemini, Claude, YouTube keys never on device. All AI calls via Edge Functions. | P0 |
| NF-06 | Battery Impact | WorkManager pre-fetch uses constraints (charging or unmetered network) | P1 |
| NF-07 | Cold Start Time | App cold start to home screen in under 2 seconds on mid-range Android | P1 |
| NF-08 | Min Android Version | API 26 (Android 8.0) minimum. Glance requires API 26+. | P0 |
| NF-09 | Font Scaling | UI usable at 150% system font size without layout breakage | P1 |
| NF-10 | Share Card Generation | 1080×1080px Canvas image generated in under 300ms | P1 |

---

## 19. Development Roadmap

### Phase 1 — Core Foundation (Weeks 1–3)

**Goal:** A working app that generates a lesson, shows it beautifully, quizzes you, and tracks your streak.

- [ ] Supabase project setup: schema (all v3 fields), Edge Functions for lesson + quiz generation
- [ ] Android project: Kotlin + Compose + Hilt + Room + Supabase SDK
- [ ] DM Serif Display + Lato fonts via Google Fonts API
- [ ] Onboarding: 4-screen flow with Google Sign-In, category selection, daily target, time picker
- [ ] Lesson screen: full block renderer (text + quote + slide — no images/video yet)
- [ ] Daily quiz: Claude Sonnet 4.6 · 5 questions · explanation feedback · score persistence
- [ ] Chain streak tracking: local + Supabase
- [ ] Dark theme + full colour system
- [ ] Notification channels: `LESSON_READY` + `STREAK_RISK` registered and working
- [ ] References block: collapsible UI + Chrome Custom Tab

### Phase 2 — Rich Formats, Widget & Dual Streaks (Weeks 4–5)

**Goal:** The app looks and feels like a premium product. Widget on home screen.

- [ ] Image block: Gemini 2.5 Flash Image · Coil skeleton loading · Supabase Storage cache
- [ ] Flashcard deck: HorizontalPager + 3D flip animation
- [ ] Video block: YouTube Data API v3 integration · in-app BottomSheet player
- [ ] Glance widget: 4×2 + 2×2 variants · target progress · streak · deep links
- [ ] WorkManager: 6 AM lesson pre-fetch · notification scheduling
- [ ] Weekly streak: Mon–Sun counter · 7-dot home screen grid
- [ ] 30-day heatmap on profile
- [ ] Journal screen: history, completion status, quiz scores, bonus badge
- [ ] Cross-device sync via Supabase Realtime

### Phase 3 — Daily Targets, Doom Scroll & Sharing (Weeks 6–7)

**Goal:** The app becomes genuinely hard to put down.

- [ ] Daily target setting: configurable 1–20, home screen progress ring
- [ ] Doom Scroll mode: "Keep Exploring" CTA · infinite generation · background pre-fetch
- [ ] Settings → AI Models screen: full model selector + toggles
- [ ] Share cards: Canvas rendering for all 5 share types · Android share sheet
- [ ] Share bottom sheet with preview
- [ ] Notification: `WEEKLY_REVIEW` + `MILESTONE` + `KEEP_LEARNING` channels
- [ ] Streak milestone celebrations (confetti Canvas animation)
- [ ] Widget: target progress display (X/Y)

### Phase 4 — Email, Weekly Review & Polish (Week 8+)

**Goal:** The full loop is complete. The app is polished to production standard.

- [ ] Weekly digest email via Supabase pg_cron + Resend (with bonus lessons section)
- [ ] Weekly Review Quiz: Claude cross-topic generation + results screen
- [ ] Daily nudge email (opt-in)
- [ ] Missed day recovery flow
- [ ] Predictive Back gesture support (Android 14+)
- [ ] SharedElement transitions: lesson card → lesson screen
- [ ] Reduced motion accessibility mode
- [ ] WCAG AA contrast audit across all screens
- [ ] TalkBack audit and content descriptions
- [ ] Material You dynamic colour (Android 12+)
- [ ] Doom scroll model quality toggle
- [ ] Category breadth nudge logic

---

## Appendix — Feature Priority Summary

### P0 — Must Have at Launch

References in lessons · Daily lesson generation · Block-based lesson renderer · Claude quiz (5 questions) · Chain streak · Weekly streak · Glance widget (4×2) · Daily target config · Doom Scroll mode · LESSON_READY notification · STREAK_RISK notification · Chrome Custom Tab for sources · Android share sheet integration · API keys secured via Edge Functions · Offline lesson access

### P1 — High Value, Ship Soon

Image blocks · Flashcard blocks · Video blocks · Doom scroll background pre-generation · Weekly Review Quiz · Weekly digest email · Share card generation (lesson + quiz score) · Share bottom sheet · 7-dot weekly grid · Notification settings screen · Hallucination guard prompting · Widget target progress · Reduced motion support

### P2 — Nice to Have

2×2 small widget · Material You dynamic colour · Quiz retry · Category breadth nudge · Doom scroll model toggle · Week start day config · Streak milestone share card · Weekly stats share card · Takeaway quote share · Email preview in-app

---

*Curiosity Engine — Complete Product Requirements Document v3.0*
*Samuel Oladipupo · Personal Project · April 2026*
