import React from 'react';
import './Input.css';

const Input = ({ label, type = 'text', value, onChange, required = false, placeholder = '' }) => {
    return (
        <div className="common-input-field">
            {label && <label className="common-input-label">{label}</label>}
            <input
                type={type}
                value={value}
                onChange={onChange}
                required={required}
                placeholder={placeholder}
                className="common-input"
            />
        </div>
    );
};

export default Input;
