export type Language = "java" | "python" | "javascript";

export interface EditorTab {
  id: string;
  name: string;
  language: Language;
  content: string;
}