import React from 'react';
import '../Wizard.css';

function WizardButtons({
  showBack = true,
  showNext = true,
  backLabel = 'ANTERIOR',
  nextLabel = 'SIGUIENTE',
  canContinue = true,
  isLoading = false,
  onBack,
  onNext,
}) {
  if (!showBack && !showNext) {
    return null;
  }

  return (
    <div className="wizard-header-buttons">
      {showBack && (
        <button
          type="button"
          className="btn-secondary"
          onClick={onBack}
          disabled={isLoading}
        >
          {backLabel}
        </button>
      )}
      {showNext && (
        <button
          type="button"
          className="btn-primary"
          onClick={onNext}
          disabled={isLoading || !canContinue}
        >
          {nextLabel}
        </button>
      )}
    </div>
  );
}

export default WizardButtons;
