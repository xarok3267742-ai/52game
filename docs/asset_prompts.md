# Asset Prompts

Фактический MVP использует собственные vector drawable ассеты. Ниже — подготовленные ImageGen prompts для дальнейшего улучшения store listing и иллюстраций без копирования чужих брендов.

## App Icon Concept
Prompt: `A clean mobile game app icon for an original offline color puzzle called "Color Quarter": a 4x4 city block mosaic made of teal, warm yellow, berry red, mint green, and violet tiles on a deep ink background, crisp flat vector-like shapes, soft rounded corners, no text, no logos, no characters, high contrast, Google Play ready.`

Google Play high-res icon refresh 2026-05-20 выполнен через built-in ImageGen и обработан до exact Play format `512x512` RGBA PNG with fully opaque alpha. Android launcher/adaptive vector assets остались нативными vector drawable.

Final prompt:

```text
Use case: logo-brand
Asset type: Google Play high-res app icon concept for an Android puzzle game
Primary request: Create a polished original app icon for the Russian-language offline puzzle game "Цветной Квартал". Square composition, no text, no letters, no logos, no characters.
Scene/backdrop: deep ink rounded-square app icon background with subtle warm off-white rim highlight, clean mobile store presentation.
Subject: centered 4x4 city-block mosaic puzzle mark, made of rounded square tiles in teal lagoon, warm yellow, berry red, mint green, and soft violet. The upper-left connected region should feel like a color flood puzzle expansion, with one small simple star reward accent near the mosaic but not covering tiles.
Style: crisp modern vector-like mobile game art, clean geometry, soft shadow inside the icon only, high contrast, readable at small sizes, Android-first, premium casual puzzle feel.
Constraints: no text, no watermark, no UI screenshot, no real maps, no buildings, no people, no copyrighted or branded elements, no transparent background, no thin outlines, no tiny details that disappear at 48px.
```

Saved outputs:
- `qa-artifacts/imagegen/app_icon_concept_2026-05-20.png` — generated source concept.
- `qa-artifacts/imagegen/app_icon_processed_512_2026-05-20.png` — exact processed concept.
- `play-assets/graphics/app_icon_512.png` — production Play high-res icon.

## Adaptive Icon Foreground
Prompt: `Centered abstract mosaic mark for Android adaptive icon foreground: four colorful city blocks forming a simple square puzzle symbol, teal, yellow, berry, violet, clean flat shapes, transparent background, no text, no copyrighted elements.`

## Splash Image Concept
Prompt: `Minimal splash mark for a calm color puzzle app: a square city-grid mosaic on warm off-white background, flat design, clean geometry, no text, no brand imitation, friendly Android mobile style.`

## Feature Graphic
Prompt: `Google Play feature graphic for an original Russian-language offline color puzzle: large colorful mosaic board on the right, calm off-white background, subtle city block pattern, title area left left empty for store text overlay, modern mobile game polish, no characters, no logos, no copied UI.`

Финальный refresh 2026-05-20 выполнен через built-in ImageGen и обработан до exact Play format `1024x500` RGB PNG.

Final prompt:

```text
Use case: ads-marketing
Asset type: Google Play feature graphic concept for an Android puzzle game
Primary request: Create a polished, original feature graphic concept for the Russian-language offline puzzle game "Цветной Квартал". Horizontal 1024x500 composition, no text, no logos, no characters.
Scene/backdrop: warm off-white mobile game background with a subtle flat city-block mosaic pattern, clean and bright.
Subject: a large colorful square city-block puzzle board, slightly angled but still flat and readable, made of rounded square tiles in teal lagoon, warm yellow, berry red, mint green, and soft violet; a few small star-shaped reward accents as simple geometric UI ornaments.
Style: crisp modern vector-like mobile game art, consistent flat shapes, Android-first, friendly but not childish, high contrast, no gradients, no photorealism.
Constraints: no copyrighted content, no brand imitation, no real maps, no text, no UI screenshots, no people, no watermark, no random stock illustration, leave safe empty breathing room on the left for store layout.
```

Saved outputs:
- `qa-artifacts/imagegen/feature_graphic_concept_2026-05-20.png` — generated source concept.
- `qa-artifacts/imagegen/feature_graphic_processed_1024x500_2026-05-20.png` — exact processed concept.
- `play-assets/graphics/feature_graphic.png` — production Play asset.

## Onboarding Illustrations
Prompt: `Three small onboarding illustrations for a color flood puzzle: 1 select a color swatch, 2 connected city blocks expand, 3 completed board with stars; flat vector-like style, same palette, no text, no copyrighted content.`

Финальный in-app onboarding refresh 2026-05-20 выполнен через built-in ImageGen и обработан до app-ready `840x500` RGB WebP. Иллюстрация заменяет программную mosaic-заглушку на первом запуске.

Final prompt:

```text
Use case: illustration-story
Asset type: In-app onboarding hero illustration for an Android puzzle game
Primary request: Create a polished onboarding illustration for the Russian-language offline puzzle game "Цветной Квартал". No text, no letters, no logos, no people, no characters.
Scene/backdrop: warm off-white mobile app card background with subtle city-block pattern, clean and bright.
Subject: a friendly 5x5 rounded-square color puzzle board. The upper-left connected region is teal lagoon and softly highlighted to show color-flood expansion; adjacent blocks use warm yellow, berry red, mint green, and soft violet. Add three small color swatches nearby and one simple star reward accent, all clearly secondary.
Style: crisp modern vector-like mobile game illustration, flat shapes with very soft depth, consistent with Google Play feature graphic, readable on a phone, Android-first, polished casual puzzle feel.
Composition: horizontal card-friendly composition, centered board, generous padding, no tiny details, no gradients dominating, no screenshots.
Constraints: no text, no watermark, no brand imitation, no real maps, no copyrighted elements, no UI labels, no transparent background.
```

Saved outputs:
- `qa-artifacts/imagegen/onboarding_illustration_concept_2026-05-20.png` — generated source concept.
- `qa-artifacts/imagegen/onboarding_illustration_processed_840x500_2026-05-20.webp` — exact processed app illustration.
- `app/src/main/res/drawable-nodpi/onboarding_illustration.webp` — production in-app onboarding asset.

## App Background Pattern
Финальный app-wide background refresh 2026-05-20 выполнен через built-in ImageGen и обработан до app-ready `720x1280` RGB WebP. Фон подключён декоративно в `ScreenFrame`, не имеет accessibility label и не меняет layout.

Final prompt:

```text
Use case: illustration-story
Asset type: Subtle full-screen app background image for an Android casual puzzle game
Primary request: Create a polished subtle background for the Russian-language offline color puzzle game "Цветной Квартал". No text, no letters, no numbers, no logos, no characters, no people.
Scene/backdrop: warm off-white paper-like mobile app background with an extremely light city-block mosaic pattern.
Subject: soft low-contrast rounded square tile outlines, tiny sparse color accents in teal lagoon, sun yellow, berry red, mint green, and soft violet; the accents should be very subtle and secondary so app text remains readable over it.
Style: crisp modern vector-like mobile game illustration, flat shapes, Android-first, premium casual puzzle feel, consistent with existing Color Quarter onboarding/home/result illustrations and Google Play graphics.
Composition: vertical mobile wallpaper composition, safe edges, seamless-feeling, no focal object, no UI labels, no screenshot, no large dark areas, no busy detail.
Color palette: mostly #F7F6F0 warm off-white and very pale linework, with tiny muted accents from the game palette.
Constraints: no text, no watermark, no brand imitation, no real maps, no copyrighted elements, no transparent background, no dark background, no gradients dominating, must preserve high readability for dark UI text and white cards placed above it.
```

Saved outputs:
- `qa-artifacts/imagegen/app_background_concept_2026-05-20.png` — generated source concept.
- `qa-artifacts/imagegen/app_background_processed_720x1280_2026-05-20.webp` — exact processed app background.
- `app/src/main/res/drawable-nodpi/app_background.webp` — production in-app background asset.

## Home Dashboard Illustration
Финальный in-app home refresh 2026-05-20 выполнен через built-in ImageGen и обработан до app-ready `720x405` RGB WebP. Иллюстрация добавлена на главный экран как компактный visual anchor между заголовком и прогрессом, чтобы home выглядел как законченный product surface, а не только список уровней.

Final prompt:

```text
Use case: illustration-story
Asset type: Compact in-game home dashboard illustration for an Android puzzle game
Primary request: Create a polished small home-screen illustration for the Russian-language offline color puzzle game "Цветной Квартал". No text, no letters, no numbers, no logos, no characters, no people.
Scene/backdrop: warm off-white rounded-card friendly background, subtle city-block pattern, clean and bright.
Subject: a cozy colorful neighborhood made of rounded square puzzle tiles arranged like city blocks. Include a small 4x4 puzzle board preview, a route-like connected teal lagoon region from upper-left, tiny warm yellow stars, and a few color swatches in berry red, mint green, sun yellow, and soft violet. It should communicate short relaxed puzzle sessions and visible progress.
Style: crisp modern vector-like mobile game illustration, flat shapes with soft depth, same visual world as the existing Color Quarter onboarding, victory/defeat illustrations, and Google Play feature graphic, Android-first, premium casual puzzle feel.
Composition: wide compact banner, centered board/neighborhood motif with generous padding, readable at small size near the top of a home dashboard, no tiny details, no screenshots, no UI labels.
Constraints: no text, no watermark, no brand imitation, no real maps, no copyrighted elements, no transparent background, no dark background.
```

Saved outputs:
- `qa-artifacts/imagegen/home_illustration_concept_2026-05-20.png` — generated source concept.
- `qa-artifacts/imagegen/home_illustration_processed_720x405_2026-05-20.webp` — exact processed app illustration.
- `app/src/main/res/drawable-nodpi/home_illustration.webp` — production in-app home dashboard asset.

## Level Screen Illustration
Финальный in-app level refresh 2026-05-21 выполнен через built-in ImageGen и обработан до app-ready `720x405` RGB WebP. Иллюстрация добавлена на экран уровня как компактный visual anchor над отдельными кнопками действий.

Final prompt:

```text
Use case: illustration-story
Asset type: Compact in-game level screen banner illustration for an Android puzzle game
Primary request: Create a polished small level-screen illustration for the Russian-language offline color puzzle game "Цветной Квартал". No text, no letters, no numbers, no logos, no characters, no people.
Scene/backdrop: warm off-white rounded-card friendly background, subtle city-block pattern, clean and bright.
Subject: a cozy colorful neighborhood puzzle board, viewed flat and slightly elevated, with the upper-left connected region glowing in teal lagoon and a clear route-like expansion path into neighboring tiles. Surrounding tiles use warm yellow, berry red, mint green, and soft violet. Add a few tiny star reward accents and color swatches as secondary details.
Style: crisp modern vector-like mobile game illustration, flat shapes with soft depth, same visual world as the existing Color Quarter onboarding, home, victory/defeat illustrations, and Google Play feature graphic, Android-first, premium casual puzzle feel.
Composition: wide compact banner, centered board/neighborhood motif with generous padding, readable at small 76dp display height inside a level screen, no tiny details, no screenshots, no UI labels.
Constraints: no text, no watermark, no brand imitation, no real maps, no copyrighted elements, no transparent background, no dark background, no famous characters, no imitation of another app UI.
```

Saved outputs:
- `qa-artifacts/imagegen/level_illustration_concept_2026-05-21.png` — generated source concept.
- `qa-artifacts/imagegen/level_illustration_processed_720x405_2026-05-21.webp` — exact processed app illustration.
- `app/src/main/res/drawable-nodpi/level_illustration.webp` — production in-app level screen asset.

## Action Panel Texture
Финальный in-app action panel refresh 2026-05-21 выполнен через built-in ImageGen и обработан до app-ready `720x240` RGB WebP. Подложка добавлена за отдельными кнопками уровня и не содержит пользовательского текста.

Final prompt:

```text
Use case: illustration-story
Asset type: Compact in-game action panel texture for an Android puzzle game
Primary request: Create a polished subtle horizontal texture/background for the action controls area of the Russian-language offline color puzzle game "Цветной Квартал". No text, no letters, no numbers, no logos, no characters, no people.
Scene/backdrop: warm off-white rounded-card friendly background, very subtle city-block mosaic pattern, clean and bright.
Subject: three softly separated rounded square color zones suggesting three action buttons, with tiny abstract symbols only as geometric motifs: a curved return path, a small sparkle hint mark, and a circular restart path. The symbols must be decorative and not contain readable text. Use teal lagoon, warm yellow, berry red, mint green, soft violet and deep ink accents at low intensity.
Style: crisp modern vector-like mobile game illustration, flat shapes with soft depth, same visual world as Color Quarter onboarding/home/level/victory/defeat illustrations and Google Play feature graphic, Android-first, premium casual puzzle feel.
Composition: wide compact banner, safe center area, generous padding, readable when cropped into a low 64dp mobile action-panel background. Keep the texture subtle enough that real app button labels/icons placed above remain readable.
Constraints: no text, no watermark, no brand imitation, no real maps, no copyrighted elements, no transparent background, no dark background, no famous characters, no imitation of another app UI, no busy details.
```

Saved outputs:
- `qa-artifacts/imagegen/action_panel_texture_concept_2026-05-21.png` — generated source concept.
- `qa-artifacts/imagegen/action_panel_texture_processed_720x240_2026-05-21.webp` — exact processed app texture.
- `app/src/main/res/drawable-nodpi/action_panel_texture.webp` — production in-app action bar texture.

## Victory Result Illustration
Финальный in-app victory refresh 2026-05-20 выполнен через built-in ImageGen и обработан до app-ready `720x405` RGB WebP. Иллюстрация добавлена в win result panel, чтобы успешное завершение уровня имело собственный polished visual reward.

Final prompt:

```text
Use case: illustration-story
Asset type: Compact in-game victory result illustration for an Android puzzle game
Primary request: Create a polished small victory illustration for the Russian-language offline color puzzle game "Цветной Квартал". No text, no letters, no numbers, no logos, no characters, no people.
Scene/backdrop: warm off-white rounded-card friendly background, subtle city-block pattern, clean and bright.
Subject: a completed 5x5 rounded-square color puzzle board where all tiles harmonize into a calm teal lagoon region, surrounded by a few tiny warm yellow star accents and small color swatches in berry red, mint green, sun yellow, and soft violet. The image should communicate that the district is completed and unified.
Style: crisp modern vector-like mobile game illustration, flat shapes with soft depth, same visual world as the existing Color Quarter onboarding and Google Play feature graphic, Android-first, premium casual puzzle feel.
Composition: wide compact banner, centered board with generous padding, readable at small size inside a result panel, no tiny details, no screenshots, no UI labels.
Constraints: no text, no watermark, no brand imitation, no real maps, no copyrighted elements, no transparent background, no dark background.
```

Saved outputs:
- `qa-artifacts/imagegen/victory_illustration_concept_2026-05-20.png` — generated source concept.
- `qa-artifacts/imagegen/victory_illustration_processed_720x405_2026-05-20.webp` — exact processed app illustration.
- `app/src/main/res/drawable-nodpi/victory_illustration.webp` — production in-app victory result asset.

## Defeat Result Illustration
Финальный in-app defeat refresh 2026-05-20 выполнен через built-in ImageGen и обработан до app-ready `720x405` RGB WebP. Иллюстрация добавлена в out-of-moves result panel как спокойный visual feedback без наказующего тона.

Final prompt:

```text
Use case: illustration-story
Asset type: Compact in-game defeat/out-of-moves result illustration for an Android puzzle game
Primary request: Create a polished small defeat-state illustration for the Russian-language offline color puzzle game "Цветной Квартал". No text, no letters, no numbers, no logos, no characters, no people.
Scene/backdrop: warm off-white rounded-card friendly background, subtle city-block pattern, clean and bright, slightly calmer than the victory art but not sad or alarming.
Subject: a 5x5 rounded-square color puzzle board that is almost completed but still has a few remaining neighboring color tiles, showing that the player ran out of moves and can try a different order. Use a mostly teal lagoon connected region from the upper-left, with a few separate warm yellow, berry red, mint green, and soft violet tiles left around the edges. Add two small soft amber caution spark accents and scattered color swatches, all secondary.
Style: crisp modern vector-like mobile game illustration, flat shapes with soft depth, same visual world as the existing Color Quarter onboarding, victory illustration, and Google Play feature graphic, Android-first, premium casual puzzle feel.
Composition: wide compact banner, centered board with generous padding, readable at small size inside a result panel, no tiny details, no screenshots, no UI labels.
Constraints: no text, no watermark, no brand imitation, no real maps, no copyrighted elements, no transparent background, no dark background, no skulls, no angry symbols, no red warning signs.
```

Saved outputs:
- `qa-artifacts/imagegen/defeat_illustration_concept_2026-05-20.png` — generated source concept.
- `qa-artifacts/imagegen/defeat_illustration_processed_720x405_2026-05-20.webp` — exact processed app illustration.
- `app/src/main/res/drawable-nodpi/defeat_illustration.webp` — production in-app defeat result asset.

## Empty State
Prompt: `Small friendly empty state illustration for a puzzle level list: a neat stack of colorful square tiles, off-white background, teal yellow berry mint violet palette, simple flat mobile UI style, no text.`

## Future Background Pattern Variants
Prompt: `Subtle decorative background pattern for a mobile puzzle app: very light off-white city grid lines, low contrast, no gradients, no icons, seamless, not distracting.`
