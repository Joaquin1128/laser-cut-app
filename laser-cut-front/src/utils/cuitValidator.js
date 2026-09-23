/**
 * Validador de CUIT / CUIL argentino según algoritmo Módulo 11 de AFIP.
 */
export const isValidCuit = (cuit) => {
  if (!cuit) return false;

  const clean = String(cuit).replace(/[^0-9]/g, '');
  if (clean.length !== 11) return false;

  const prefix = clean.substring(0, 2);
  const validPrefixes = ['20', '23', '24', '27', '30', '33', '34'];
  if (!validPrefixes.includes(prefix)) return false;

  const multipliers = [5, 4, 3, 2, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 10; i++) {
    sum += parseInt(clean.charAt(i), 10) * multipliers[i];
  }

  const mod = 11 - (sum % 11);
  let expectedVerifier;
  if (mod === 11) {
    expectedVerifier = 0;
  } else if (mod === 10) {
    expectedVerifier = 9;
  } else {
    expectedVerifier = mod;
  }

  const actualVerifier = parseInt(clean.charAt(10), 10);
  return actualVerifier === expectedVerifier;
};

/**
 * Formatea un CUIT como XX-XXXXXXXX-X
 */
export const formatCuit = (cuit) => {
  if (!cuit) return '';
  const clean = String(cuit).replace(/[^0-9]/g, '');
  if (clean.length <= 2) return clean;
  if (clean.length <= 10) return `${clean.slice(0, 2)}-${clean.slice(2)}`;
  return `${clean.slice(0, 2)}-${clean.slice(2, 10)}-${clean.slice(10, 11)}`;
};
