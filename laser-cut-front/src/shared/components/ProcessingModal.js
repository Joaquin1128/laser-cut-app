import React from 'react';
import '../../features/quote/Step.css';

function ProcessingModal({ progress, title, message }) {
  return (
    <div className="processing-modal-overlay">
      <div className="processing-modal">
        <div className="circular-progress-container">
          <svg className="circular-progress" viewBox="0 0 120 120">
            <circle
              className="circular-progress-background"
              cx="60"
              cy="60"
              r="54"
              fill="none"
              stroke="#e5e5e5"
              strokeWidth="8"
            />
            <circle
              className="circular-progress-foreground"
              cx="60"
              cy="60"
              r="54"
              fill="none"
              stroke="#d32f2f"
              strokeWidth="8"
              strokeLinecap="round"
              strokeDasharray={`${2 * Math.PI * 54}`}
              strokeDashoffset={`${2 * Math.PI * 54 * (1 - progress / 100)}`}
              transform="rotate(-90 60 60)"
              style={{ transition: 'stroke-dashoffset 0.3s ease' }}
            />
          </svg>
          <div className="circular-progress-text">
            <span className="progress-percentage">{Math.round(progress)}%</span>
          </div>
        </div>
        <h2 className="processing-modal-title">{title}</h2>
        <p className="processing-modal-message">{message}</p>
      </div>
    </div>
  );
}

export default ProcessingModal;
