interface Props {
  label: string;
  value: string;
  hint?: string;
  tone?: "default" | "good" | "bad";
}

export default function MetricCard({ label, value, hint, tone = "default" }: Props) {
  return (
    <div className={`metric-card ${tone}`}>
      <span className="metric-label">{label}</span>
      <span className="metric-value">{value}</span>
      {hint && <span className="metric-hint">{hint}</span>}
    </div>
  );
}