/**
 * ========================================
 * AJAX Utilities for Real-Time Updates
 * ========================================
 *
 * This file provides shared functions for
 * making AJAX calls and updating DOM elements
 * across all pages without page refreshes.
 */

/**
 * Generic AJAX fetch function
 * @param {string} url - The endpoint URL (e.g., '/api/bills')
 * @param {object} params - Query parameters as object (e.g., { sort: 'date', month: '2024-01' })
 * @returns {Promise<object>} - Parsed JSON response or null on error
 */
async function fetchData(url, params = {}) {
    try {
        // Build query string from params
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;

        // Make the fetch request
        const response = await fetch(fullUrl);

        // Check if response is ok
        if (!response.ok) {
            console.error(`HTTP Error: ${response.status}`);
            return null;
        }

        // Parse and return JSON
        return await response.json();

    } catch (error) {
        console.error('Fetch error:', error);
        return null;
    }
}

/**
 * Format DateTime string to readable format
 * @param {string} dateString - ISO format datetime (e.g., '2024-01-15T10:30:00')
 * @returns {string} - Formatted date and time (e.g., '15-01-2024 10:30')
 */
function formatDateTime(dateString) {
    if (!dateString) return 'N/A';

    const date = new Date(dateString);

    // Format date part (DD-MM-YYYY)
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    // Format time part (HH:MM)
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${day}-${month}-${year} ${hours}:${minutes}`;
}

/**
 * Show a temporary success message
 * @param {string} message - Message to display
 * @param {number} duration - Duration in milliseconds (default 3000)
 */
/**
 * Show a temporary success message
 * @param {string} message - Message to display
 * @param {duration} duration - Duration in milliseconds (default 5000)
 */
function showSuccessMessage(message, duration = 5000) {
    console.log('showSuccessMessage called with:', message); // Debug log

    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-success alert-dismissible fade show';
    alertDiv.setAttribute('role', 'alert');
    alertDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        width: auto;
        max-width: 400px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    `;
    alertDiv.innerHTML = `
        <strong>✓ Success!</strong> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    // Add to body instead of container
    document.body.appendChild(alertDiv);

    // Auto-remove after duration
    setTimeout(() => {
        alertDiv.remove();
    }, duration);
}
/**
 * Show a temporary error message
 * @param {string} message - Message to display
 * @param {number} duration - Duration in milliseconds (default 3000)
 */
/**
 * Show a temporary error message
 * @param {string} message - Message to display
 * @param {duration} duration - Duration in milliseconds (default 5000)
 */
function showErrorMessage(message, duration = 5000) {
    console.log('showErrorMessage called with:', message); // Debug log

    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show';
    alertDiv.setAttribute('role', 'alert');
    alertDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        width: auto;
        max-width: 400px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    `;
    alertDiv.innerHTML = `
        <strong>✗ Error!</strong> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    // Add to body instead of container
    document.body.appendChild(alertDiv);

    // Auto-remove after duration
    setTimeout(() => {
        alertDiv.remove();
    }, duration);
}
/**
 * Disable a button temporarily and show loading state
 * @param {HTMLElement} button - The button element
 * @param {string} loadingText - Text to show while loading (default 'Loading...')
 */
function setButtonLoading(button, loadingText = 'Loading...') {
    button.disabled = true;
    button.setAttribute('data-original-text', button.textContent);
    button.textContent = loadingText;
}

/**
 * Re-enable a button and restore original text
 * @param {HTMLElement} button - The button element
 */
function unsetButtonLoading(button) {
    button.disabled = false;
    const originalText = button.getAttribute('data-original-text');
    if (originalText) {
        button.textContent = originalText;
    }
}