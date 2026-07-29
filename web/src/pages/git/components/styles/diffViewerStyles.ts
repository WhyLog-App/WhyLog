import type { ReactDiffViewerStylesOverride } from "react-diff-viewer-continued";

const DIFF_STYLES: ReactDiffViewerStylesOverride = {
  variables: {
    light: {
      diffViewerBackground: "#f8fdf9",
      diffViewerColor: "var(--color-text-primary)",
      addedBackground: "#e8f5e9",
      addedColor: "#2e7d32",
      removedBackground: "var(--color-red-50)",
      removedColor: "var(--color-red-700)",
      wordAddedBackground: "transparent",
      wordRemovedBackground: "transparent",
      addedGutterBackground: "#e8f5e9",
      removedGutterBackground: "var(--color-red-50)",
      gutterBackground: "#f8fdf9",
      gutterColor: "var(--color-text-secondary)",
      addedGutterColor: "var(--color-text-secondary)",
      removedGutterColor: "var(--color-text-secondary)",
      emptyLineBackground: "#f8fdf9",
    },
  },
  diffContainer: {
    width: "100%",
    fontFamily: '"Menlo", "Monaco", "Consolas", monospace',
    fontSize: "12px",
  },
  gutter: {
    width: "48px",
    minWidth: "48px",
    maxWidth: "48px",
    padding: "0",
    textAlign: "right" as const,
  },
  lineNumber: {
    fontFamily: '"Menlo", "Monaco", "Consolas", monospace',
    fontSize: "12px",
    color: "var(--color-text-secondary)",
    paddingRight: "12px",
    paddingLeft: "8px",
  },
  marker: {
    fontFamily: '"Menlo", "Monaco", "Consolas", monospace',
    fontSize: "12px",
    padding: "0 8px 0 12px",
    minWidth: "20px",
  },
  content: {
    padding: "0",
  },
  contentText: {
    fontFamily: '"Menlo", "Monaco", "Consolas", monospace',
    fontSize: "12px",
    lineHeight: "18px",
    padding: "0 12px",
  },
  line: {
    minHeight: "37px",
  },
};

export default DIFF_STYLES;
