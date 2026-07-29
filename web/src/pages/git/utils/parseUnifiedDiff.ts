export const parseUnifiedDiff = (
  diffText: string,
): { oldStr: string; newStr: string } => {
  const lines = diffText.split(/\r?\n/);
  const oldLines: string[] = [];
  const newLines: string[] = [];

  for (const raw of lines) {
    if (raw.startsWith("+")) {
      newLines.push(raw.slice(1));
    } else if (raw.startsWith("-")) {
      oldLines.push(raw.slice(1));
    } else if (raw.startsWith(" ")) {
      const content = raw.slice(1);
      oldLines.push(content);
      newLines.push(content);
    } else if (
      raw.startsWith("@@") ||
      raw.startsWith("---") ||
      raw.startsWith("+++")
    ) {
    } else {
      oldLines.push(raw);
      newLines.push(raw);
    }
  }

  return { oldStr: oldLines.join("\n"), newStr: newLines.join("\n") };
};
