

My original approach tried to squeeze by on primitive types and lists between the components and the FRP scripts.
This was inadequate given the complexity of the ASR data, so, I added a `struct` metaphor.
Initially, I focussed on adding *just* CMUSphinx so this branch adds support for Googal Cloud ASR (gasr) to the new implementation.
To expand this, I wanted/needed some sort of "import" system so that I could share IDL definitions between modules.
This allowed me to move the "microhpne" logic out of the ASR modules and give each ASR it's own module.

[![](https://mermaid.ink/img/eyJjb2RlIjoiXG5ncmFwaCBURFxuXG4gICAgbmV3TWljcm9waG9uZVtzaGFyZWQgTWljcm9waG9uZV1cblxuICAgIG5ld0NNVVNwaGlueFtuZXcgQ01VU3BoaW54IG9ubHldXG4gICAgbmV3R29vZ2xlQVNSW25ldyBHb29nbGVBU1Igb25seV1cblxuICAgIG5ld0dvb2dsZUFTUiAtLT58aW1wb3J0c3xuZXdNaWNyb3Bob25lXG4gICAgbmV3Q01VU3BoaW54IC0tPnxpbXBvcnRzfG5ld01pY3JvcGhvbmUiLCJtZXJtYWlkIjp7fSwidXBkYXRlRWRpdG9yIjpmYWxzZX0)](https://mermaid-js.github.io/mermaid-live-editor/#/edit/eyJjb2RlIjoiXG5ncmFwaCBURFxuXG4gICAgbmV3TWljcm9waG9uZVtzaGFyZWQgTWljcm9waG9uZV1cblxuICAgIG5ld0NNVVNwaGlueFtuZXcgQ01VU3BoaW54IG9ubHldXG4gICAgbmV3R29vZ2xlQVNSW25ldyBHb29nbGVBU1Igb25seV1cblxuICAgIG5ld0dvb2dsZUFTUiAtLT58aW1wb3J0c3xuZXdNaWNyb3Bob25lXG4gICAgbmV3Q01VU3BoaW54IC0tPnxpbXBvcnRzfG5ld01pY3JvcGhvbmUiLCJtZXJtYWlkIjp7fSwidXBkYXRlRWRpdG9yIjpmYWxzZX0)

This update didn't yeild any interesting notes about the implementation of a component.
I will have to update the tutorial with a section for the new ASR but once that's done - I can merge.