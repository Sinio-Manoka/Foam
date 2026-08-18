# Foam — CSS / layout coverage audit

This is a snapshot of what the current Foam prototype supports and what is
missing relative to a "full CSS" goal. Read this before adding new
properties or restructuring anything — it tells you exactly where the gaps
are and how big they are.

The prototype currently consists of:

- `app/src/main/kotlin/core/node/Node.kt`, `ElementNode.kt`, `TextNode.kt`
- `app/src/main/kotlin/core/view/View.kt` (fluent modifier base)
- `app/src/main/kotlin/core/geometry/LayoutRect.kt`
- `app/src/main/kotlin/dsl/{ViewScope,ExportScope,VStack,Button,Text}.kt`
- `app/src/main/kotlin/style/ComputedStyle.kt`
- `app/src/main/kotlin/style/css/Css4jStyleEngine.kt`
- `app/src/main/kotlin/layout/LayoutEngine.kt`
- `app/src/main/kotlin/layout/taffy/TaffyLayoutEngine.kt`
- `app/src/main/kotlin/render/Renderer.kt`
- `app/src/main/kotlin/render/skia/SkiaRenderer.kt`
- `app/src/main/kotlin/text/{TextEngine,TextLayout}.kt`
- `app/src/main/kotlin/text/skia/SkiaTextEngine.kt`
- `app/src/main/kotlin/runtime/{FoamApp,FoamConfig,FoamRuntime}.kt`
- `app/src/main/resources/styles/app.css`

The audit is structured as:

1. **What is currently wired end-to-end** — i.e. the CSS property can be
   parsed, lives on `ComputedStyle`, and reaches both layout and paint.
2. **What is parsed but ignored** — the CSS property is parsed but doesn't
   flow into layout or paint.
3. **What is missing at each layer** — broken down by layer, so a fix in one
   layer is obvious.
4. **Other design-level issues** — not properties, but architectural smells.
5. **Prioritised gap list** — concrete tasks, ordered.

---

## 1. What is currently wired end-to-end

For every CSS property below: it is parsed by `Css4jStyleEngine`, it has a
field on `ComputedStyle`, the layout engine reads it (or would, if the
node uses it), and the renderer or text engine uses it.

### Box model

| CSS property        | Foam field         | Layout reads? | Renderer reads? |
| ------------------- | ------------------ | ------------- | --------------- |
| `width`             | `width: Float?`    | yes           | yes             |
| `height`            | `height: Float?`   | yes           | yes             |
| `padding` + 4-sided | `paddingTop/...`   | yes           | (used by Taffy) |
| `margin` + 4-sided  | `marginTop/...`    | yes           | (used by Taffy) |

### Visual

| CSS property        | Foam field         | Renderer reads? |
| ------------------- | ------------------ | --------------- |
| `background-color`  | `backgroundColor`  | yes (RRect fill) |
| `border-radius`     | `borderRadius`     | yes             |
| `color` (text)      | `textColor`        | yes (Skia paint) |
| `font-size`         | `fontSize`         | yes             |

### Flex container (parent)

| CSS property       | Foam field       | Taffy reads? |
| ------------------ | ---------------- | ------------ |
| `display: flex`    | `display`        | yes          |
| `display: block`   | `display`        | yes          |
| `display: none`    | `display`        | yes (children hidden) |
| `flex-direction`   | `flexDirection`  | yes          |
| `justify-content`  | `justifyContent` | yes (via `AlignContent`) |
| `align-items`      | `alignItems`     | yes          |
| `gap`              | `gap`            | yes          |

### Numbers worth keeping in mind

- `paddingLeft + paddingRight` is subtracted from `availableWidth` when
  descending into a node's children. (Recent fix.)
- Text node is given the parent's **content** width (not the natural text
  width) so Skia's `Alignment.CENTER` matches the box Taffy positions.

---

## 2. What is parsed but ignored

I went through `Css4jStyleEngine` line by line. **Nothing is parsed-but-ignored
right now** — every parsed property reaches `ComputedStyle` and is consumed
either by Taffy or by the Skia renderer. (This is small enough that the next
section, "what is missing", is the real story.)

---

## 3. What is missing at each layer

### 3.1 CSS parser / `ComputedStyle` (the biggest gap)

These are CSS properties that should exist for "full support" and **don't**.
Every one of them is a clean, contained addition once we have the right
infrastructure (see §5.5 about a property registry).

#### Sizing & box model

| CSS property | Notes |
| --- | --- |
| `min-width`, `min-height` | Taffy supports these natively as `TaffyDimension`. |
| `max-width`, `max-height` | Same; only `length` for now, no `100%` / `min-content`. |
| `box-sizing: border-box` (default) vs `content-box` | Taffy supports it. We always use border-box semantics by hand. |
| `width: 100%`, `width: 50%` etc. | Taffy supports it. We currently only support `length` and `auto`. **CSS percentages don't parse.** |
| `aspect-ratio` | Taffy supports it; we don't expose it. |
| `overflow: hidden / scroll / visible` | Taffy supports it; no clipping in renderer. |

#### Position (CSS positioning)

| CSS property | Notes |
| --- | --- |
| `position: relative / absolute / fixed / sticky` | **Completely missing.** Taffy supports all four. |
| `top`, `right`, `bottom`, `left` | **Missing.** Required for any absolute positioning. |
| `z-index` / stacking | **Missing.** No layer ordering. |
| `inset` shorthand | **Missing.** |

#### Flex item (child)

| CSS property | Notes |
| --- | --- |
| `flex-grow`, `flex-shrink`, `flex-basis` | **Missing.** Items can't grow/shrink. Layout currently uses natural sizes only. |
| `flex: 1` shorthand | **Missing** (depends on the three above). |
| `align-self` | **Missing.** |
| `order` | **Missing.** |
| `flex-wrap: wrap / nowrap / wrap-reverse` | **Missing.** No multi-line flex yet. |

#### Visual

| CSS property | Notes |
| --- | --- |
| `border` + `border-{top,right,bottom,left}` and `border-{width,style,color}` | **Missing.** Only `border-radius` is rendered. No border, no border colour. |
| `border-color`, `border-style`, `border-width` | **Missing.** |
| `box-shadow`, `text-shadow` | **Missing.** |
| `opacity` | **Missing.** |
| `background-image: linear-gradient(...)`, `radial-gradient(...)` | **Missing.** Solid colour only. |
| `background-position`, `background-size`, `background-repeat` | **Missing.** No images at all yet. |
| `outline` | **Missing.** |
| `filter: blur(...)` etc. | **Missing.** |
| `visibility: hidden` | **Missing** (`display: none` is supported but doesn't fully hide). |

#### Text

| CSS property | Notes |
| --- | --- |
| `text-align: left / right / center / justify` | **Missing.** Paragraph hard-codes `Alignment.CENTER`. |
| `line-height` (and unit-less number form) | **Missing.** Paragraph uses default. |
| `letter-spacing` | **Missing.** |
| `word-spacing` | **Missing.** |
| `text-decoration: underline / overline / line-through` | **Missing.** |
| `text-transform: uppercase / lowercase / capitalize` | **Missing.** |
| `font-family` | **Missing.** Hard-coded to "Segoe UI". |
| `font-weight: 100..900`, `bold`, `normal` | **Missing.** No way to get a bold typeface. |
| `font-style: italic` | **Missing.** |
| `white-space: nowrap / normal / pre / pre-wrap` | **Missing** — we hard-code `maxLinesCount = 1`. |
| `text-overflow: ellipsis` + `overflow: hidden` | **Missing.** No way to render "Continue…" when text overflows. |
| `line-clamp` (line-count) | **Missing.** |
| `word-break`, `overflow-wrap` | **Missing.** |

#### Selector support

The CSS parser supports only class selectors (`.foo`). What is missing:

| Selector | Notes |
| --- | --- |
| `tag` selectors (`button { ... }`) | Not supported. |
| `#id` selectors | `Node.id` exists on the model but isn't matched. |
| Descendant / child / sibling combinators | Not supported. |
| `:hover`, `:focus`, `:active`, `:disabled` (pseudo-classes) | Not supported. **Required for buttons.** |
| `::before`, `::after` (pseudo-elements) | Not supported. |
| `,` group selector (`.a, .b { ... }`) | Not supported. |
| `[attr=value]` | Not supported. |

This is the single biggest usability gap — without pseudo-classes you can't
build any interactive button.

### 3.2 Layout engine (`TaffyLayoutEngine`)

Even for the properties we *do* have on `ComputedStyle`, the layout engine
has gaps:

- **`flex-grow` / `flex-shrink`** — Taffy exposes these but we never set them.
- **`align-self`** — same.
- **`flex-wrap`** — same.
- **`AvailableSpace`** — we always pass `AvailableSpace.definite(...)` for
  both axes. We should pass `max-content` for the cross axis when computing
  intrinsic sizes, otherwise we can't measure things correctly in a
  re-layout scenario (e.g. when content size changes).
- **Measure callback** — Taffy supports `computeLayoutWithMeasure` which
  lets Taffy ask text for its size lazily. We pre-measure everything in
  one pass, which is simpler but means we don't support cases where a
  child's content drives a parent's size. For our prototype this is fine,
  but it'll bite later.
- **Re-layout** — every frame we rebuild the tree from scratch. For a small
  UI this is fine; for a real app we should diff.
- **Dirty propagation** — none. We re-layout everything on every frame
  even when nothing changed.

### 3.3 Text engine (`SkiaTextEngine`)

- **Hard-coded `fontFamilies = arrayOf("Segoe UI")`** — no `font-family`.
- **Hard-coded `Alignment.CENTER`** — no `text-align`.
- **Hard-coded `maxLinesCount = 1`** — no multi-line, no `white-space`.
- **Hard-coded `ellipsis = null`** — no `text-overflow`.
- **`TextStyle.fontFamilies` is a single fallback list** — no per-locale or
  per-character fallbacks. The Skia API supports it.
- **`TextStyle.fontStyle`** (weight + italic) — never set.
- **`TextStyle.decoration`** — never set.
- **`TextStyle.letterSpacing` / `wordSpacing`** — never set.
- **No bidi / RTL** — fine for English-only apps, but a real gap.

### 3.4 Renderer (`SkiaRenderer`)

The renderer is the thinnest layer — it does **only**:

- Fill a rounded rect with `backgroundColor`.
- Paint text via the text engine.

Missing:

- **Borders** (width, style, colour, radius per corner).
- **Box-shadow** / **text-shadow**.
- **Linear/radial gradient** backgrounds.
- **`opacity`** (alpha applied to a layer).
- **Clipping** (`overflow: hidden`).
- **Image backgrounds**.
- **Pseudo-state styles** (`hover`, `active`, `disabled`) — would need
  pointer-event handling at a higher layer first; renderer just needs to
  consume a state-dependent `ComputedStyle`.
- **Cached paint** — we recreate the `Paint` and re-issue the draw every
  frame. Cheap but worth knowing.
- **Multi-layer / z-index** — currently the tree order *is* the paint
  order; z-index would require a different model.

### 3.5 Runtime / app model

- **No event handling** — the `Renderer` doesn't react to clicks, hover,
  focus, keyboard. We can't wire `:hover` until we have at least a basic
  pointer-event pipeline (hit-testing against the laid-out tree).
- **`FoamApp` doesn't expose a way to update state** — to make a button
  respond to hover we'd need an observable state model and a way to
  trigger a re-style / re-layout / re-render.
- **`FoamApp.launch()` blocks on `app.runEventLoop`** — fine for a desktop
  app but not testable without a display.

---

## 4. Other design-level issues (not properties)

These are smells that will hurt as the codebase grows:

### 4.1 Enums in one file

`ComputedStyle.kt` currently contains four enums (`Display`, `FlexDirection`,
`JustifyContent`, `AlignItems`) **and** the `ComputedStyle` data class.
This is exactly what you flagged. With the rest of the audit in mind, the
right shape is:

- `style/display/Display.kt`
- `style/flex/FlexDirection.kt`
- `style/flex/JustifyContent.kt`
- `style/flex/AlignItems.kt`
- `style/flex/AlignSelf.kt` (new)
- `style/flex/FlexWrap.kt` (new)
- `style/flex/FlexGrow.kt`, `FlexShrink.kt`, `FlexBasis.kt` (new, or values
  on `ComputedStyle`)
- `style/position/Position.kt` (new)
- `style/text/TextAlign.kt` (new)
- `style/text/FontWeight.kt` (new)
- `style/text/WhiteSpace.kt` (new)
- `style/overflow/Overflow.kt` (new)
- `ComputedStyle.kt` reduced to just the data class (or split per concern:
  `BoxStyle.kt`, `FlexContainerStyle.kt`, `FlexItemStyle.kt`, `VisualStyle.kt`,
  `TextStyle.kt`).

### 4.2 No DSL component base

Right now `Button`, `Text`, `VStack` are top-level / extension functions
sitting in `dsl/`. There's no way to introduce a new component (e.g. `Image`,
`Checkbox`, `TextField`) without copy-pasting the same 10 lines:

```kotlin
fun ViewScope.Button(vararg classNames: String, content: ViewScope.() -> Unit): ElementNode {
    val node = ElementNode("button")
    node.classes += classNames
    ViewScope(node).apply(content)
    add(node)
    return node
}
```

A `Component` abstraction that takes a `type: String` and an optional
`content` lambda removes all that duplication and gives us one place to
add common modifiers (`id`, `aria-*`, etc.).

### 4.3 CSS parser is a long if-else chain

`Css4jStyleEngine.applyRule` is 170 lines of `declaration.getPropertyValue(...).takeIf { ... }.let { ... }`.
This is fine at the current size but is a code smell that gets worse with
every property we add.

The right shape is a **property registry**: a `Map<String, PropertyHandler>`
where each handler is `(StyleRule, ComputedStyle) -> Unit`. Adding a new
property becomes "write one handler, register it" instead of "add a 6-line
block to a 170-line method".

### 4.4 Selector matching is class-only

`findRule` does a linear scan and matches `selectorList.toString().trim() == ".foo"`.
This is fine for one CSS file with a handful of rules. Once we have any
state-driven styles (`:hover`), we need a real selector matcher.

### 4.5 The renderer / text engine hold no cache invalidation

Every frame we re-measure, re-layout, and re-paint the whole tree. We need
a way to mark "this subtree is dirty, please re-style / re-layout /
re-paint" — and we need a way to express that at the DSL level (e.g. a
`MutableState<T>` similar to Compose's `mutableStateOf`).

### 4.6 Inline styles vs computed styles — only the latter is wired

`View.padding(...)` writes to `inlineStyle.paddingTop`, but nothing reads
`inlineStyle`. They are *not* merged into `computedStyle` before the
layout engine runs. So fluent modifiers effectively do nothing today.
This is a latent bug that will bite as soon as we start using
`View.padding(...)` in an `App.kt`.

### 4.7 Text height measurement is the only `TextNode` dimension

When a `TextNode` is laid out, we set its Taffy size to
`availableWidth × textHeight` (the fix from the centering bug). This
means multi-line text — once we add it — won't be able to drive its own
height; the parent has to give it room. This is correct CSS behavior for
fixed-height containers but worth flagging.

---

## 5. Prioritised gap list — what to build, in what order

This is the concrete plan that follows from §3 and §4.

### P0 — required before any meaningful UI

1. **CSS property registry** (replaces `applyRule` long if-else with a
   `Map<String, PropertyHandler>`). Unlocks everything below.
2. **`tag` and `#id` selectors**, plus combinators (descendant / child) —
   so we can target `.button` directly instead of through the class.
   Without this, every component has to be styled by class.
3. **`:hover`, `:focus`, `:active`, `:disabled` pseudo-classes** — so a
   `Button` can react to pointer state.
4. **Pointer event pipeline** — hit-test pointer events against the laid-
   out tree, update node state, trigger re-style.
5. **Inline → computed style merge** (fixes `View.padding()` etc.).

### P1 — design-level, needed for the codebase to stay maintainable

6. **Split `ComputedStyle.kt` per concern** (`BoxStyle`, `FlexContainer`,
   `FlexItem`, `Visual`, `Text`). Each in its own file under `style/`.
7. **Split enums into their own files** (one enum per file under
   `style/{flex,text,position,...}`).
8. **Component base class** — replaces the per-component copy-paste in
   `dsl/` with one `Component(type, content?)`.
9. **Selector matcher** — at minimum class + tag + id + descendant.

### P2 — box / position

10. **`position: relative / absolute / fixed`** + `top/right/bottom/left`.
11. **`min-width` / `min-height` / `max-width` / `max-height`**.
12. **`flex-grow` / `flex-shrink` / `flex-basis`** (and `flex` shorthand).
13. **`flex-wrap`** — multi-line flex.
14. **`align-self`**, **`order`**.
15. **`box-sizing`** toggle.
16. **`aspect-ratio`**.

### P3 — text

17. **`font-family`**.
18. **`text-align`**.
19. **`line-height`**.
20. **`letter-spacing`**, **`word-spacing`**.
21. **`font-weight`**, **`font-style`**.
22. **`white-space: normal / nowrap / pre / pre-wrap`**.
23. **`text-overflow: ellipsis`** (when paired with `white-space: nowrap`
    and `overflow: hidden`).
24. **`text-decoration`**.
25. **`text-transform`**.

### P4 — visual polish

26. **`border` + 4-sided + shorthand**.
27. **`border-radius` per corner** (we only have all-corners-uniform).
28. **`box-shadow`**, **`text-shadow`**.
29. **`opacity`**.
30. **`background-image: linear-gradient(...)`**, then `radial-gradient`.
31. **`outline`**.
32. **`overflow: hidden / scroll`** (with clipping in the renderer).

### P5 — image / advanced

33. **`<img>` / background images** (`background-image: url(...)`,
    `background-size`, `background-repeat`, `background-position`).
34. **`filter: blur(...)` etc.**
35. **`z-index`** and a real layer stack.

### P6 — performance

36. **Style / layout / paint dirty propagation** (mark dirty on
    `setState`, propagate up the tree).
37. **Text and paint caches** (cache `Paragraph`, cache rounded-rect
    paths, etc.).
38. **Re-layout only dirty subtrees** instead of the whole tree.

---

## 6. Suggested package layout for the refactor

This is the shape I'd suggest landing §5 P1 items into:

```
style/
  ComputedStyle.kt              (data class only — or composed of the below)
  BoxStyle.kt                   (width, height, min/max, padding, margin, box-sizing)
  VisualStyle.kt                (background-color, border, shadow, opacity)
  TextStyle.kt                  (font-*, text-*, letter-spacing, white-space)
  FlexContainerStyle.kt         (display, flex-direction, justify, align-items, gap, wrap)
  FlexItemStyle.kt              (flex grow/shrink/basis, align-self, order)
  PositionStyle.kt              (position, top/right/bottom/left, z-index)
  OverflowStyle.kt              (overflow-x/y)
  registry/
    CssProperty.kt              (interface: name, parse, apply)
    CssPropertyRegistry.kt      (the Map<String, CssProperty>)
  css/
    Css4jStyleEngine.kt         (drives the registry; no per-property code)

dsl/
  Component.kt                  (base — replaces the per-component copy-paste)
  components/
    Button.kt
    Text.kt
    VStack.kt
    Image.kt          (later)
    Checkbox.kt       (later)
    TextField.kt      (later)
```

### Open questions worth answering before the refactor

- Do we want **Sass-style nesting**, or stay flat-class?
- Do we want **CSS variables** (`var(--primary)`)? Cheap to support if we
  resolve them at parse time.
- Do we want **keyframes / transitions**? That's a much bigger lift; not in
  the P0–P2 list above.
- Do we want **runtime theme switching** (e.g. dark mode)? That's mostly a
  CSS parser feature (parse `@media (prefers-color-scheme: dark)`).
