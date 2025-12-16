import React from 'react';
import {
  FiCrop,
  FiLayers,
  FiHash,
  FiCheckCircle,
} from 'react-icons/fi';
import './WizardSteps.css';

const STEP_ICONS = [
  FiCrop,
  FiLayers,
  FiHash,
  FiCheckCircle,
];

function WizardSteps({ stepLabels, stepSummaries, activeStep }) {
  return (
    <div className="wizard-steps-fixed">
      <div className="wizard-steps-container">
        <div className="progress-bar progress-bar-desktop">
          {stepLabels.map((label, index) => {
            const stepNumber = index + 1;
            const isActive = activeStep === stepNumber;
            const isCompleted = activeStep > stepNumber;
            const isLast = index === stepLabels.length - 1;
            const isFinalActive = isLast && isActive;

            const summary = stepSummaries[index];
            const Icon = STEP_ICONS[index];

            const wrapperClass = [
              'progress-step-wrapper',
              isCompleted && 'completed',
              isFinalActive && 'completed',
            ]
              .filter(Boolean)
              .join(' ');

            const circleClass = [
              'progress-step-circle',
              isCompleted && 'completed',
              isFinalActive && 'completed',
              isActive && !isFinalActive && 'active',
            ]
              .filter(Boolean)
              .join(' ');

            return (
              <div key={label} className={wrapperClass}>
                <div className={circleClass}>
                  <span className="step-icon">
                    <Icon />
                  </span>
                </div>

                <div className="progress-step-label">
                  <span
                    className="progress-step-text"
                    title={summary || label}
                  >
                    {summary || label}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export default WizardSteps;
