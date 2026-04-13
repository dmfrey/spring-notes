import '@testing-library/jest-dom'

// jsdom does not implement HTMLDialogElement methods
HTMLDialogElement.prototype.showModal = function () { this.setAttribute('open', '') }
HTMLDialogElement.prototype.close = function () { this.removeAttribute('open') }