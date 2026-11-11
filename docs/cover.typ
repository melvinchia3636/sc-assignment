// ===========================================================================
// Cover page - Software Construction group assignment
//
// Usage in the main document:
//   #import "cover.typ": cover
//   #cover()
// ===========================================================================

#let ink = luma(30)
#let slate = luma(90)
#let muted = luma(120)
#let rule = luma(190)
#let serif = "Times New Roman"
#let sans = "DM Sans 9pt"

#let info-row(label, value) = (
  text(font: sans, size: 7.5pt, tracking: 0.12em, fill: muted)[#upper(label)],
  text(font: serif, size: 10.5pt, fill: ink)[#value],
)

#let cover-field(label, value, size: 9.5pt, line: true) = (
  text(font: sans, size: 7.5pt, tracking: 0.12em, fill: muted)[#upper(label)],
  box(
    width: 100%,
    height: 1.2em,
    stroke: if line { (bottom: 0.5pt + rule) } else { none },
    align(left + horizon, {
      show link: it => text(fill: rgb("#0563C1"), underline(it))
      text(font: serif, size: size, weight: "bold", fill: ink)[#value]
    }),
  ),
)

#let cover() = {
  set page(
    paper: "a4",
    margin: (top: 1.6cm, bottom: 1.4cm, x: 2.3cm),
  )
  set text(font: serif, size: 10.5pt, fill: ink, lang: "en")

  align(center)[
    #image("assets/taylors-logo.png", width: 8.4cm)
  ]

  v(1.1em)
  align(center)[
    #text(font: sans, size: 12pt, tracking: 0.14em, fill: muted)[
      SCHOOL OF COMPUTER SCIENCE \
      BACHELOR OF SOFTWARE ENGINEERING
    ]
  ]

  v(1.1em)
  align(center)[
    #text(font: sans, size: 21pt, weight: "bold", fill: ink)[GROUP ASSIGNMENT]
    #v(0em)
    #text(font: sans, size: 10.5pt, fill: slate)[
      CSC62604 / CSC61204 Software Construction
    ]
  ]

  v(1.35em)
  align(center)[
    #text(font: sans, size: 16.5pt, weight: "bold", fill: ink)[
      SDG 11 — Sustainable Cities and Communities \
      Software System
    ]
  ]

  v(2em)
  align(center)[
    #table(
      columns: (3.7cm, 9.2cm),
      stroke: none,
      inset: (x: 0pt, y: 4.5pt),
      align: (left + horizon, left + horizon),
      ..info-row("Module Name", [Software Construction]),
      ..info-row("Module Code", [CSC62604 / CSC61204]),
      ..info-row("Module Leader", [Dr. Reema Bin Thabit]),
      ..info-row("Assessment", [Group Assignment · 30% · 100 marks]),
      ..info-row("Learning Outcome", [MLO2]),
      ..info-row("Due Date", [Week 11, 1st December 2026]),
    )
  ]

  v(1.9em)
  align(center)[
    #text(font: sans, size: 8pt, tracking: 0.16em, fill: muted)[GROUP MEMBERS]
  ]

  v(0.65em)
  align(center)[
    #table(
      columns: (0.9cm, 6.3cm, 3.0cm, 3.0cm),
      stroke: 0.4pt + rule,
      inset: (x: 7pt, y: 10pt),
      align: (left + top, left + top, center + top, center + horizon),
      fill: (col, row) => if row == 0 { ink.lighten(92%) } else { white },
      table.header(
        text(font: sans, size: 8pt, weight: "bold")[No.],
        text(font: sans, size: 8pt, weight: "bold")[Name],
        text(font: sans, size: 8pt, weight: "bold")[Student ID],
        text(font: sans, size: 8pt, weight: "bold")[Signature],
      ),
      [1], [Melvin Chia Pin Wen], [0383080], [],
      [2], [Wu Zhuofei], [0384209], [],
      [3], [Cheng Jun Hao], [0383484], [],
      [4], [Danial Abir], [0374873], [],
    )
  ]

  v(1em)
  align(center)[
    #table(
      columns: (5.3cm, 7.9cm),
      stroke: none,
      inset: (x: 0pt, y: 5pt),
      align: (left + horizon, left + horizon),
      ..cover-field("Group Name", [Group 01]),
      ..cover-field("Scenario Selected (1–5)", [5 — Urban Air Quality and Green Space Monitor]),
      ..cover-field(
        "GitHub Repository URL",
        link("https://github.com/melvinchia3636/sc-assignment"),
        size: 8.5pt,
        line: false,
      ),
    )
  ]

  v(1fr)
  align(center)[
    #text(font: sans, size: 8pt, fill: muted)[
      Group 01 · Software Construction (CSC62604 / CSC61204)
    ]
  ]
}
