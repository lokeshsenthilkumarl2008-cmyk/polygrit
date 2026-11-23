import { useState } from "react";

interface EditorTab {
  id: string;
  name: string;
}

interface Props {
  tabs: EditorTab[];
  activeId: string;
  onSelect: (id: string) => void;
  onAdd: () => void;
  onClose: (id: string) => void;
  onRename: (id: string, name: string) => void;
}

export default function TabBar({ tabs, activeId, onSelect, onAdd, onClose, onRename }: Props) {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [draft, setDraft] = useState("");

  const startEdit = (tab: EditorTab) => {
    setEditingId(tab.id);
    setDraft(tab.name);
  };

  const commitEdit = () => {
    if (editingId && draft.trim()) onRename(editingId, draft.trim());
    setEditingId(null);
  };

  return (
    <div className="tab-bar">
      {tabs.map((tab) => (
        <div
          key={tab.id}
          className={`tab ${tab.id === activeId ? "active" : ""}`}
          onClick={() => onSelect(tab.id)}
          onDoubleClick={() => startEdit(tab)}
          title="Double-click to rename"
        >
          {editingId === tab.id ? (
            <input
              className="tab-input"
              autoFocus
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onBlur={commitEdit}
              onClick={(e) => e.stopPropagation()}
              onKeyDown={(e) => {
                if (e.key === "Enter") commitEdit();
                if (e.key === "Escape") setEditingId(null);
              }}
            />
          ) : (
            <span>{tab.name}</span>
          )}
          {tabs.length > 1 && (
            <button
              className="tab-close"
              title="Close tab"
              onClick={(e) => {
                e.stopPropagation();
                onClose(tab.id);
              }}
            >
              &times;
            </button>
          )}
        </div>
      ))}
      <button className="tab-add" onClick={onAdd} title="New tab">
        +
      </button>
    </div>
  );
}