const MIN_PARAGRAPH_LENGTH = 80;
const MAX_PARAGRAPH_LENGTH = 180;
const SENTENCE_END_CHARS = '。！？!?；;';
const SOFT_BREAK_CHARS = '，,、 ';
const CLOSING_CHARS = '”’」』）)》】';

function splitLongLine(line) {
  const text = line.replace(/\s+/g, ' ').trim();
  if (text.length <= MAX_PARAGRAPH_LENGTH) {
    return text ? [text] : [];
  }

  const lines = [];
  let start = 0;
  let index = 0;
  while (index < text.length) {
    const char = text[index];
    const currentLength = index - start + 1;
    if (SENTENCE_END_CHARS.includes(char) && currentLength >= MIN_PARAGRAPH_LENGTH) {
      let end = index + 1;
      while (end < text.length && CLOSING_CHARS.includes(text[end])) {
        end++;
      }
      lines.push(text.slice(start, end).trim());
      start = end;
      index = end;
      continue;
    }
    if (currentLength >= MAX_PARAGRAPH_LENGTH && SOFT_BREAK_CHARS.includes(char)) {
      lines.push(text.slice(start, index + 1).trim());
      start = index + 1;
    }
    index++;
  }

  const rest = text.slice(start).trim();
  if (rest) {
    lines.push(rest);
  }
  return lines;
}

export function splitSourceContentLines(content) {
  if (!content) {
    return [];
  }
  return content
    .split(/\r?\n/)
    .flatMap((line) => splitLongLine(line))
    .filter(Boolean);
}
