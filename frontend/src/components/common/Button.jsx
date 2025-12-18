import React from 'react';
import './Button.css';

const Button = ({ children, type = 'button', onClick, className = '', disabled = false }) => {
    return (
        <button
            type={type}
            onClick={onClick}
            disabled={disabled}
            className={`common-button ${className}`}
        >
            {children}
        </button>
    );
};

export default Button;
