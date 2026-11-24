# Test Checklist

Run with `VITE_USE_MOCK=true` unless a step says "real backend".

## Editor and layout
- [ ] Editor loads with Python starter code
- [ ] Syntax highlighting changes for Java, Python and JavaScript
- [ ] Typing works and the status bar Ln/Col updates
- [ ] Dragging the divider resizes the console
- [ ] Layout works on a small window (header stacks)

## Tabs
- [ ] + adds a new tab
- [ ] Switching tabs keeps each tab's code
- [ ] Double-click renames, Enter saves, Esc cancels
- [ ] x closes a tab, the last tab cannot be closed
- [ ] Changing language affects only the active tab

## Run and console
- [ ] Run shows "Running..." then output and "Finished in X ms"
- [ ] Ctrl+Enter runs the code from inside the editor
- [ ] Run button is disabled while running
- [ ] Empty editor shows a warning
- [ ] `while True:` shows a timeout warning
- [ ] `raise Exception("x")` shows an error stack
- [ ] `MEMORY_TEST` shows a memory limit warning
- [ ] Copy copies the console text
- [ ] Clear empties the console
- [ ] Status bar shows the last run time

## Error handling
- [ ] VITE_USE_MOCK=false and backend off: "Connection error" message, UI does not hang

## Real backend (when available)
- [ ] Python, Java and JavaScript each return real output
- [ ] Infinite loop returns TIMEOUT
- [ ] No CORS error in the browser console

## Result
Tested on: ____  Passed: ____  Failed / notes: ____