// ===========================================================================
// Table of contents - Software Construction group assignment
//
// Real, auto-generated from the headings in report.typ.
//
// Usage in the main document:
//   #import "toc.typ": toc
//   #toc()
// ===========================================================================

#import "setup.typ": ink, serif

#let toc() = {
  heading(numbering: none, outlined: false)[Contents]

  set outline.entry(fill: repeat([.], gap: 0.18em))
  show outline.entry: set text(font: serif, size: 10.5pt, fill: ink)
  show outline.entry.where(level: 1): set text(weight: "bold")
  show outline.entry.where(level: 1): set block(above: 1.05em)
  show outline.entry: set block(below: 0.4em)

  outline(title: none, depth: 2, indent: auto)
}
