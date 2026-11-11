// ===========================================================================
// Shared document setup - Software Construction group assignment
//
// Usage in the main document:
//   #import "setup.typ": apply-style
//   #show: apply-style
// ===========================================================================

// ---------------------------------------------------------------------------
// Design tokens
// ---------------------------------------------------------------------------

#let ink = luma(30)
#let slate = luma(90)
#let muted = luma(120)
#let rule = luma(190)

#let serif = "Times New Roman"
#let sans = "DM Sans 9pt"

// ---------------------------------------------------------------------------
// Page and typographic style
// ---------------------------------------------------------------------------

#let apply-style(body) = {
  set page(
    paper: "a4",
    margin: (top: 2.6cm, bottom: 2.2cm, x: 2.3cm),
    header: context {
      if counter(page).get().first() > 1 [
        #set text(font: sans, size: 8pt, fill: luma(115))
        #grid(
          columns: (1fr, auto),
          [Urban Air Quality and Green Space Monitor],
          [Software Construction · Group 01],
        )
        #v(-0.35em)
        #line(length: 100%, stroke: 0.4pt + rule)
      ]
    },
    footer: context {
      if counter(page).get().first() > 1 [
        #set text(font: sans, size: 8pt, fill: luma(115))
        #align(center)[
          Page #counter(page).display("1")
          of #counter(page).final().first()
        ]
      ]
    },
  )

  set text(font: serif, size: 10.5pt, lang: "en")
  set par(justify: true, leading: 0.72em, spacing: 9pt)

  // Headings: numbered, sans, ink.
  set heading(numbering: "1.1")
  show heading: it => {
    if it.level == 1 {
      if it.numbering != none {
        pagebreak(weak: true)
      }
      block(above: 1.6em, below: 1.1em, width: 100%)[
        #set text(font: sans, size: 15pt, weight: "bold", fill: ink)
        #set par(justify: false, leading: 0.65em)
        #if it.numbering != none [
          #context counter(heading).display(it.numbering)
          #h(0.5em)
        ]
        #it.body
        #v(0.2em)
      ]
    } else if it.level == 2 {
      block(above: 1.4em, below: 0.7em, width: 100%)[
        #set text(font: sans, size: 12pt, weight: "bold", fill: ink)
        #set par(justify: false, leading: 0.6em)
        #if it.numbering != none [
          #context counter(heading).display(it.numbering)
          #h(0.45em)
        ]
        #it.body
      ]
    } else {
      block(above: 1.3em, below: 0.6em)[
        #set text(font: sans, size: 10.5pt, weight: "bold", fill: ink)
        #it.body
      ]
    }
  }

  set list(indent: 1.15em, spacing: 0.65em)
  set enum(indent: 1.15em, spacing: 0.65em)

  show link: it => text(fill: rgb("#0563C1"), it)

  body
}
