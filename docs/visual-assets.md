# MiniLedger reference assets

Generated with the built-in imagegen tool. Images are bundled offline in `app/src/main/res/drawable-nodpi/`.

- `onboarding_ledger.png`: Portrait editorial photograph of a dark forest-green cloth ledger on a warm wooden board and beige stone desk, black-and-gold fountain pen, dramatic afternoon window light. No text or UI.
- `otp_interior.png`: Portrait warm ivory plaster interior, window shadows, cane chair at lower left and leafy plant in a ceramic pot at lower right; empty upper wall for OTP controls.
- `dashboard_wall.png`: Landscape greige plaster wall, diagonal sunlight and window shadows, plant at far right, darker empty left for white dashboard text.
- `new_loan_notebook.png`: Landscape warm oak desk, cream lined notebook, black fountain pen, handwritten “New Loan”, afternoon window shadows.

These are newly generated photographs matching the reference mood, not identical source photos. Native Android controls and real account data remain in use.

## October 23 reference update

The supplied reference is saved as `docs/reference-design.png`. The rejected editorial mockup was not implemented.

Built-in imagegen recreated these project assets:
- `app/src/main/res/drawable-nodpi/home_cafe.png`: Recreate only the left reference phone's clean portrait background: rustic sunlit cafe table by a right-side window, greenery, cream spiral notebook with “Good friends, better memories...”, black-and-gold fountain pen, warm shadows, dark upper left for greeting. No UI or phone frame.
- `app/src/main/res/drawable-nodpi/loan_green_notebook.png`: Recreate only the center reference phone's photograph: dark green notebook angled on the upper right of a wooden table, gold handwritten “New Loan”, leaf shadows and a potted plant, dark empty left side for the heading. No UI or phone frame.

Both photos are recreations rather than the original source photographs. History thumbnails are decorative stationery images, not borrower photos. Amounts, statuses and dates are real loan data; the month-to-month percentage is shown only when prior-month lending exists. Notes are persisted by a non-destructive Room v1-to-v2 migration.

## Three-page introduction
Built-in imagegen recreated three photographic assets from the supplied October 23 onboarding reference. These are close recreations, not the original source photos. Native text and navigation are rendered separately. The illustrated loan entries in the second page are sample artwork, not account data.
- `app/src/main/res/drawable-nodpi/intro_ledger.png`: Clean portrait background matching the left reference: warm ivory wall and window shadows, marble tabletop, leafy plant at far right, stacked forest-green MiniLedger notebook at lower right. No phone frame or UI; blank upper/middle left for text.
- `app/src/main/res/drawable-nodpi/intro_history.png`: Dark forest-green portrait product photograph, tilted smartphone showing ivory Loan History with sample Rahul/Siddharth/Aman/Neha rows, handwritten “All your loans at a glance” and arrow. No outer phone, onboarding controls, or foreground panel.
- `app/src/main/res/drawable-nodpi/intro_private.png`: Clean right-reference background: ivory wall on left, green panel at right, diagonal sunlight, textured cream vase with leafy branch on oak cabinet lower right. No UI/text.
Headings use bundled DM Serif Display from the Google Fonts repository; license in `docs/DMSerifDisplay-OFL.txt`. Copy avoids claiming reminder delivery, cloud synchronization, or exclusive access that the current app does not implement.

## Paper form and welcome screen
Built-in imagegen assets, recreated from the latest supplied references:
- `app/src/main/res/drawable-nodpi/add_loan_paper.png`: Clean portrait photo of warm desk, green New Loan notebook and pen upper right, large blank textured ivory sheet with sloping top/left edges and brass binder clip; curled lower-right paper, blurred leaves lower left. No UI/phone frame. Native form overlays the paper.
- `app/src/main/res/drawable-nodpi/welcome_desk.png`: Ivory wall with sunlit window shadows, leaves left, desk below with spiral notebook, fountain pen, coffee mug and books. Empty upper/middle wall for native welcome logo/text, no UI/frame.
Launcher artwork is a native vector book symbol on green; Android's system splash is green with a white book. The photographic welcome screen is no longer displayed.

## Spiral loan form revision
Latest revision: built-in imagegen created `app/src/main/res/drawable-nodpi/add_loan_clipped.png`, copied to `mobile/assets/paper.png`. Prompt: reproduce the latest reference background in 1024x2048 portrait; warm wood upper 36%, tilted green New Loan notebook and pen upper right, blank ivory paper below with rounded upper-left corner and gently sloping top, brass binder clip at x88%/y35%. Match reference lighting and palette, realistic cream paper, no UI, labels, buttons, title, or phone frame. Only notebook lettering is allowed. Form controls are native, placed below the clip.
Built-in imagegen produced `app/src/main/res/drawable-nodpi/add_loan_spiral.png`, a photographic recreation of the supplied reference. Text, inputs, and buttons are native controls, not baked into the photograph. Existing `onboarding_ledger.png` and `otp_interior.png` provide distinct History and Profile headers.

Final generation prompt: Create a production mobile app background image, portrait 1024x2048. Reference is the user's latest attached Add New Loan screen. Reproduce ONLY its photographic background, no UI, no text, no icons, no phone bezel or status bar. Warm dark walnut desk, sun shadows, plant leaves upper right, a blank ivory spiral notebook occupies lower 70% of entire canvas. Its top edge starts at y=29%, angled very slightly upward to right, black binder clip on top right at y=26%, spiral binding along far left. Paper extends to y=97%, with its whole interior blank and unobstructed for live form controls between x=10%-91%, y=37%-89%. Top 28% is dark wooden desk for a white app title, no big green notebook. Subtle out of focus leaves bottom left. Follow reference layout closely, realistic paper grain, warm photographic light, muted cream, brown and forest green. Absolutely no lettering, fields or buttons.
