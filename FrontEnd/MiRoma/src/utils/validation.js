/**
 * Utilidades de validación y sanitización para prevenir SQL injection y XSS
 */

// Caracteres peligrosos para SQL injection
const DANGEROUS_SQL_CHARS = /['";\\]|(--)|(\/\*)|(\*\/)|(union\s+select)|(drop\s+table)|(delete\s+from)|(insert\s+into)|(update\s+set)|(exec\s*\()/gi;

// Caracteres peligrosos para XSS
const DANGEROUS_XSS_CHARS = /<[^>]*>|javascript:|on\w+\s*=/gi;

/**
 * Sanitiza un string eliminando caracteres peligrosos
 * @param {string} input - El string a sanitizar
 * @param {boolean} allowSpecialChars - Si permite algunos caracteres especiales básicos
 * @returns {string} - El string sanitizado
 */
export function sanitizeInput(input, allowSpecialChars = false) {
  if (!input || typeof input !== 'string') {
    return '';
  }

  // Eliminar caracteres peligrosos de SQL injection
  let sanitized = input.replace(DANGEROUS_SQL_CHARS, '');
  
  // Eliminar caracteres peligrosos de XSS
  sanitized = sanitized.replace(DANGEROUS_XSS_CHARS, '');
  
  // Eliminar espacios en blanco al inicio y final
  sanitized = sanitized.trim();
  
  // Si no se permiten caracteres especiales, eliminar más caracteres
  if (!allowSpecialChars) {
    // Permitir solo letras, números, espacios y algunos caracteres básicos
    sanitized = sanitized.replace(/[^a-zA-Z0-9\sáéíóúÁÉÍÓÚñÑüÜ.,!?@#%&()\-_]/g, '');
  }
  
  return sanitized;
}

/**
 * Valida y sanitiza un email
 * @param {string} email - El email a validar
 * @returns {string|null} - El email sanitizado o null si es inválido
 */
export function validateAndSanitizeEmail(email) {
  if (!email || typeof email !== 'string') {
    return null;
  }
  
  // Sanitizar primero
  let sanitized = email.trim().toLowerCase();
  
  // Validar formato de email básico
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(sanitized)) {
    return null;
  }
  
  // Eliminar caracteres peligrosos pero mantener estructura de email
  sanitized = sanitized.replace(/['";\\<>]/g, '');
  
  return sanitized;
}

/**
 * Valida y sanitiza un número
 * @param {string|number} input - El número a validar
 * @returns {number|null} - El número validado o null si es inválido
 */
export function validateAndSanitizeNumber(input) {
  if (input === null || input === undefined || input === '') {
    return null;
  }
  
  // Convertir a número
  const num = typeof input === 'string' ? parseFloat(input) : Number(input);
  
  // Verificar que sea un número válido
  if (isNaN(num) || !isFinite(num)) {
    return null;
  }
  
  return num;
}

/**
 * Valida y sanitiza un texto (nombre, descripción, etc.)
 * @param {string} input - El texto a validar
 * @param {number} maxLength - Longitud máxima permitida
 * @returns {string} - El texto sanitizado
 */
export function validateAndSanitizeText(input, maxLength = 500) {
  if (!input || typeof input !== 'string') {
    return '';
  }
  
  // Sanitizar
  let sanitized = sanitizeInput(input, true);
  
  // Limitar longitud
  if (sanitized.length > maxLength) {
    sanitized = sanitized.substring(0, maxLength);
  }
  
  return sanitized;
}

/**
 * Valida una fecha
 * @param {string} dateString - La fecha en formato string
 * @returns {string|null} - La fecha validada o null si es inválida
 */
export function validateAndSanitizeDate(dateString) {
  if (!dateString || typeof dateString !== 'string') {
    return null;
  }
  
  // Validar formato de fecha (YYYY-MM-DD)
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
  if (!dateRegex.test(dateString)) {
    return null;
  }
  
  // Verificar que sea una fecha válida
  const date = new Date(dateString);
  if (isNaN(date.getTime())) {
    return null;
  }
  
  return dateString;
}

/**
 * Valida un teléfono
 * @param {string} phone - El teléfono a validar
 * @returns {string} - El teléfono sanitizado
 */
export function validateAndSanitizePhone(phone) {
  if (!phone || typeof phone !== 'string') {
    return '';
  }
  
  // Permitir solo números, espacios, +, -, () y espacios
  let sanitized = phone.replace(/[^0-9+\-()\s]/g, '');
  
  // Limitar longitud
  if (sanitized.length > 20) {
    sanitized = sanitized.substring(0, 20);
  }
  
  return sanitized.trim();
}

/**
 * Valida una contraseña (solo verifica longitud y caracteres básicos)
 * @param {string} password - La contraseña a validar
 * @returns {boolean} - true si es válida, false si no
 */
export function validatePassword(password) {
  if (!password || typeof password !== 'string') {
    return false;
  }
  
  // Longitud mínima
  if (password.length < 6) {
    return false;
  }
  
  // Longitud máxima razonable
  if (password.length > 128) {
    return false;
  }
  
  return true;
}

/**
 * Valida que un string no contenga caracteres peligrosos
 * @param {string} input - El string a validar
 * @returns {boolean} - true si es seguro, false si contiene caracteres peligrosos
 */
export function isInputSafe(input) {
  if (!input || typeof input !== 'string') {
    return true;
  }
  
  // Verificar caracteres peligrosos de SQL
  if (DANGEROUS_SQL_CHARS.test(input)) {
    return false;
  }
  
  // Verificar caracteres peligrosos de XSS
  if (DANGEROUS_XSS_CHARS.test(input)) {
    return false;
  }
  
  return true;
}

