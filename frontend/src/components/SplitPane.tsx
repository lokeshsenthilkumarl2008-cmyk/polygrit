import { useRef, useState, type ReactNode } from "react";

interface Props {
  top: ReactNode;
  bottom: ReactNode;
  initialBottomHeight?: number;
}

export default function SplitPane({ top, bottom, initialBottomHeight = 220 }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [bottomHeight, setBottomHeight] = useState(initialBottomHeight);

  const startDrag = (e: React.MouseEvent) => {
    e.preventDefault();

    const onMove = (ev: MouseEvent) => {
      if (!containerRef.current) return;
      const rect = containerRef.current.getBoundingClientRect();
      const next = rect.bottom - ev.clientY;
      // Dono panes ki minimum height maintain karo
      setBottomHeight(Math.min(Math.max(next, 80), rect.height - 120));
    };

    const onUp = () => {
      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup", onUp);
    };

    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup", onUp);
  };

  return (
    <div className="split-pane" ref={containerRef}>
      <div className="split-top">{top}</div>
      <div className="split-divider" onMouseDown={startDrag} />
      <div className="split-bottom" style={{ height: bottomHeight }}>
        {bottom}
      </div>
    </div>
  );
}